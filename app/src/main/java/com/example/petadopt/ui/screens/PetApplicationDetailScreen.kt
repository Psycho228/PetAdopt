package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.RiskAssessmentRecord
import com.example.petadopt.data.model.RiskLevel
import com.example.petadopt.data.model.normalizeRecommendationText
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import kotlinx.serialization.json.Json

// ─── Цвета для уровней риска ────────────────────────────────────
private val RiskLow = Color(0xFF4CAF50)
private val RiskMedium = Color(0xFFFF9800)
private val RiskHigh = Color(0xFFF44336)
private val RiskVeryHigh = Color(0xFFB71C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetApplicationDetailScreen(
    navController: NavHostController,
    applicationId: String,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val application = uiState.currentApplication
    var questionnaire by remember { mutableStateOf<QuestionnaireAnswer?>(null) }
    var riskAssessment by remember { mutableStateOf<RiskAssessmentRecord?>(null) }
    var isLoadingQuestionnaire by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(applicationId) {
        isLoadingQuestionnaire = true
        questionnaire = null
        riskAssessment = null
        viewModel.loadApplicationById(applicationId)
    }

    LaunchedEffect(application, uiState.currentQuestionnaire, uiState.currentRiskAssessment) {
        isLoadingQuestionnaire = application == null || uiState.isLoading
        questionnaire = uiState.currentQuestionnaire
        riskAssessment = uiState.currentRiskAssessment
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Заявка", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = application?.user_name.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
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
            if (application != null) Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = { navController.navigate("chat/${application.id}") },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Открыть чат",
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Отказать", maxLines = 1)
                    }
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiskLow),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Принять", maxLines = 1)
                    }
                }
            }
        }
    ) { padding ->
        if (isLoadingQuestionnaire) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (application != null && questionnaire != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ═══ СТАТУС ЗАЯВКИ ═══
                StatusBanner(status = application.status)

                Spacer(Modifier.height(12.dp))

                // ═══ ДЕТАЛЬНАЯ ОЦЕНКА РИСКОВ (GigaChat) — главный блок ═══
                if (riskAssessment != null) {
                    DetailedRiskAssessment(
                        risk = riskAssessment!!,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                } else {
                    // Плейсхолдер если оценки нет
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Оценка рисков не проводилась",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ═══ КОНТАКТНАЯ ИНФОРМАЦИЯ ═══
                SectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "Контактная информация"
                )
                ContactRow(Icons.Outlined.Phone, "Телефон", questionnaire!!.q1_phone.ifBlank { "Не указан" })
                ContactRow(Icons.Outlined.Email, "Email", questionnaire!!.q1_email.ifBlank { "Не указан" })
                ContactRow(Icons.Outlined.LocationOn, "Город", questionnaire!!.q1_city.ifBlank { "Не указан" })
                ContactRow(Icons.Outlined.Cake, "Возраст", "${questionnaire!!.q1_age ?: "Не указан"} лет")
                ContactRow(Icons.Outlined.BusinessCenter, "Род занятий", questionnaire!!.q1_occupation.ifBlank { "Не указан" })

                Spacer(Modifier.height(8.dp))

                // ═══ СООБЩЕНИЕ ОТ ЗАЯВИТЕЛЯ ═══
                if (application.message.isNotBlank()) {
                    SectionHeader(
                        icon = Icons.Outlined.Message,
                        title = "Сообщение от заявителя"
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Primary.copy(alpha = 0.06f)
                        )
                    ) {
                        Text(
                            application.message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ═══ ВРЕМЯ КОНТАКТА ═══
                if (application.contact_time?.isNotBlank() == true || application.contact_days?.isNotBlank() == true) {
                    SectionHeader(
                        icon = Icons.Outlined.Schedule,
                        title = "Предпочтительное время контакта"
                    )
                    if (application.contact_days?.isNotBlank() == true) {
                        ContactRow(Icons.Outlined.CalendarToday, "Дни недели", application.contact_days ?: "")
                    }
                    if (application.contact_time?.isNotBlank() == true) {
                        ContactRow(Icons.Outlined.AccessTime, "Время для связи", application.contact_time ?: "")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ═══ СВОРАЧИВАЕМЫЕ РАЗДЕЛЫ ОПРОСНИКА ═══
                var expandedHousing by remember { mutableStateOf(false) }
                var expandedExperience by remember { mutableStateOf(false) }
                var expandedPlans by remember { mutableStateOf(false) }
                var expandedResponsibility by remember { mutableStateOf(false) }
                var expandedSafety by remember { mutableStateOf(false) }
                var expandedEmotional by remember { mutableStateOf(false) }
                var expandedDesired by remember { mutableStateOf(false) }

                // Жилищные условия
                QuestionnaireExpandable(
                    title = "Жилищные условия",
                    expanded = expandedHousing,
                    onToggle = { expandedHousing = !expandedHousing }
                ) {
                    ContactRow(Icons.Outlined.Home, "Тип жилья", questionnaire!!.q2_housing_type.ifBlank { "Не указано" })
                    ContactRow(Icons.Outlined.Groups, "С кем живёт", questionnaire!!.q2_living_with.joinToString(", ").ifEmpty { "Не указано" })
                    ContactRow(Icons.Outlined.ChildCare, "Дети",
                        if (questionnaire!!.q2_has_children == true) "Да (${questionnaire!!.q2_children_ages})" else "Нет")
                    ContactRow(Icons.Outlined.Pets, "Другие животные",
                        if (questionnaire!!.q2_has_other_pets == true) "Да (${questionnaire!!.q2_other_pets_types.joinToString(", ")})" else "Нет")
                    ContactRow(Icons.Outlined.Timer, "Часов в одиночестве", "${questionnaire!!.q2_hours_alone ?: 0} ч.")
                }

                // Опыт
                QuestionnaireExpandable(
                    title = "Опыт с животными",
                    expanded = expandedExperience,
                    onToggle = { expandedExperience = !expandedExperience }
                ) {
                    ContactRow(Icons.Outlined.CheckCircle, "Опыт с собаками", yn(questionnaire!!.q3_dog_experience))
                    ContactRow(Icons.Outlined.CheckCircle, "Опыт с кошками", yn(questionnaire!!.q3_cat_experience))
                    if (questionnaire!!.q3_why_now.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        TextBlock("Почему сейчас?", questionnaire!!.q3_why_now)
                    }
                }

                // Планы
                QuestionnaireExpandable(
                    title = "План в сложных ситуациях",
                    expanded = expandedPlans,
                    onToggle = { expandedPlans = !expandedPlans }
                ) {
                    if (questionnaire!!.q4_furniture_damage_plan.isNotBlank())
                        TextBlock("Порча мебели", questionnaire!!.q4_furniture_damage_plan)
                    if (questionnaire!!.q4_noise_plan.isNotBlank())
                        TextBlock("Шум", questionnaire!!.q4_noise_plan)
                    if (questionnaire!!.q4_shy_pet_plan.isNotBlank())
                        TextBlock("Пугливый питомец", questionnaire!!.q4_shy_pet_plan)
                    if (questionnaire!!.q4_long_adaptation_plan.isNotBlank())
                        TextBlock("Долгая адаптация", questionnaire!!.q4_long_adaptation_plan)
                }

                // Ответственность
                QuestionnaireExpandable(
                    title = "Ответственность и готовность",
                    expanded = expandedResponsibility,
                    onToggle = { expandedResponsibility = !expandedResponsibility }
                ) {
                    val understand = mutableListOf<String>()
                    if (questionnaire!!.q4_understand_time == true) understand.add("Время")
                    if (questionnaire!!.q4_understand_attention == true) understand.add("Внимание")
                    if (questionnaire!!.q4_understand_training == true) understand.add("Обучение")
                    if (questionnaire!!.q4_understand_vet_care == true) understand.add("Ветпомощь")
                    ContactRow(Icons.Outlined.Lightbulb, "Понимание требований", understand.joinToString(", ").ifEmpty { "—" })

                    val expenses = mutableListOf<String>()
                    if (questionnaire!!.q4_ready_food == true) expenses.add("Корм")
                    if (questionnaire!!.q4_ready_vet == true) expenses.add("Ветеринар")
                    if (questionnaire!!.q4_ready_medication == true) expenses.add("Лекарства")
                    if (questionnaire!!.q4_ready_vaccinations == true) expenses.add("Прививки")
                    if (questionnaire!!.q4_ready_grooming == true) expenses.add("Груминг")
                    ContactRow(Icons.Outlined.AttachMoney, "Готовность к расходам", expenses.joinToString(", ").ifEmpty { "—" })

                    ContactRow(Icons.Outlined.School, "Готовность к воспитанию", yn(questionnaire!!.q4_ready_education))

                    if (questionnaire!!.q4_life_changes_plan.isNotBlank())
                        TextBlock("При изменении обстоятельств", questionnaire!!.q4_life_changes_plan)
                    if (questionnaire!!.q4_obstacles_next_year.isNotBlank())
                        TextBlock("Препятствия в ближайший год", questionnaire!!.q4_obstacles_next_year)
                }

                // Безопасность
                QuestionnaireExpandable(
                    title = "Безопасность",
                    expanded = expandedSafety,
                    onToggle = { expandedSafety = !expandedSafety }
                ) {
                    ContactRow(Icons.Outlined.Security, "Меры безопасности",
                        questionnaire!!.q5_safety_measures.joinToString(", ").ifEmpty { "Не указаны" })
                    ContactRow(Icons.Outlined.VolunteerActivism, "Готовность к стерилизации", yn(questionnaire!!.q5_ready_neuter))
                    ContactRow(Icons.Outlined.Recommend, "Следование рекомендациям", yn(questionnaire!!.q5_ready_recommendations))
                    ContactRow(Icons.Outlined.Badge, "Установка адресника", yn(questionnaire!!.q5_ready_tracker))
                    ContactRow(Icons.Outlined.ConnectWithoutContact, "Поддержание связи", yn(questionnaire!!.q5_ready_keep_contact))
                }

                // Эмоциональная
                QuestionnaireExpandable(
                    title = "Эмоциональная часть",
                    expanded = expandedEmotional,
                    onToggle = { expandedEmotional = !expandedEmotional }
                ) {
                    if (questionnaire!!.q6_responsible_owner_meaning.isNotBlank())
                        TextBlock("Ответственный хозяин — это?", questionnaire!!.q6_responsible_owner_meaning)
                    if (questionnaire!!.q6_life_with_pet_vision.isNotBlank())
                        TextBlock("Жизнь с питомцем", questionnaire!!.q6_life_with_pet_vision)
                    if (questionnaire!!.q6_why_good_owner.isNotBlank())
                        TextBlock("Почему хороший хозяин?", questionnaire!!.q6_why_good_owner)
                }

                // Желаемые виды
                QuestionnaireExpandable(
                    title = "Желаемые виды животных",
                    expanded = expandedDesired,
                    onToggle = { expandedDesired = !expandedDesired }
                ) {
                    ContactRow(Icons.Outlined.FavoriteBorder, "Интересуют",
                        questionnaire!!.q7_desired_pets.joinToString(", ").ifEmpty { "Не указаны" })
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            // Опросник не найден
            EmptyQuestionnaire(modifier = Modifier.padding(padding))
        }
    }

    // ── Диалоги подтверждения ──
    if (showConfirmDialog && application != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = RiskLow, modifier = Modifier.size(40.dp)) },
            title = { Text("Подтвердить заявку?", fontWeight = FontWeight.Bold) },
            text = { Text("Заявка от ${application.user_name} будет одобрена. Заявитель получит уведомление.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "approved")
                        showConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskLow)
                ) { Text("Подтвердить") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Отмена") } }
        )
    }

    if (showRejectDialog && application != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp)) },
            title = { Text("Отклонить заявку?", fontWeight = FontWeight.Bold) },
            text = { Text("Заявка от ${application.user_name} будет отклонена. Это действие можно отменить на экране деталей.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "rejected")
                        showRejectDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Отклонить") }
            },
            dismissButton = { TextButton(onClick = { showRejectDialog = false }) { Text("Отмена") } }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ДЕТАЛЬНАЯ ОЦЕНКА РИСКОВ (ГЛАВНЫЙ БЛОК)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DetailedRiskAssessment(
    risk: RiskAssessmentRecord,
    modifier: Modifier = Modifier
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val riskLevel = remember(risk.overallRisk) {
        runCatching { RiskLevel.valueOf(risk.overallRisk) }.getOrDefault(RiskLevel.MEDIUM)
    }
    val riskColor = riskColor(riskLevel)

    val riskFactors = remember(risk.riskFactorsJson) {
        if (risk.riskFactorsJson.isNotBlank())
            runCatching { json.decodeFromString<List<String>>(risk.riskFactorsJson) }.getOrDefault(emptyList())
        else emptyList()
    }

    val positiveFactors = remember(risk.positiveFactorsJson) {
        if (risk.positiveFactorsJson.isNotBlank())
            runCatching { json.decodeFromString<List<String>>(risk.positiveFactorsJson) }.getOrDefault(emptyList())
        else emptyList()
    }

    val recommendations = remember(risk.recommendationsJson) {
        if (risk.recommendationsJson.isNotBlank())
            runCatching { json.decodeFromString<List<String>>(risk.recommendationsJson) }.getOrDefault(emptyList())
        else emptyList()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Шапка с градиентом ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                riskColor,
                                riskColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Assessment,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Оценка рисков",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "🔒 GigaChat AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    // Бейдж уровня риска
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = levelLabel(riskLevel),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // ── Балл риска с прогресс-баром ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Балл риска",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${risk.riskScore} / 100",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
                Spacer(Modifier.height(8.dp))

                // Кастомный прогресс-бар
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (risk.riskScore / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(RiskLow, RiskMedium, RiskHigh)
                                )
                            )
                    )
                }

                // Метки под прогресс-баром
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Низкий", style = MaterialTheme.typography.labelSmall, color = RiskLow)
                    Text("Средний", style = MaterialTheme.typography.labelSmall, color = RiskMedium)
                    Text("Высокий", style = MaterialTheme.typography.labelSmall, color = RiskHigh)
                }

                Spacer(Modifier.height(20.dp))

                // ── Итоговая рекомендация ──
                Text(
                    text = "Вердикт",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskColor.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (riskLevel) {
                                RiskLevel.LOW -> Icons.Outlined.ThumbUp
                                RiskLevel.MEDIUM -> Icons.Outlined.Info
                                RiskLevel.HIGH -> Icons.Outlined.WarningAmber
                                RiskLevel.VERY_HIGH -> Icons.Outlined.GppBad
                            },
                            null,
                            tint = riskColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = risk.recommendation.takeIf { it.isNotBlank() }
                                ?.let(::normalizeRecommendationText)
                                ?: run {
                                when (riskLevel) {
                                    RiskLevel.LOW -> "Рекомендуется одобрить"
                                    RiskLevel.MEDIUM -> "Одобрить с условиями"
                                    RiskLevel.HIGH -> "Требуется дополнительная проверка"
                                    RiskLevel.VERY_HIGH -> "Рекомендуется отклонить"
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }

                // ── Детальный анализ ──
                if (risk.detailedAnalysis.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Детальный анализ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = risk.detailedAnalysis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // ── Факторы риска ──
                if (riskFactors.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    FactorSection(
                        title = "Факторы риска",
                        emoji = "⚠️",
                        items = riskFactors,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // ── Положительные факторы ──
                if (positiveFactors.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    FactorSection(
                        title = "Положительные факторы",
                        emoji = "✅",
                        items = positiveFactors,
                        color = RiskLow
                    )
                }

                // ── Рекомендации ──
                if (recommendations.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Рекомендации",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    recommendations.forEachIndexed { idx, rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = rec.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FactorSection(
    title: String,
    emoji: String,
    items: List<String>,
    color: Color
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
    )
    Spacer(Modifier.height(6.dp))
    items.forEach { item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(emoji, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(6.dp))
            Text(
                text = item.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.85f),
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusBanner(status: String) {
    val color = getStatusColor(status)
    val text = getStatusText(status)
    val icon = getStatusIcon(status)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Статус заявки",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TextBlock(label: String, text: String) {
    Text(
        text = label,
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun QuestionnaireExpandable(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(16.dp))
            Text("Загрузка данных заявителя...", color = TextSecondary, fontSize = 15.sp)
        }
    }
}

@Composable
private fun EmptyQuestionnaire(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PersonOff, null, modifier = Modifier.size(48.dp), tint = TextSecondary.copy(alpha = 0.5f))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Данные заявителя не найдены", fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text("Пользователь не заполнил опросник", style = MaterialTheme.typography.bodyMedium, color = TextSecondary.copy(alpha = 0.6f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// УТИЛИТЫ
// ═══════════════════════════════════════════════════════════════

private fun yn(value: Boolean?): String = if (value == true) "Да" else "Нет"

private fun getStatusColor(status: String): Color = when (status) {
    "pending" -> Color(0xFFFF9800)
    "processing" -> Color(0xFF2196F3)
    "approved" -> RiskLow
    "rejected" -> RiskHigh
    else -> TextSecondary
}

private fun getStatusText(status: String): String = when (status) {
    "pending" -> "В ожидании"
    "processing" -> "В работе"
    "approved" -> "Принята"
    "rejected" -> "Отклонена"
    else -> status
}

@Composable
private fun getStatusIcon(status: String) = when (status) {
    "pending" -> Icons.Outlined.HourglassTop
    "processing" -> Icons.Outlined.Engineering
    "approved" -> Icons.Outlined.CheckCircle
    "rejected" -> Icons.Outlined.Cancel
    else -> Icons.Outlined.Info
}

private fun riskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLow
    RiskLevel.MEDIUM -> RiskMedium
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.VERY_HIGH -> RiskVeryHigh
}

private fun levelLabel(level: RiskLevel): String = when (level) {
    RiskLevel.LOW -> "🟢 Низкий"
    RiskLevel.MEDIUM -> "🟡 Средний"
    RiskLevel.HIGH -> "🔴 Высокий"
    RiskLevel.VERY_HIGH -> "⚫ Критический"
}
