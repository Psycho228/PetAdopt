package com.example.petadopt.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.extended.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.data.model.GigaChatRiskAssessment
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.components.RiskAssessmentCard
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.QuestionnaireViewModel
import com.example.petadopt.viewmodel.*

sealed class Question {
    data class Text(
        val title: String,
        val hint: String = "",
        val value: String,
        val onValueChange: (String) -> Unit,
        val singleLine: Boolean = true,
        val icon: ImageVector? = null,
        val keyboardType: KeyboardType = KeyboardType.Text,
        val isPhoneNumber: Boolean = false
    ) : Question()

    data class Dropdown(
        val title: String,
        val options: List<String>,
        val value: String,
        val onValueChange: (String) -> Unit,
        val icon: ImageVector? = null
    ) : Question()

    data class CheckboxGroup(
        val title: String,
        val options: List<String>,
        val selectedValues: List<String>,
        val onValueChange: (List<String>) -> Unit,
        val icon: ImageVector? = null
    ) : Question()

    data class YesNo(
        val title: String,
        val value: String,
        val onValueChange: (String) -> Unit,
        val icon: ImageVector? = null
    ) : Question()
}

data class SectionInfo(
    val title: String,
    val icon: ImageVector
)

/**
 * Форматирует номер телефона в шаблоне +7(***)***-**-**
 * @param digits только цифры номера (без +7 и других символов)
 * @return отформатированная строка
 */
fun formatPhoneNumber(digits: String): String {
    // Оставляем только цифры, максимум 11 цифр (7 + 10)
    val cleaned = digits.filter { it.isDigit() }.take(11)
    
    if (cleaned.isEmpty()) return ""
    
    val result = StringBuilder("+7")
    
    if (cleaned.length > 1) {
        // Берём цифры после первой (которая должна быть 7 или 8)
        val rest = cleaned.drop(1)
        result.append("(")
        result.append(rest.take(3))
        if (rest.length >= 3) {
            result.append(")-")
            result.append(rest.take(6).drop(3))
            if (rest.length >= 6) {
                result.append("-")
                result.append(rest.take(8).drop(6))
                if (rest.length >= 8) {
                    result.append("-")
                    result.append(rest.drop(8))
                }
            }
        }
    }
    
    return result.toString()
}

/**
 * Извлекает только цифры из отформатированного номера телефона
 */
