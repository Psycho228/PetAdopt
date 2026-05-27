package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetApplicationsScreen(
    navController: NavHostController,
    petId: String,
    petName: String,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val applications = uiState.applicationsByPet[petId] ?: emptyList()

    // При открытии экрана помечаем все pending заявки как "в работе"
    LaunchedEffect(Unit) {
        applications.filter { it.status == "pending" }.forEach { app ->
            viewModel.updateApplicationStatus(app.id, "processing")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Заявки на \"$petName\"", maxLines = 2)
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (applications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Нет заявок на этого питомца",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                // Статистика
                val pendingCount = applications.count { it.status == "pending" }
                val processingCount = applications.count { it.status == "processing" }
                val approvedCount = applications.count { it.status == "approved" }
                val rejectedCount = applications.count { it.status == "rejected" }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge("В ожидании", pendingCount, Color(0xFFFF9800))
                        StatBadge("В работе", processingCount, Color(0xFF2196F3))
                        StatBadge("Принято", approvedCount, Color(0xFF4CAF50))
                        if (rejectedCount > 0) {
                            StatBadge("Откл.", rejectedCount, Color(0xFFF44336))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Список заявок
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(applications.size, key = { applications[it].id }) { index ->
                        val app = applications[index]
                        ApplicationCard(
                            application = app,
                            onClick = {
                                navController.navigate(
                                    "admin/application/detail/${app.id}/${app.user_id}/${app.user_name}/${app.user_email}/${app.pet_id}/${app.pet_name}/${app.message}/${app.contact_time}/${app.status}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(64.dp),
            contentColor = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 2
        )
    }
}

@Composable
private fun ApplicationCard(
    application: Application,
    onClick: () -> Unit
) {
    val statusColor = when (application.status) {
        "pending" -> Color(0xFFFF9800)
        "processing" -> Color(0xFF2196F3)
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> TextSecondary
    }

    val statusText = when (application.status) {
        "pending" -> "В ожидании"
        "processing" -> "В работе"
        "approved" -> "Принята"
        "rejected" -> "Отклонена"
        else -> application.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = application.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = application.contactTime.ifBlank { "Не указано" },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = application.message.ifBlank { "Нет сообщения" }.take(50),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicantDetailDialog(
    application: Application,
    questionnaire: QuestionnaireAnswer,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Заявка от ${application.userName}") 
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Контактная информация
                SectionTitle("Контактная информация")
                ContactInfoRow(Icons.Default.Phone, "Телефон", questionnaire.q1_phone.ifBlank { "Не указан" })
                ContactInfoRow(Icons.Default.Email, "Email", questionnaire.q1_email.ifBlank { "Не указан" })
                ContactInfoRow(Icons.Default.LocationOn, "Город", questionnaire.q1_city.ifBlank { "Не указан" })
                
                Spacer(Modifier.height(12.dp))
                
                // Жилищные условия
                SectionTitle("Жилищные условия")
                ContactInfoRow(Icons.Default.Home, "Тип жилья", questionnaire.q2_housing_type.ifBlank { "Не указано" })
                ContactInfoRow(Icons.Default.PersonOutline, "С кем живёт", questionnaire.q2_living_with.joinToString(", ").ifEmpty { "Не указано" })
                ContactInfoRow(Icons.Default.People, "Дети", if (questionnaire.q2_has_children == true) "Да (${questionnaire.q2_children_ages})" else "Нет")
                ContactInfoRow(Icons.Default.Pets, "Другие животные", if (questionnaire.q2_has_other_pets == true) "Да (${questionnaire.q2_other_pets_types.joinToString(", ")})" else "Нет")
                ContactInfoRow(Icons.Default.Timer, "Часов в одиночестве", "${questionnaire.q2_hours_alone ?: 0} часов")
                
                Spacer(Modifier.height(12.dp))
                
                // Опыт
                SectionTitle("Опыт с животными")
                ContactInfoRow(Icons.Default.CheckCircle, "Опыт с собаками", if (questionnaire.q3_dog_experience == true) "Да" else "Нет")
                ContactInfoRow(Icons.Default.CheckCircle, "Опыт с кошками", if (questionnaire.q3_cat_experience == true) "Да" else "Нет")
                if (questionnaire.q3_why_now.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Почему сейчас?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        questionnaire.q3_why_now,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Ответственность
                SectionTitle("План действий")
                if (questionnaire.q4_furniture_damage_plan.isNotBlank()) {
                    Text(
                        "Порча мебели:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        questionnaire.q4_furniture_damage_plan,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (questionnaire.q4_noise_plan.isNotBlank()) {
                    Text(
                        "Шум:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        questionnaire.q4_noise_plan,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (questionnaire.q4_shy_pet_plan.isNotBlank()) {
                    Text(
                        "Пугливый питомец:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        questionnaire.q4_shy_pet_plan,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Сообщение от заявителя
                if (application.message.isNotBlank()) {
                    SectionTitle("Сообщение от заявителя")
                    Text(
                        application.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Желаемый контакт
                if (application.contact_time.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Предпочтительное время контакта:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        application.contact_time,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    onDismiss()
                    onReject()
                }) {
                    Text("Отклонить", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = {
                    onDismiss()
                    onApprove()
                }) {
                    Text("Принять", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Если заявка была "в работе", возвращаем в "в ожидании"
                if (application.status == "processing") {
                    onApprove() // Просто закрываем, статус не меняем (уже processing)
                }
                onDismiss()
            }) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ContactInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}