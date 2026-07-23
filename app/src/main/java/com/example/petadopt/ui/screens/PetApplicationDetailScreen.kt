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
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import kotlinx.serialization.json.Json

// в”Ђв”Ђв”Ђ Р¦РІРµС‚Р° РґР»СЏ СѓСЂРѕРІРЅРµР№ СЂРёСЃРєР° в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
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
                        Text("Р—Р°СЏРІРєР°", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            contentDescription = "РќР°Р·Р°Рґ",
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // РљРЅРѕРїРєР° С‡Р°С‚Р°
                    OutlinedButton(
                        onClick = { 
                            navController.navigate("chat/${application.id}") 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Р§Р°С‚")
                    }
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("РћС‚РєР»РѕРЅРёС‚СЊ")
                    }
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RiskLow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("РџРѕРґС‚РІРµСЂРґРёС‚СЊ")
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
                // в•ђв•ђв•ђ РЎРўРђРўРЈРЎ Р—РђРЇР’РљР в•ђв•ђв•ђ
                StatusBanner(status = application.status)

                Spacer(Modifier.height(12.dp))

                // в•ђв•ђв•ђ Р”Р•РўРђР›Р¬РќРђРЇ РћР¦Р•РќРљРђ Р РРЎРљРћР’ (GigaChat) вЂ” РіР»Р°РІРЅС‹Р№ Р±Р»РѕРє в•ђв•ђв•ђ
                if (riskAssessment != null) {
                    DetailedRiskAssessment(
                        risk = riskAssessment!!,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                } else {
                    // РџР»РµР№СЃС…РѕР»РґРµСЂ РµСЃР»Рё РѕС†РµРЅРєРё РЅРµС‚
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
                                "РћС†РµРЅРєР° СЂРёСЃРєРѕРІ РЅРµ РїСЂРѕРІРѕРґРёР»Р°СЃСЊ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // в•ђв•ђв•ђ РљРћРќРўРђРљРўРќРђРЇ РРќР¤РћР РњРђР¦РРЇ в•ђв•ђв•ђ
                SectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "РљРѕРЅС‚Р°РєС‚РЅР°СЏ РёРЅС„РѕСЂРјР°С†РёСЏ"
                )
                ContactRow(Icons.Outlined.Phone, "РўРµР»РµС„РѕРЅ", questionnaire!!.q1_phone.ifBlank { "РќРµ СѓРєР°Р·Р°РЅ" })
                ContactRow(Icons.Outlined.Email, "Email", questionnaire!!.q1_email.ifBlank { "РќРµ СѓРєР°Р·Р°РЅ" })
                ContactRow(Icons.Outlined.LocationOn, "Р“РѕСЂРѕРґ", questionnaire!!.q1_city.ifBlank { "РќРµ СѓРєР°Р·Р°РЅ" })
                ContactRow(Icons.Outlined.Cake, "Р’РѕР·СЂР°СЃС‚", "${questionnaire!!.q1_age ?: "РќРµ СѓРєР°Р·Р°РЅ"} Р»РµС‚")
                ContactRow(Icons.Outlined.BusinessCenter, "Р РѕРґ Р·Р°РЅСЏС‚РёР№", questionnaire!!.q1_occupation.ifBlank { "РќРµ СѓРєР°Р·Р°РЅ" })

                Spacer(Modifier.height(8.dp))

                // в•ђв•ђв•ђ РЎРћРћР‘Р©Р•РќРР• РћРў Р—РђРЇР’РРўР•Р›РЇ в•ђв•ђв•ђ
                if (application.message.isNotBlank()) {
                    SectionHeader(
                        icon = Icons.Outlined.Message,
                        title = "РЎРѕРѕР±С‰РµРЅРёРµ РѕС‚ Р·Р°СЏРІРёС‚РµР»СЏ"
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

                // в•ђв•ђв•ђ Р’Р Р•РњРЇ РљРћРќРўРђРљРўРђ в•ђв•ђв•ђ
                if (application.contact_time?.isNotBlank() == true || application.contact_days?.isNotBlank() == true) {
                    SectionHeader(
                        icon = Icons.Outlined.Schedule,
                        title = "РџСЂРµРґРїРѕС‡С‚РёС‚РµР»СЊРЅРѕРµ РІСЂРµРјСЏ РєРѕРЅС‚Р°РєС‚Р°"
                    )
                    if (application.contact_days?.isNotBlank() == true) {
                        ContactRow(Icons.Outlined.CalendarToday, "Р”РЅРё РЅРµРґРµР»Рё", application.contact_days ?: "")
                    }
                    if (application.contact_time?.isNotBlank() == true) {
                        ContactRow(Icons.Outlined.AccessTime, "Р’СЂРµРјСЏ РґР»СЏ СЃРІСЏР·Рё", application.contact_time ?: "")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // в•ђв•ђв•ђ РЎР’РћР РђР§РР’РђР•РњР«Р• Р РђР—Р”Р•Р›Р« РћРџР РћРЎРќРРљРђ в•ђв•ђв•ђ
                var expandedHousing by remember { mutableStateOf(false) }
                var expandedExperience by remember { mutableStateOf(false) }
                var expandedPlans by remember { mutableStateOf(false) }
                var expandedResponsibility by remember { mutableStateOf(false) }
                var expandedSafety by remember { mutableStateOf(false) }
                var expandedEmotional by remember { mutableStateOf(false) }
                var expandedDesired by remember { mutableStateOf(false) }

                // Р–РёР»РёС‰РЅС‹Рµ СѓСЃР»РѕРІРёСЏ
                QuestionnaireExpandable(
                    title = "Р–РёР»РёС‰РЅС‹Рµ СѓСЃР»РѕРІРёСЏ",
                    expanded = expandedHousing,
                    onToggle = { expandedHousing = !expandedHousing }
                ) {
                    ContactRow(Icons.Outlined.Home, "РўРёРї Р¶РёР»СЊСЏ", questionnaire!!.q2_housing_type.ifBlank { "РќРµ СѓРєР°Р·Р°РЅРѕ" })
                    ContactRow(Icons.Outlined.Groups, "РЎ РєРµРј Р¶РёРІС‘С‚", questionnaire!!.q2_living_with.joinToString(", ").ifEmpty { "РќРµ СѓРєР°Р·Р°РЅРѕ" })
                    ContactRow(Icons.Outlined.ChildCare, "Р”РµС‚Рё",
                        if (questionnaire!!.q2_has_children == true) "Р”Р° (${questionnaire!!.q2_children_ages})" else "РќРµС‚")
                    ContactRow(Icons.Outlined.Pets, "Р”СЂСѓРіРёРµ Р¶РёРІРѕС‚РЅС‹Рµ",
                        if (questionnaire!!.q2_has_other_pets == true) "Р”Р° (${questionnaire!!.q2_other_pets_types.joinToString(", ")})" else "РќРµС‚")
                    ContactRow(Icons.Outlined.Timer, "Р§Р°СЃРѕРІ РІ РѕРґРёРЅРѕС‡РµСЃС‚РІРµ", "${questionnaire!!.q2_hours_alone ?: 0} С‡.")
                }

                // РћРїС‹С‚
                QuestionnaireExpandable(
                    title = "РћРїС‹С‚ СЃ Р¶РёРІРѕС‚РЅС‹РјРё",
                    expanded = expandedExperience,
                    onToggle = { expandedExperience = !expandedExperience }
                ) {
                    ContactRow(Icons.Outlined.CheckCircle, "РћРїС‹С‚ СЃ СЃРѕР±Р°РєР°РјРё", yn(questionnaire!!.q3_dog_experience))
                    ContactRow(Icons.Outlined.CheckCircle, "РћРїС‹С‚ СЃ РєРѕС€РєР°РјРё", yn(questionnaire!!.q3_cat_experience))
                    if (questionnaire!!.q3_why_now.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        TextBlock("РџРѕС‡РµРјСѓ СЃРµР№С‡Р°СЃ?", questionnaire!!.q3_why_now)
                    }
                }

                // РџР»Р°РЅС‹
                QuestionnaireExpandable(
                    title = "РџР»Р°РЅ РІ СЃР»РѕР¶РЅС‹С… СЃРёС‚СѓР°С†РёСЏС…",
                    expanded = expandedPlans,
                    onToggle = { expandedPlans = !expandedPlans }
                ) {
                    if (questionnaire!!.q4_furniture_damage_plan.isNotBlank())
                        TextBlock("РџРѕСЂС‡Р° РјРµР±РµР»Рё", questionnaire!!.q4_furniture_damage_plan)
                    if (questionnaire!!.q4_noise_plan.isNotBlank())
                        TextBlock("РЁСѓРј", questionnaire!!.q4_noise_plan)
                    if (questionnaire!!.q4_shy_pet_plan.isNotBlank())
                        TextBlock("РџСѓРіР»РёРІС‹Р№ РїРёС‚РѕРјРµС†", questionnaire!!.q4_shy_pet_plan)
                    if (questionnaire!!.q4_long_adaptation_plan.isNotBlank())
                        TextBlock("Р”РѕР»РіР°СЏ Р°РґР°РїС‚Р°С†РёСЏ", questionnaire!!.q4_long_adaptation_plan)
                }

                // РћС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚СЊ
                QuestionnaireExpandable(
                    title = "РћС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚СЊ Рё РіРѕС‚РѕРІРЅРѕСЃС‚СЊ",
                    expanded = expandedResponsibility,
                    onToggle = { expandedResponsibility = !expandedResponsibility }
                ) {
                    val understand = mutableListOf<String>()
                    if (questionnaire!!.q4_understand_time == true) understand.add("Р’СЂРµРјСЏ")
                    if (questionnaire!!.q4_understand_attention == true) understand.add("Р’РЅРёРјР°РЅРёРµ")
                    if (questionnaire!!.q4_understand_training == true) understand.add("РћР±СѓС‡РµРЅРёРµ")
                    if (questionnaire!!.q4_understand_vet_care == true) understand.add("Р’РµС‚РїРѕРјРѕС‰СЊ")
                    ContactRow(Icons.Outlined.Lightbulb, "РџРѕРЅРёРјР°РЅРёРµ С‚СЂРµР±РѕРІР°РЅРёР№", understand.joinToString(", ").ifEmpty { "вЂ”" })

                    val expenses = mutableListOf<String>()
                    if (questionnaire!!.q4_ready_food == true) expenses.add("РљРѕСЂРј")
                    if (questionnaire!!.q4_ready_vet == true) expenses.add("Р’РµС‚РµСЂРёРЅР°СЂ")
                    if (questionnaire!!.q4_ready_medication == true) expenses.add("Р›РµРєР°СЂСЃС‚РІР°")
                    if (questionnaire!!.q4_ready_vaccinations == true) expenses.add("РџСЂРёРІРёРІРєРё")
                    if (questionnaire!!.q4_ready_grooming == true) expenses.add("Р“СЂСѓРјРёРЅРі")
                    ContactRow(Icons.Outlined.AttachMoney, "Р“РѕС‚РѕРІРЅРѕСЃС‚СЊ Рє СЂР°СЃС…РѕРґР°Рј", expenses.joinToString(", ").ifEmpty { "вЂ”" })

                    ContactRow(Icons.Outlined.School, "Р“РѕС‚РѕРІРЅРѕСЃС‚СЊ Рє РІРѕСЃРїРёС‚Р°РЅРёСЋ", yn(questionnaire!!.q4_ready_education))

                    if (questionnaire!!.q4_life_changes_plan.isNotBlank())
                        TextBlock("РџСЂРё РёР·РјРµРЅРµРЅРёРё РѕР±СЃС‚РѕСЏС‚РµР»СЊСЃС‚РІ", questionnaire!!.q4_life_changes_plan)
                    if (questionnaire!!.q4_obstacles_next_year.isNotBlank())
                        TextBlock("РџСЂРµРїСЏС‚СЃС‚РІРёСЏ РІ Р±Р»РёР¶Р°Р№С€РёР№ РіРѕРґ", questionnaire!!.q4_obstacles_next_year)
                }

                // Р‘РµР·РѕРїР°СЃРЅРѕСЃС‚СЊ
                QuestionnaireExpandable(
                    title = "Р‘РµР·РѕРїР°СЃРЅРѕСЃС‚СЊ",
                    expanded = expandedSafety,
                    onToggle = { expandedSafety = !expandedSafety }
                ) {
                    ContactRow(Icons.Outlined.Security, "РњРµСЂС‹ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё",
                        questionnaire!!.q5_safety_measures.joinToString(", ").ifEmpty { "РќРµ СѓРєР°Р·Р°РЅС‹" })
                    ContactRow(Icons.Outlined.VolunteerActivism, "Р“РѕС‚РѕРІРЅРѕСЃС‚СЊ Рє СЃС‚РµСЂРёР»РёР·Р°С†РёРё", yn(questionnaire!!.q5_ready_neuter))
                    ContactRow(Icons.Outlined.Recommend, "РЎР»РµРґРѕРІР°РЅРёРµ СЂРµРєРѕРјРµРЅРґР°С†РёСЏРј", yn(questionnaire!!.q5_ready_recommendations))
                    ContactRow(Icons.Outlined.Badge, "РЈСЃС‚Р°РЅРѕРІРєР° Р°РґСЂРµСЃРЅРёРєР°", yn(questionnaire!!.q5_ready_tracker))
                    ContactRow(Icons.Outlined.ConnectWithoutContact, "РџРѕРґРґРµСЂР¶Р°РЅРёРµ СЃРІСЏР·Рё", yn(questionnaire!!.q5_ready_keep_contact))
                }

                // Р­РјРѕС†РёРѕРЅР°Р»СЊРЅР°СЏ
                QuestionnaireExpandable(
                    title = "Р­РјРѕС†РёРѕРЅР°Р»СЊРЅР°СЏ С‡Р°СЃС‚СЊ",
                    expanded = expandedEmotional,
                    onToggle = { expandedEmotional = !expandedEmotional }
                ) {
                    if (questionnaire!!.q6_responsible_owner_meaning.isNotBlank())
                        TextBlock("РћС‚РІРµС‚СЃС‚РІРµРЅРЅС‹Р№ С…РѕР·СЏРёРЅ вЂ” СЌС‚Рѕ?", questionnaire!!.q6_responsible_owner_meaning)
                    if (questionnaire!!.q6_life_with_pet_vision.isNotBlank())
                        TextBlock("Р–РёР·РЅСЊ СЃ РїРёС‚РѕРјС†РµРј", questionnaire!!.q6_life_with_pet_vision)
                    if (questionnaire!!.q6_why_good_owner.isNotBlank())
                        TextBlock("РџРѕС‡РµРјСѓ С…РѕСЂРѕС€РёР№ С…РѕР·СЏРёРЅ?", questionnaire!!.q6_why_good_owner)
                }

                // Р–РµР»Р°РµРјС‹Рµ РІРёРґС‹
                QuestionnaireExpandable(
                    title = "Р–РµР»Р°РµРјС‹Рµ РІРёРґС‹ Р¶РёРІРѕС‚РЅС‹С…",
                    expanded = expandedDesired,
                    onToggle = { expandedDesired = !expandedDesired }
                ) {
                    ContactRow(Icons.Outlined.FavoriteBorder, "РРЅС‚РµСЂРµСЃСѓСЋС‚",
                        questionnaire!!.q7_desired_pets.joinToString(", ").ifEmpty { "РќРµ СѓРєР°Р·Р°РЅС‹" })
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            // РћРїСЂРѕСЃРЅРёРє РЅРµ РЅР°Р№РґРµРЅ
            EmptyQuestionnaire(modifier = Modifier.padding(padding))
        }
    }

    // в”Ђв”Ђ Р”РёР°Р»РѕРіРё РїРѕРґС‚РІРµСЂР¶РґРµРЅРёСЏ в”Ђв”Ђ
    if (showConfirmDialog && application != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = RiskLow, modifier = Modifier.size(40.dp)) },
            title = { Text("РџРѕРґС‚РІРµСЂРґРёС‚СЊ Р·Р°СЏРІРєСѓ?", fontWeight = FontWeight.Bold) },
            text = { Text("Р—Р°СЏРІРєР° РѕС‚ ${application.user_name} Р±СѓРґРµС‚ РѕРґРѕР±СЂРµРЅР°. Р—Р°СЏРІРёС‚РµР»СЊ РїРѕР»СѓС‡РёС‚ СѓРІРµРґРѕРјР»РµРЅРёРµ.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "approved")
                        showConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskLow)
                ) { Text("РџРѕРґС‚РІРµСЂРґРёС‚СЊ") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("РћС‚РјРµРЅР°") } }
        )
    }

    if (showRejectDialog && application != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp)) },
            title = { Text("РћС‚РєР»РѕРЅРёС‚СЊ Р·Р°СЏРІРєСѓ?", fontWeight = FontWeight.Bold) },
            text = { Text("Р—Р°СЏРІРєР° РѕС‚ ${application.user_name} Р±СѓРґРµС‚ РѕС‚РєР»РѕРЅРµРЅР°. Р­С‚Рѕ РґРµР№СЃС‚РІРёРµ РјРѕР¶РЅРѕ РѕС‚РјРµРЅРёС‚СЊ РЅР° СЌРєСЂР°РЅРµ РґРµС‚Р°Р»РµР№.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(application.id, "rejected")
                        showRejectDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("РћС‚РєР»РѕРЅРёС‚СЊ") }
            },
            dismissButton = { TextButton(onClick = { showRejectDialog = false }) { Text("РћС‚РјРµРЅР°") } }
        )
    }
}

// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ
// Р”Р•РўРђР›Р¬РќРђРЇ РћР¦Р•РќРљРђ Р РРЎРљРћР’ (Р“Р›РђР’РќР«Р™ Р‘Р›РћРљ)
// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ

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
            // в”Ђв”Ђ РЁР°РїРєР° СЃ РіСЂР°РґРёРµРЅС‚РѕРј в”Ђв”Ђ
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
                            "РћС†РµРЅРєР° СЂРёСЃРєРѕРІ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "рџ”’ GigaChat AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    // Р‘РµР№РґР¶ СѓСЂРѕРІРЅСЏ СЂРёСЃРєР°
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
                // в”Ђв”Ђ Р‘Р°Р»Р» СЂРёСЃРєР° СЃ РїСЂРѕРіСЂРµСЃСЃ-Р±Р°СЂРѕРј в”Ђв”Ђ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Р‘Р°Р»Р» СЂРёСЃРєР°",
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

                // РљР°СЃС‚РѕРјРЅС‹Р№ РїСЂРѕРіСЂРµСЃСЃ-Р±Р°СЂ
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

                // РњРµС‚РєРё РїРѕРґ РїСЂРѕРіСЂРµСЃСЃ-Р±Р°СЂРѕРј
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("РќРёР·РєРёР№", style = MaterialTheme.typography.labelSmall, color = RiskLow)
                    Text("РЎСЂРµРґРЅРёР№", style = MaterialTheme.typography.labelSmall, color = RiskMedium)
                    Text("Р’С‹СЃРѕРєРёР№", style = MaterialTheme.typography.labelSmall, color = RiskHigh)
                }

                Spacer(Modifier.height(20.dp))

                // в”Ђв”Ђ РС‚РѕРіРѕРІР°СЏ СЂРµРєРѕРјРµРЅРґР°С†РёСЏ в”Ђв”Ђ
                Text(
                    text = "Р’РµСЂРґРёРєС‚",
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
                            text = risk.recommendation.ifBlank {
                                when (riskLevel) {
                                    RiskLevel.LOW -> "Р РµРєРѕРјРµРЅРґСѓРµС‚СЃСЏ РѕРґРѕР±СЂРёС‚СЊ"
                                    RiskLevel.MEDIUM -> "РћРґРѕР±СЂРёС‚СЊ СЃ СѓСЃР»РѕРІРёСЏРјРё"
                                    RiskLevel.HIGH -> "РўСЂРµР±СѓРµС‚СЃСЏ РґРѕРїРѕР»РЅРёС‚РµР»СЊРЅР°СЏ РїСЂРѕРІРµСЂРєР°"
                                    RiskLevel.VERY_HIGH -> "Р РµРєРѕРјРµРЅРґСѓРµС‚СЃСЏ РѕС‚РєР»РѕРЅРёС‚СЊ"
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }

                // в”Ђв”Ђ Р”РµС‚Р°Р»СЊРЅС‹Р№ Р°РЅР°Р»РёР· в”Ђв”Ђ
                if (risk.detailedAnalysis.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Р”РµС‚Р°Р»СЊРЅС‹Р№ Р°РЅР°Р»РёР·",
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

                // в”Ђв”Ђ Р¤Р°РєС‚РѕСЂС‹ СЂРёСЃРєР° в”Ђв”Ђ
                if (riskFactors.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    FactorSection(
                        title = "Р¤Р°РєС‚РѕСЂС‹ СЂРёСЃРєР°",
                        emoji = "вљ пёЏ",
                        items = riskFactors,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // в”Ђв”Ђ РџРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Рµ С„Р°РєС‚РѕСЂС‹ в”Ђв”Ђ
                if (positiveFactors.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    FactorSection(
                        title = "РџРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Рµ С„Р°РєС‚РѕСЂС‹",
                        emoji = "вњ…",
                        items = positiveFactors,
                        color = RiskLow
                    )
                }

                // в”Ђв”Ђ Р РµРєРѕРјРµРЅРґР°С†РёРё в”Ђв”Ђ
                if (recommendations.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Р РµРєРѕРјРµРЅРґР°С†РёРё",
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

// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ
// Р’РЎРџРћРњРћР“РђРўР•Р›Р¬РќР«Р• РљРћРњРџРћРќР•РќРўР«
// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ

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
                    text = "РЎС‚Р°С‚СѓСЃ Р·Р°СЏРІРєРё",
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
            Text("Р—Р°РіСЂСѓР·РєР° РґР°РЅРЅС‹С… Р·Р°СЏРІРёС‚РµР»СЏ...", color = TextSecondary, fontSize = 15.sp)
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
            Text("Р”Р°РЅРЅС‹Рµ Р·Р°СЏРІРёС‚РµР»СЏ РЅРµ РЅР°Р№РґРµРЅС‹", fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text("РџРѕР»СЊР·РѕРІР°С‚РµР»СЊ РЅРµ Р·Р°РїРѕР»РЅРёР» РѕРїСЂРѕСЃРЅРёРє", style = MaterialTheme.typography.bodyMedium, color = TextSecondary.copy(alpha = 0.6f))
        }
    }
}

// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ
// РЈРўРР›РРўР«
// в•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђв•ђ

private fun yn(value: Boolean?): String = if (value == true) "Р”Р°" else "РќРµС‚"

private fun getStatusColor(status: String): Color = when (status) {
    "pending" -> Color(0xFFFF9800)
    "processing" -> Color(0xFF2196F3)
    "approved" -> RiskLow
    "rejected" -> RiskHigh
    else -> TextSecondary
}

private fun getStatusText(status: String): String = when (status) {
    "pending" -> "Р’ РѕР¶РёРґР°РЅРёРё"
    "processing" -> "Р’ СЂР°Р±РѕС‚Рµ"
    "approved" -> "РџСЂРёРЅСЏС‚Р°"
    "rejected" -> "РћС‚РєР»РѕРЅРµРЅР°"
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
    RiskLevel.LOW -> "рџџў РќРёР·РєРёР№"
    RiskLevel.MEDIUM -> "рџџЎ РЎСЂРµРґРЅРёР№"
    RiskLevel.HIGH -> "рџ”ґ Р’С‹СЃРѕРєРёР№"
    RiskLevel.VERY_HIGH -> "вљ« РљСЂРёС‚РёС‡РµСЃРєРёР№"
}
