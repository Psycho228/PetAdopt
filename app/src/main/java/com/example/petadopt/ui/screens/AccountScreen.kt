package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.data.model.*
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.Secondary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onApplications: () -> Unit,
    onEditProfile: () -> Unit,
    onRetakeQuestionnaire: () -> Unit,
    onAdminPanel: () -> Unit = {},
    onMarketplace: () -> Unit = {},
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showQuestionnaireDialog by remember { mutableStateOf(false) }
    var showRiskAssessment by remember { mutableStateOf(false) }
    var riskAssessmentDialogData by remember { mutableStateOf<RiskAssessmentRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Primary,
                    navigationIconContentColor = Primary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header с аватаром и именем
                HeaderSection(user = state.user, onEditClick = onEditProfile)

                Spacer(Modifier.height(20.dp))

                // Секция "Мои заявки"
                ApplicationsSection(onClick = onApplications)

                Spacer(Modifier.height(16.dp))

                MarketplaceAccountSection(onClick = onMarketplace)

                Spacer(Modifier.height(16.dp))

                // Секция "Личные данные"
                PersonalDataSection(user = state.user)

                Spacer(Modifier.height(16.dp))

                // Секция "Опросник"
                QuestionnaireSection(
                    questionnaire = state.questionnaire,
                    onClick = { showQuestionnaireDialog = true }
                )

                Spacer(Modifier.height(16.dp))

                // Секция "Оценка рисков"
                RiskAssessmentSection(
                    riskAssessment = state.riskAssessment,
                    onClick = {
                        state.riskAssessment?.let {
                            riskAssessmentDialogData = it
                            showRiskAssessment = true
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Секция "Админ-панель" (только для администраторов и приютов)
                if (state.isAdmin || state.isShelter) {
                    AdminPanelSection(
                        isAdmin = state.isAdmin,
                        onClick = onAdminPanel
                    )

                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showQuestionnaireDialog && state.questionnaire != null) {
        FullQuestionnaireDialog(
            questionnaire = state.questionnaire!!,
            onDismiss = { showQuestionnaireDialog = false },
            onRetake = {
                showQuestionnaireDialog = false
                onRetakeQuestionnaire()
            }
        )
    }
    
    // Диалог отображения полной оценки рисков
    if (showRiskAssessment && riskAssessmentDialogData != null) {
        riskAssessmentDialogData?.let { record ->
            AlertDialog(
                onDismissRequest = { 
                    showRiskAssessment = false 
                    riskAssessmentDialogData = null
                },
                title = { Text("Детальная оценка рисков") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        // Уровень риска
                        val riskLevel = RiskLevel.valueOf(record.overallRisk)
                        val riskColor = when (riskLevel) {
                            RiskLevel.LOW -> Color(0xFF4CAF50)
                            RiskLevel.MEDIUM -> Color(0xFFFF9800)
                            RiskLevel.HIGH -> Color(0xFFF44336)
                            RiskLevel.VERY_HIGH -> Color(0xFFB71C1C)
                        }
                        
                        Surface(
                            color = riskColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${when(riskLevel) {
                                    RiskLevel.LOW -> "Низкий"
                                    RiskLevel.MEDIUM -> "Средний"
                                    RiskLevel.HIGH -> "Высокий"
                                    RiskLevel.VERY_HIGH -> "Очень высокий"
                                }} риск • ${record.riskScore}/100",
                                style = MaterialTheme.typography.titleMedium,
                                color = riskColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Анализ
                        Text("Анализ:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(record.detailedAnalysis, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Рекомендация
                        Text("Рекомендация:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (record.recommendation.lowercase()) {
                                    "approve", "рекомендуется одобрить" -> "✅ Рекомендуется одобрить"
                                    "approve_with_conditions", "одобрить с условиями" -> "⚠️ Одобрить с условиями"
                                    "review_required", "требуется дополнительная проверка" -> "🔍 Требуется дополнительная проверка"
                                    "reject", "рекомендуется отклонить" -> "❌ Рекомендуется отклонить"
                                    else -> "🔍 ${record.recommendation}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Понятно",
                        onClick = {
                            showRiskAssessment = false
                            riskAssessmentDialogData = null
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(user: com.example.petadopt.data.model.User?, onEditClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.15f))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Primary
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = user?.name ?: "Неизвестно",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = user?.email ?: "Нет email",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Редактировать профиль")
        }
    }
}

@Composable
private fun ApplicationsSection(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Мои заявки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Посмотреть все заявки",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun MarketplaceAccountSection(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Secondary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Secondary
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "От заводчиков",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Объявления и кабинет заводчика",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun PersonalDataSection(user: com.example.petadopt.data.model.User?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Личные данные",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(16.dp))

            InfoItem(icon = Icons.Default.Person, label = "Имя", value = user?.name ?: "—")
            Spacer(Modifier.height(12.dp))
            InfoItem(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "—")
        }
    }
}

@Composable
private fun QuestionnaireSection(
    questionnaire: com.example.petadopt.data.model.QuestionnaireAnswer?,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Опросник",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            if (questionnaire != null) {
                InfoItem(
                    icon = Icons.Default.Place,
                    label = "Город",
                    value = questionnaire.city.ifBlank { "—" }
                )
                Spacer(Modifier.height(12.dp))
                InfoItem(
                    icon = Icons.Default.Home,
                    label = "Тип жилья",
                    value = questionnaire.housingType.ifBlank { "—" }
                )
                Spacer(Modifier.height(12.dp))
                InfoItem(
                    icon = Icons.Default.Person,
                    label = "Есть ли дети",
                    value = questionnaire.hasChildren.ifBlank { "—" }
                )
                Spacer(Modifier.height(12.dp))
                InfoItem(
                    icon = Icons.Default.Person,
                    label = "Есть ли другие животные",
                    value = questionnaire.hasOtherAnimals.ifBlank { "—" }
                )
                Spacer(Modifier.height(12.dp))
                InfoItem(
                    icon = Icons.Default.Home,
                    label = "С кем живёте",
                    value = questionnaire.livingWith.ifBlank { "—" }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Опросник не заполнен",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextSecondary
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullQuestionnaireDialog(
    questionnaire: com.example.petadopt.data.model.QuestionnaireAnswer,
    onDismiss: () -> Unit,
    onRetake: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ответы на опросник", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // 1. Основная информация
                SectionTitle("1. Основная информация")
                QuestionAnswer("Имя", questionnaire.name)
                QuestionAnswer("Возраст", questionnaire.age)
                QuestionAnswer("Город", questionnaire.city)
                QuestionAnswer("Занятость", questionnaire.occupation)
                QuestionAnswer("Телефон", questionnaire.phone)
                QuestionAnswer("Email", questionnaire.email)
                
                Spacer(Modifier.height(16.dp))
                
                // 2. Жилищные условия
                SectionTitle("2. Жилищные условия")
                QuestionAnswer("Тип жилья", questionnaire.housingType)
                QuestionAnswer("Разрешены ли животные", questionnaire.petsAllowed)
                QuestionAnswer("С кем живёте", questionnaire.livingWith)
                QuestionAnswer("Семья согласна", questionnaire.familyAgreement)
                QuestionAnswer("Есть ли дети", questionnaire.hasChildren)
                if (questionnaire.hasChildren == "Да") {
                    QuestionAnswer("Возраст детей", questionnaire.childrenAge)
                }
                QuestionAnswer("Есть ли другие животные", questionnaire.hasOtherAnimals)
                if (questionnaire.hasOtherAnimals == "Да") {
                    QuestionAnswer("Какие животные", questionnaire.otherAnimals)
                }
                QuestionAnswer("Часов в одиночестве", questionnaire.hoursAlone)
                QuestionAnswer("Кто ухаживает в отпуске", questionnaire.caretaker)
                
                Spacer(Modifier.height(16.dp))
                
                // 3. Опыт с животными
                SectionTitle("3. Опыт с животными")
                QuestionAnswer("Были ли питомцы раньше", questionnaire.hadPetsBefore)
                QuestionAnswer("Что с ними сейчас", questionnaire.petsNow)
                QuestionAnswer("Опыт с собаками", questionnaire.experienceDogs)
                QuestionAnswer("Опыт с кошками", questionnaire.experienceCats)
                QuestionAnswer("Опыт с особыми животными", questionnaire.experienceSpecialNeeds)
                QuestionAnswer("Почему сейчас", questionnaire.reasonNow)
                
                Spacer(Modifier.height(16.dp))
                
                // 4. Ответственность и готовность
                SectionTitle("4. Ответственность и готовность")
                QuestionAnswer("Понимаете потребности", questionnaire.understandsNeeds.ifEmpty { listOf("—") }.joinToString(", "))
                QuestionAnswer("Готовы к расходам", questionnaire.readyForExpenses.ifEmpty { listOf("—") }.joinToString(", "))
                QuestionAnswer("Если испортит мебель", questionnaire.furnitureDamage.ifBlank { "—" })
                QuestionAnswer("Если будет шуметь", questionnaire.noiseBehavior.ifBlank { "—" })
                QuestionAnswer("Если пугливый", questionnaire.timidPet.ifBlank { "—" })
                QuestionAnswer("Адаптация", questionnaire.adaptation.ifBlank { "—" })
                QuestionAnswer("Готовы к воспитанию", questionnaire.willingToTrain)
                QuestionAnswer("При изменениях", questionnaire.lifeChanges.ifBlank { "—" })
                QuestionAnswer("Возможные препятствия", questionnaire.obstacles.ifBlank { "—" })
                
                Spacer(Modifier.height(16.dp))
                
                // 5. Безопасность
                SectionTitle("5. Безопасность")
                QuestionAnswer("Меры безопасности", questionnaire.safetyMeasures.joinToString(", ") { it.ifBlank { "—" } })
                QuestionAnswer("Готовы к процедурам", questionnaire.willingTo.ifEmpty { listOf("—") }.joinToString(", "))
                QuestionAnswer("Поддерживать связь", questionnaire.maintainContact)
                
                Spacer(Modifier.height(16.dp))
                
                // 6. Эмоциональная часть
                SectionTitle("6. Эмоциональная часть")
                QuestionAnswer("Ответственный хозяин", questionnaire.responsibleOwner.ifBlank { "—" })
                QuestionAnswer("Жизнь с питомцем", questionnaire.lifeWithPet.ifBlank { "—" })
                QuestionAnswer("Почему вы хороший хозяин", questionnaire.whyGoodOwner.ifBlank { "—" })
                
                Spacer(Modifier.height(16.dp))
                
                // 7. Желаемые питомцы
                SectionTitle("7. Желаемые питомцы")
                QuestionAnswer("Кого хотите взять", questionnaire.desiredPets.ifBlank { "—" })
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Пройти ещё раз")
                }
                TextButton(onClick = onDismiss) {
                    Text("Закрыть", color = Primary)
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Primary
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun QuestionAnswer(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(0.55f)
        )
    }
}

@Composable
private fun RiskAssessmentSection(
    riskAssessment: RiskAssessmentRecord?,
    onClick: () -> Unit
) {
    if (riskAssessment != null) {
        val riskLevel = RiskLevel.valueOf(riskAssessment.overallRisk)
        val (riskText, riskColor) = when (riskLevel) {
            RiskLevel.LOW -> Pair("Низкий риск", Color(0xFF4CAF50))
            RiskLevel.MEDIUM -> Pair("Средний риск", Color(0xFFFF9800))
            RiskLevel.HIGH -> Pair("Высокий риск", Color(0xFFF44336))
            RiskLevel.VERY_HIGH -> Pair("Очень высокий риск", Color(0xFFB71C1C))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(riskColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Оценка рисков",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${riskText} • ${riskAssessment.riskScore}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = riskColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = riskColor
                )
            }
        }
    } else {
        // Если оценки нет, показываем информационную карточку
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Оценка рисков не пройдена",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Пройдите опросник для получения оценки",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminPanelSection(
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = if (isAdmin) "Админ-панель" else "Кабинет приюта",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isAdmin) "Управление всеми питомцами" else "Управление питомцами приюта",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
