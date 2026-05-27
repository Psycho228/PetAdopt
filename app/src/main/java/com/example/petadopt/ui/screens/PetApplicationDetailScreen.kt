package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavHostController
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.RiskAssessmentRecord
import com.example.petadopt.data.model.RiskLevel
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetApplicationDetailScreen(
    navController: NavHostController,
    application: Application,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var questionnaire by remember { mutableStateOf<QuestionnaireAnswer?>(null) }
    var riskAssessment by remember { mutableStateOf<RiskAssessmentRecord?>(null) }
    var isLoadingQuestionnaire by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    // Сбрасываем опросник и оценку рисков при изменении application.user_id
    LaunchedEffect(application.user_id) {
        isLoadingQuestionnaire = true
        questionnaire = null
        riskAssessment = null
        viewModel.loadQuestionnaireForUser(application.user_id)
    }

    // Слушаем изменения из ViewModel
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.currentQuestionnaire, uiState.currentRiskAssessment) {
        isLoadingQuestionnaire = false
        questionnaire = uiState.currentQuestionnaire
        riskAssessment = uiState.currentRiskAssessment
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Заявка от ${application.user_name}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Кнопки действий внизу экрана (всегда видны)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Отклонить")
                    }
                    PrimaryButton(
                        text = "Подтвердить",
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        if (isLoadingQuestionnaire) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (questionnaire != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Статус заявки
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = getStatusColor(application.status).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Статус: ${getStatusText(application.status)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(application.status)
                        )
                        Icon(
                            getStatusIcon(application.status),
                            contentDescription = null,
                            tint = getStatusColor(application.status)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Оценка рисков (если есть)
                if (riskAssessment != null) {
                    RiskAssessmentCard(
                        riskAssessment = riskAssessment!!,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Контактная информация (всегда видна)
                SectionTitle("Контактная информация")
                ContactCard(
                    Icons.Default.Phone,
                    "Телефон",
                    questionnaire!!.q1_phone.ifBlank { "Не указан" }
                )
                ContactCard(
                    Icons.Default.Email,
                    "Email",
                    questionnaire!!.q1_email.ifBlank { "Не указан" }
                )
                ContactCard(
                    Icons.Default.LocationOn,
                    "Город",
                    questionnaire!!.q1_city.ifBlank { "Не указан" }
                )
                ContactCard(
                    Icons.Default.Person,
                    "Возраст",
                    "${questionnaire!!.q1_age ?: "Не указан"} лет"
                )
                ContactCard(
                    Icons.Default.Business,
                    "Род занятий",
                    questionnaire!!.q1_occupation.ifBlank { "Не указан" }
                )

                Spacer(Modifier.height(16.dp))

                // Сообщение от заявителя (всегда видно)
                if (application.message.isNotBlank()) {
                    SectionTitle("Сообщение от заявителя")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            application.message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Предпочтительное время контакта (всегда видно)
                if (application.contact_time.isNotBlank()) {
                    SectionTitle("Предпочтительное время контакта")
                    ContactCard(
                        Icons.Default.AccessTime,
                        "Время для связи",
                        application.contact_time
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Разворачиваемые разделы
                var expandedHousing by remember { mutableStateOf(false) }
                var expandedExperience by remember { mutableStateOf(false) }
                var expandedPlans by remember { mutableStateOf(false) }

                // Жилищные условия (сворачиваемый)
                ExpandableSection(
                    title = "Жилищные условия",
                    expanded = expandedHousing,
                    onToggle = { expandedHousing = !expandedHousing }
                ) {
                    ContactCard(
                        Icons.Default.Home,
                        "Тип жилья",
                        questionnaire!!.q2_housing_type.ifBlank { "Не указано" }
                    )
                    ContactCard(
                        Icons.Default.People,
                        "С кем живёт",
                        questionnaire!!.q2_living_with.joinToString(", ").ifEmpty { "Не указано" }
                    )
                    ContactCard(
                        Icons.Default.People,
                        "Дети",
                        if (questionnaire!!.q2_has_children == true) 
                            "Да (${questionnaire!!.q2_children_ages})" 
                        else 
                            "Нет"
                    )
                    ContactCard(
                        Icons.Default.Pets,
                        "Другие животные",
                        if (questionnaire!!.q2_has_other_pets == true) 
                            "Да (${questionnaire!!.q2_other_pets_types.joinToString(", ")})" 
                        else 
                            "Нет"
                    )
                    ContactCard(
                        Icons.Default.Timer,
                        "Часов в одиночестве",
                        "${questionnaire!!.q2_hours_alone ?: 0} часов"
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Опыт с животными (сворачиваемый)
                ExpandableSection(
                    title = "Опыт с животными",
                    expanded = expandedExperience,
                    onToggle = { expandedExperience = !expandedExperience }
                ) {
                    ContactCard(
                        Icons.Default.CheckCircle,
                        "Опыт с собаками",
                        if (questionnaire!!.q3_dog_experience == true) "Да" else "Нет"
                    )
                    ContactCard(
                        Icons.Default.CheckCircle,
                        "Опыт с кошками",
                        if (questionnaire!!.q3_cat_experience == true) "Да" else "Нет"
                    )
                    if (questionnaire!!.q3_why_now.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Почему решили взять питомца сейчас?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                questionnaire!!.q3_why_now,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Планы действий (сворачиваемый)
                ExpandableSection(
                    title = "План действий в сложных ситуациях",
                    expanded = expandedPlans,
                    onToggle = { expandedPlans = !expandedPlans }
                ) {
                    if (questionnaire!!.q4_furniture_damage_plan.isNotBlank()) {
                        Text(
                            "Если питомец испортит мебель:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                questionnaire!!.q4_furniture_damage_plan,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (questionnaire!!.q4_noise_plan.isNotBlank()) {
                        Text(
                            "Если питомец будет шуметь:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                questionnaire!!.q4_noise_plan,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (questionnaire!!.q4_shy_pet_plan.isNotBlank()) {
                        Text(
                            "Если питомец окажется пугливым:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                questionnaire!!.q4_shy_pet_plan,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            // Если опросник не найден
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Информация о заявителе не найдена",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // Диалог подтверждения
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Подтвердить заявку?") },
            text = { Text("Вы подтверждаете эту заявку. Заявитель будет уведомлён.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "approved")
                        showConfirmDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Подтвердить", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог отклонения
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Отклонить заявку?") },
            text = { Text("Вы уверены, что хотите отклонить эту заявку? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "rejected")
                        showRejectDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Отклонить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ContactCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Заголовок с иконкой разворачивания
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = TextSecondary
                )
            }
            
            // Контент с анимацией
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
private fun RiskAssessmentCard(
    riskAssessment: RiskAssessmentRecord,
    modifier: Modifier = Modifier
) {
    val riskLevel = RiskLevel.valueOf(riskAssessment.overallRisk)
    val riskColor = getRiskColor(riskLevel)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖 Оценка рисков (GigaChat)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = riskColor
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Уровень риска
            Surface(
                color = riskColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (riskLevel) {
                        RiskLevel.LOW -> "🟢 Низкий риск"
                        RiskLevel.MEDIUM -> "🟡 Средний риск"
                        RiskLevel.HIGH -> "🔴 Высокий риск"
                        RiskLevel.VERY_HIGH -> "⚫ Очень высокий риск"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = riskColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Score
            Text(
                text = "Балл риска: ${riskAssessment.riskScore}/100",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Рекомендация
            if (riskAssessment.recommendation.isNotBlank()) {
                Text(
                    text = "Рекомендация:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = riskAssessment.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
            }
            
            // Подробный анализ
            if (riskAssessment.detailedAnalysis.isNotBlank()) {
                Text(
                    text = "Анализ:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = riskAssessment.detailedAnalysis,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            
            // Факторы риска (если есть в JSON)
            val json = Json { ignoreUnknownKeys = true }
            val riskFactors = try {
                if (riskAssessment.riskFactorsJson.isNotBlank()) {
                    json.decodeFromString<List<String>>(riskAssessment.riskFactorsJson)
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            if (riskFactors.isNotEmpty()) {
                Text(
                    text = "Выявленные риски:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                riskFactors.forEach { factor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "⚠️ ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = factor,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Положительные факторы (если есть в JSON)
            val positiveFactors = try {
                if (riskAssessment.positiveFactorsJson.isNotBlank()) {
                    json.decodeFromString<List<String>>(riskAssessment.positiveFactorsJson)
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            if (positiveFactors.isNotEmpty()) {
                Text(
                    text = "Положительные факторы:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                positiveFactors.forEach { factor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "✅ ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = factor,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFF9800)
        "processing" -> Color(0xFF2196F3)
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> TextSecondary
    }
}

@Composable
private fun getStatusText(status: String): String {
    return when (status) {
        "pending" -> "В ожидании"
        "processing" -> "В работе"
        "approved" -> "Принята"
        "rejected" -> "Отклонена"
        else -> status
    }
}

@Composable
private fun getStatusIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        "pending" -> Icons.Default.AccessTime
        "processing" -> Icons.Default.Build
        "approved" -> Icons.Default.CheckCircle
        "rejected" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }
}

@Composable
private fun getRiskColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MEDIUM -> Color(0xFFFF9800)
        RiskLevel.HIGH -> Color(0xFFF44336)
        RiskLevel.VERY_HIGH -> Color(0xFFB71C1C)
    }
}