fun extractPhoneDigits(formatted: String): String {
    return formatted.filter { it.isDigit() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    onFinish: (Boolean) -> Unit,
    viewModel: QuestionnaireViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var step by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Скроллим к верху при изменении шага
    LaunchedEffect(step) {
        scrollState.scrollTo(0)
    }
    var showConfirmation by remember { mutableStateOf(false) }
    var showRiskAssessment by remember { mutableStateOf(false) }
    var riskAssessmentResult by remember { mutableStateOf<GigaChatRiskAssessment?>(null) }
    var isLoadingRisk by remember { mutableStateOf(false) }
    var currentErrors by remember { mutableStateOf<List<ValidationError>>(emptyList()) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAnswers()
    }

    // Создаём вопросы напрямую — они будут обновляться при изменении state
    val sections = listOf(
        SectionInfo("Основная информация", Icons.Default.Person) to listOf(
            Question.Text("Как вас зовут?", "", state.name, viewModel::onNameChange, icon = Icons.Default.Person),
            Question.Text("Сколько вам лет?", "", state.age, viewModel::onAgeChange, icon = Icons.Default.CalendarToday),
            Question.Dropdown("В каком городе вы живёте?",
                listOf("Москва", "Санкт-Петербург", "Казань", "Новосибирск", "Екатеринбург", "Другой"),
                state.city, viewModel::onCityChange, icon = Icons.Default.LocationOn),
            Question.Text("Чем вы занимаетесь?", "", state.occupation, viewModel::onOccupationChange, icon = Icons.Default.Work),
            Question.Text("Ваш номер телефона", "", state.phone, viewModel::onPhoneChange, singleLine = false, icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone, isPhoneNumber = true),
            Question.Text("Ваш Email", "", state.email, viewModel::onEmailChange, singleLine = false, icon = Icons.Default.Email)
        ),
        SectionInfo("Жилищные условия", Icons.Default.Home) to listOf(
            Question.Dropdown("Где вы живёте?",
                listOf("Квартира", "Частный дом", "Съёмное жильё", "Другое"),
                state.housingType, viewModel::onHousingTypeChange, icon = Icons.Default.Home),
            Question.YesNo("Разрешены ли животные в вашем жилье?",
                state.petsAllowed, viewModel::onPetsAllowedChange, icon = Icons.Default.CheckCircle),
            Question.Dropdown("С кем вы живёте?",
                listOf("Один", "Семья", "Друзья / соседи", "Другое"),
                state.livingWith, viewModel::onLivingWithChange, icon = Icons.Default.Group),
            Question.YesNo("Все ли члены семьи согласны с появлением питомца?",
                state.familyAgreement, viewModel::onFamilyAgreementChange, icon = Icons.Default.Person),
            Question.YesNo("Есть ли у вас дети?",
                state.hasChildren, viewModel::onHasChildrenChange, icon = Icons.Default.Person),
            Question.Text("Если да — какого возраста?", "", state.childrenAge, viewModel::onChildrenAgeChange, icon = Icons.Default.Person),
            Question.YesNo("Есть ли у вас другие животные?",
                state.hasOtherAnimals, viewModel::onHasOtherAnimalsChange, icon = Icons.Default.Pets),
            Question.Text("Если да — то какие?", "", state.otherAnimals, viewModel::onOtherAnimalsChange, icon = Icons.Default.Pets),
            Question.Text("Сколько часов в день питомец будет оставаться один?", "", state.hoursAlone, viewModel::onHoursAloneChange, icon = Icons.Default.AccessTime),
            Question.Text("Кто будет ухаживать за питомцем во время вашего отсутствия?", "", state.caretaker, viewModel::onCaretakerChange, icon = Icons.Default.PersonAdd)
        ),
        SectionInfo("Опыт с животными", Icons.Default.Pets) to listOf(
            Question.YesNo("Были ли у вас раньше питомцы?",
                state.hadPetsBefore, viewModel::onHadPetsBeforeChange, icon = Icons.Default.Star),
            Question.Text("Что с ними сейчас?", "", state.petsNow, viewModel::onPetsNowChange, icon = Icons.Default.Info),
            Question.YesNo("Есть ли у вас опыт ухода за собаками?",
                state.experienceDogs, viewModel::onExperienceDogsChange, icon = Icons.Default.Pets),
            Question.YesNo("Есть ли у вас опыт ухода за кошками?",
                state.experienceCats, viewModel::onExperienceCatsChange, icon = Icons.Default.Pets),
            Question.YesNo("Есть ли у вас опыт ухода за животными с особенностями?",
                state.experienceSpecialNeeds, viewModel::onExperienceSpecialNeedsChange, icon = Icons.Default.Accessibility),
            Question.Text("Почему вы решили взять питомца именно сейчас?", "", state.reasonNow, viewModel::onReasonNowChange, icon = Icons.Default.Lightbulb)
        ),
        SectionInfo("Ответственность", Icons.Default.Favorite) to listOf(
            Question.CheckboxGroup("Понимаете ли вы, что питомцу потребуется:",
                listOf("Время", "Внимание", "Обучение", "Ветеринарная помощь"),
                state.understandsNeeds, viewModel::onUnderstandsNeedsChange, icon = Icons.Default.Notifications),
            Question.CheckboxGroup("Готовы ли вы к регулярным расходам на:",
                listOf("Корм", "Ветеринара", "Лекарства", "Прививки", "Груминг"),
                state.readyForExpenses, viewModel::onReadyForExpensesChange, icon = Icons.Default.AttachMoney),
            Question.Text("Что вы будете делать, если питомец испортит мебель?", "", state.furnitureDamage, viewModel::onFurnitureDamageChange, icon = Icons.Default.Warning),
            Question.Text("Что вы будете делать, если питомец будет шуметь?", "", state.noiseBehavior, viewModel::onNoiseBehaviorChange, icon = Icons.Default.NotificationsActive),
            Question.Text("Что вы будете делать, если питомец окажется пугливым?", "", state.timidPet, viewModel::onTimidPetChange, icon = Icons.Default.SentimentDissatisfied),
            Question.Text("Что вы будете делать, если питомец долго адаптируется?", "", state.adaptation, viewModel::onAdaptationChange, icon = Icons.Default.AccessTime),
            Question.YesNo("Готовы ли вы заниматься воспитанием и адаптацией питомца?",
                state.willingToTrain, viewModel::onWillingToTrainChange, icon = Icons.Default.School),
            Question.Text("Что вы будете делать при изменении жизненных обстоятельств?", "", state.lifeChanges, viewModel::onLifeChangesChange, icon = Icons.Default.TrendingUp),
            Question.Text("Есть ли что-то, что может помешать заботе о питомце в ближайший год?", "", state.obstacles, viewModel::onObstaclesChange, icon = Icons.Default.Block)
        ),
        SectionInfo("Безопасность", Icons.Default.Lock) to listOf(
            Question.CheckboxGroup("Установлены ли у вас:",
                listOf("Сетки на окнах", "Безопасные балконы", "Ограждения (для дома)"),
                state.safetyMeasures, viewModel::onSafetyMeasuresChange, icon = Icons.Default.Lock),
            Question.CheckboxGroup("Готовы ли вы:",
                listOf("Стерилизовать питомца (если нужно)", "Соблюдать рекомендации приюта", "Использовать адресник и поводок"),
                state.willingTo, viewModel::onWillingToChange, icon = Icons.Default.ConfirmationNumber),
            Question.YesNo("Готовы ли вы поддерживать связь после пристройства?",
                state.maintainContact, viewModel::onMaintainContactChange, icon = Icons.Default.Email)
        ),
        SectionInfo("Эмоциональная часть", Icons.Default.Favorite) to listOf(
            Question.Text("Что для вас значит \"ответственный хозяин\"?", "", state.responsibleOwner, viewModel::onResponsibleOwnerChange, icon = Icons.Default.Favorite),
            Question.Text("Как вы представляете жизнь с питомцем?", "", state.lifeWithPet, viewModel::onLifeWithPetChange, icon = Icons.Default.Favorite),
            Question.Text("Почему, по вашему мнению, именно вы станете хорошим хозяином?", "", state.whyGoodOwner, viewModel::onWhyGoodOwnerChange, icon = Icons.Default.Star)
        ),
        SectionInfo("Желаемые питомцы", Icons.Default.Pets) to listOf(
            Question.CheckboxGroup("Каких питомцев вы хотите взять?",
                listOf("Собака", "Кошка", "Птица", "Грызуны", "Рыбы", "Рептилии", "Другое"),
                state.q7_desired_pets, viewModel::onDesiredPetsChange, icon = Icons.Default.Pets)
        )
    )

    val currentSectionInfo = sections[step].first
    val currentQuestions = sections[step].second

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Заполните обязательные поля", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Пожалуйста, заполните следующие поля:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    currentErrors.forEach { error ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(error.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Понятно",
                    onClick = { showErrorDialog = false }
                )
            }
        )
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Завершение опросника") },
            text = {
                Column {
                    Text("Сохранить ответы и получить оценку рисков от GigaChat?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Это займёт несколько секунд. Оценка поможет приюту принять решение.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Сохранить и оценить",
                    onClick = {
                        showConfirmation = false
                        isLoadingRisk = true
                        viewModel.saveWithRiskAssessment(
                            onSuccess = { assessment ->
                                isLoadingRisk = false
                                riskAssessmentResult = assessment
                                showRiskAssessment = true
                            },
                            onRiskAssessed = { result ->
                                if (result.isFailure) {
                                    isLoadingRisk = false
                                }
                            }
                        )
                    }
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmation = false
                        viewModel.saveAndFinish { onFinish(false) }
                    }
                ) {
                    Text("Только сохранить")
                }
            }
        )
    }

    if (showRiskAssessment && riskAssessmentResult != null) {
        AlertDialog(
            onDismissRequest = { 
                showRiskAssessment = false 
                riskAssessmentResult = null
            },
            title = { Text("Результат оценки") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    riskAssessmentResult?.let { assessment ->
                        RiskAssessmentCard(
                            assessment = assessment,
                            modifier = Modifier.widthIn(max = 400.dp)
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Понятно",
                    onClick = {
                        showRiskAssessment = false
                        riskAssessmentResult = null
                        onFinish(true)
                    }
                )
            }
        )
    }

    // Индикатор загрузки оценки рисков
    if (isLoadingRisk) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    "Оценка рисков",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Анализирую ответы...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            currentSectionInfo.icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(currentSectionInfo.title, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Закрыть клавиатуру и снять фокус при клике по фону
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Улучшенный прогресс-бар с маркерами секций
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sections.forEachIndexed { index, (sectionInfo, _) ->
                        val isCompleted = index < step
                        val isCurrent = index == step
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        ) {
                            AnimatedVisibility(
                                visible = isCompleted || isCurrent,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isCurrent) 24.dp else 16.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            when {
                                                isCurrent -> Primary
                                                isCompleted -> Primary.copy(alpha = 0.5f)
                                                else -> TextSecondary.copy(alpha = 0.3f)
                                            }
                                        )
                                )
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isCurrent -> Primary
                                    isCompleted -> Primary
                                    else -> TextSecondary.copy(alpha = 0.5f)
                                },
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        
                        if (index < sections.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .padding(horizontal = 4.dp)
                                    .background(
                                        when {
                                            isCompleted -> Primary.copy(alpha = 0.5f)
                                            else -> TextSecondary.copy(alpha = 0.2f)
                                        }
                                    )
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Шаг ${step + 1} из ${sections.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${((step + 1) * 100 / sections.size)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                }
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    currentQuestions.forEachIndexed { index, question ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { 20 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(animationSpec = tween(300 + index * 50))
                        ) {
                            QuestionView(
                                question = question,
                                isLastQuestion = index == currentQuestions.size - 1
                            )
                        }
                        
                        if (index < currentQuestions.size - 1) {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                }
            ) { currentStep ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondary
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Назад")
                        }
                    }

                    PrimaryButton(
                        text = if (currentStep == sections.lastIndex) "Завершить" else "Далее",
                        onClick = {
                            // Валидация текущего раздела
                            val validation = state.validateSection(currentStep)
                            if (!validation.isValid) {
                                currentErrors = validation.errors
                                showErrorDialog = true
                                return@PrimaryButton
                            }
                            
                            if (currentStep == sections.lastIndex) {
                                showConfirmation = true
                            } else {
                                step++
                            }
                        },
                        modifier = Modifier.weight(if (currentStep > 0) 1f else 1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionView(
    question: Question,
    isLastQuestion: Boolean
) {
    when (question) {
        is Question.Text -> {
            var isError by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("") }
            // Для поля телефона храним TextFieldValue с позицией курсора
            val phoneState = remember { mutableStateOf(TextFieldValue()) }
            
            // Синхронизация phoneState с question.value при загрузке данных
            LaunchedEffect(question.value) {
                if (question.isPhoneNumber && question.value.isNotEmpty()) {
                    phoneState.value = TextFieldValue(
                        text = question.value,
                        selection = TextRange(question.value.length)
                    )
                }
            }
            
            QuestionCard(
                title = question.title,
                icon = question.icon
            ) {
                if (question.isPhoneNumber) {
                    // Особая обработка для телефона с сохранением курсора
                    OutlinedTextField(
                        value = phoneState.value,
                        onValueChange = { newTextFieldValue ->
                            val newText = newTextFieldValue.text
                            val oldText = phoneState.value.text
                            val oldCursorPos = phoneState.value.selection.start
                            
                            // Извлекаем цифры из нового и старого текста
                            val newDigits = newText.filter { it.isDigit() }
                            val oldDigits = oldText.filter { it.isDigit() }
                            
                            // Если текст очистили полностью
                            if (newDigits.isEmpty()) {
                                phoneState.value = TextFieldValue(
                                    text = "",
                                    selection = TextRange(0)
                                )
                                question.onValueChange("")
                                return@OutlinedTextField
                            }
                            
                            // Определяем, было ли удаление или добавление
                            val isDeletion = newDigits.length < oldDigits.length
                            
                            if (isDeletion) {
                                // При удалении просто форматируем новые цифры
                                val formatted = formatPhoneNumber(newDigits)
                                // Вычисляем новую позицию курсора
                                val digitsBeforeCursor = oldDigits.take(oldCursorPos - 2).count { it.isDigit() }
                                val newCursorPos = 2 + digitsBeforeCursor.coerceAtMost(newDigits.length)
                                
                                phoneState.value = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(newCursorPos.coerceAtMost(formatted.length))
                                )
                                question.onValueChange(formatted)
                            } else {
                                // При добавлении - добавляем только новые цифры
                                val addedDigitCount = newDigits.length - oldDigits.length
                                if (addedDigitCount > 0 && newDigits.length <= 11) {
                                    val formatted = formatPhoneNumber(newDigits)
                                    // Курсор ставим в конец
                                    phoneState.value = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length)
                                    )
                                    question.onValueChange(formatted)
                                } else {
                                    // Если не добавлялись цифры (например, ввод формата), просто обновляем позицию
                                    phoneState.value = newTextFieldValue
                                }
                            }
                        },
                        placeholder = { Text("7 (___) ___-__-__", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = question.singleLine,
                        minLines = if (question.singleLine) 1 else 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Phone,
                            imeAction = if (isLastQuestion) ImeAction.Done else ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = isError,
                        supportingText = if (isError) {
                            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                } else {
                    OutlinedTextField(
                        value = question.value,
                        onValueChange = { newValue ->
                            // Ограничение ввода для числовых полей
                            val filteredValue = when (question.title) {
                                "Сколько вам лет?" -> newValue.filter { it.isDigit() }
                                "Сколько часов в день питомец будет оставаться один?" -> newValue.filter { it.isDigit() }
                                "Если да — какого возраста?" -> newValue.filter { it.isDigit() || it == ',' || it == ' ' }
                                else -> newValue
                            }
                            question.onValueChange(filteredValue)
                        },
                        placeholder = { Text(question.hint, color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = question.singleLine,
                        minLines = if (question.singleLine) 1 else 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = when (question.title) {
                                "Сколько вам лет?" -> KeyboardType.Number
                                "Сколько часов в день питомец будет оставаться один?" -> KeyboardType.Number
                                "Если да — какого возраста?" -> KeyboardType.Number
                                else -> question.keyboardType
                            },
                            imeAction = if (isLastQuestion) ImeAction.Done else ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = isError,
                        supportingText = if (isError) {
                            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                }
            }
            
            // Проверка валидности при изменении значения
            LaunchedEffect(question.value) {
                when (question.title) {
                    "Сколько вам лет?" -> {
                        val age = question.value.toIntOrNull()
                        isError = question.value.isNotEmpty() && (age == null || age < 18 || age > 120)
                        errorMessage = if (isError) "Введите возраст от 18 до 120" else ""
                    }
                    "Сколько часов в день питомец будет оставаться один?" -> {
                        val hours = question.value.toIntOrNull()
                        isError = question.value.isNotEmpty() && (hours == null || hours < 0 || hours > 24)
                        errorMessage = if (isError) "Введите часы от 0 до 24" else ""
                    }
                    "Если да — какого возраста?" -> {
                        // Проверка на корректный ввод возрастов
                        isError = question.value.any { !it.isDigit() && it != ',' && it != ' ' }
                        errorMessage = if (isError) "Только цифры, запятые и пробелы" else ""
                    }
                    else -> {
                        isError = false
                        errorMessage = ""
                    }
                }
            }
        }
        is Question.Dropdown -> {
            var isExpanded by remember { mutableStateOf(false) }
            
            QuestionCard(
                title = question.title,
                icon = question.icon
            ) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = question.value.ifEmpty { "Выберите вариант" },
                        onValueChange = { },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isExpanded
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        question.options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    question.onValueChange(option)
                                    isExpanded = false
                                },
                                leadingIcon = {
                                    if (question.value == option) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Primary)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        is Question.CheckboxGroup -> {
            QuestionCard(
                title = question.title,
                icon = question.icon
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    question.options.forEach { option ->
                        val isSelected = question.selectedValues.contains(option)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        question.onValueChange(question.selectedValues - option)
                                    } else {
                                        question.onValueChange(question.selectedValues + option)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) {
                                        question.onValueChange(question.selectedValues - option)
                                    } else {
                                        question.onValueChange(question.selectedValues + option)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Primary,
                                    uncheckedColor = TextSecondary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else TextSecondary
                            )
                        }
                    }
                }
            }
        }
        is Question.YesNo -> {
            QuestionCard(
                title = question.title,
                icon = question.icon
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isSelectedYes = question.value == "Да"
                    val isSelectedNo = question.value == "Нет"
                    
                    val buttonColorsYes = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSelectedYes) Primary else TextSecondary,
                        containerColor = if (isSelectedYes) Primary.copy(alpha = 0.08f) else Color.Transparent
                    )
                    
                    OutlinedButton(
                        onClick = { question.onValueChange("Да") },
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelectedYes) Modifier.border(
                                    width = 2.dp,
                                    color = Primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) else Modifier
                            ),
                        colors = buttonColorsYes,
                        shape = RoundedCornerShape(12.dp),
                        border = null // Убираем стандартную рамку
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Да", fontWeight = if (isSelectedYes) FontWeight.Bold else FontWeight.Normal)
                    }
                    
                    val buttonColorsNo = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSelectedNo) Primary else TextSecondary,
                        containerColor = if (isSelectedNo) Primary.copy(alpha = 0.08f) else Color.Transparent
                    )
                    
                    OutlinedButton(
                        onClick = { question.onValueChange("Нет") },
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelectedNo) Modifier.border(
                                    width = 2.dp,
                                    color = Primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) else Modifier
                            ),
                        colors = buttonColorsNo,
                        shape = RoundedCornerShape(12.dp),
                        border = null // Убираем стандартную рамку
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Нет", fontWeight = if (isSelectedNo) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    title: String,
    icon: ImageVector?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
