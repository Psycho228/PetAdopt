package com.example.petadopt.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.example.petadopt.viewmodel.QuestionnaireState
import com.example.petadopt.viewmodel.*
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.QuestionnaireViewModel

sealed class Question {
    data class Text(
        val title: String,
        val hint: String = "",
        val value: String,
        val onValueChange: (String) -> Unit,
        val singleLine: Boolean = true
    ) : Question()

    data class Dropdown(
        val title: String,
        val options: List<String>,
        val value: String,
        val onValueChange: (String) -> Unit
    ) : Question()

    data class YesNo(
        val title: String,
        val value: String,
        val onValueChange: (String) -> Unit
    ) : Question()

    data class CheckboxGroup(
        val title: String,
        val options: List<String>,
        val selected: List<String>,
        val onValueChange: (List<String>) -> Unit
    ) : Question()

    data class TextArea(
        val title: String,
        val hint: String = "",
        val value: String,
        val onValueChange: (String) -> Unit,
        val minLines: Int = 3
    ) : Question()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    onFinish: () -> Unit,
    viewModel: QuestionnaireViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var step by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current
    var showConfirmation by remember { mutableStateOf(false) }

    // Секция 1: Основная информация (5 вопросов)
    val section1Questions = listOf(
        Question.Text("Как вас зовут?", "Ваше имя", state.name, viewModel::onNameChange),
        Question.Text("Сколько вам лет?", "Возраст", state.age, viewModel::onAgeChange),
        Question.Dropdown("В каком городе вы живёте?",
            listOf("Москва", "Санкт-Петербург", "Казань", "Новосибирск", "Екатеринбург", "Другой"),
            state.city, viewModel::onCityChange),
        Question.Text("Чем вы занимаетесь?", "Работа / учёба", state.occupation, viewModel::onOccupationChange),
        Question.Text("Как с вами лучше связаться?", "Телефон / Email", state.contactMethod, viewModel::onContactMethodChange)
    )

    // Секция 2: Жилищные условия (10 вопросов)
    val section2Questions = listOf(
        Question.Dropdown("Где вы живёте?",
            listOf("Квартира", "Частный дом", "Съёмное жильё", "Другое"),
            state.housingType, viewModel::onHousingTypeChange),
        Question.YesNo("Разрешены ли животные в вашем жилье?",
            state.petsAllowed, viewModel::onPetsAllowedChange),
        Question.Dropdown("С кем вы живёте?",
            listOf("Один", "Семья", "Друзья / соседи", "Другое"),
            state.livingWith, viewModel::onLivingWithChange),
        Question.YesNo("Все ли члены семьи согласны с появлением питомца?",
            state.familyAgreement, viewModel::onFamilyAgreementChange),
        Question.YesNo("Есть ли у вас дети?",
            state.hasChildren, viewModel::onHasChildrenChange),
        Question.Text("Если да — какого возраста?", "Возраст детей", state.childrenAge, viewModel::onChildrenAgeChange),
        Question.YesNo("Есть ли у вас другие животные?",
            state.hasOtherAnimals, viewModel::onHasOtherAnimalsChange),
        Question.Text("Если да — то какие?", "Виды животных", state.otherAnimals, viewModel::onOtherAnimalsChange),
        Question.Text("Сколько часов в день питомец будет оставаться один?", "Часы", state.hoursAlone, viewModel::onHoursAloneChange),
        Question.Text("Кто будет ухаживать за питомцем во время вашего отсутствия или отпуска?", "Контактное лицо", state.caretaker, viewModel::onCaretakerChange)
    )

    // Секция 3: Опыт с животными (6 вопросов)
    val section3Questions = listOf(
        Question.YesNo("Были ли у вас раньше питомцы?",
            state.hadPetsBefore, viewModel::onHadPetsBeforeChange),
        Question.Text("Что с ними сейчас?", "Статус прошлых питомцев", state.petsNow, viewModel::onPetsNowChange),
        Question.YesNo("Есть ли у вас опыт ухода за собаками?",
            state.experienceDogs, viewModel::onExperienceDogsChange),
        Question.YesNo("Есть ли у вас опыт ухода за кошками?",
            state.experienceCats, viewModel::onExperienceCatsChange),
        Question.YesNo("Есть ли у вас опыт ухода за животными с особенностями?",
            state.experienceSpecialNeeds, viewModel::onExperienceSpecialNeedsChange),
        Question.Text("Почему вы решили взять питомца именно сейчас?", "", state.reasonNow, viewModel::onReasonNowChange, singleLine = false)
    )

    // Секция 4: Ответственность и готовность (8 вопросов)
    val section4Questions = listOf(
        Question.CheckboxGroup("Понимаете ли вы, что питомцу потребуется:",
            listOf("Время", "Внимание", "Обучение", "Ветеринарная помощь"),
            state.understandsNeeds, viewModel::onUnderstandsNeedsChange),
        Question.CheckboxGroup("Готовы ли вы к регулярным расходам на:",
            listOf("Корм", "Ветеринара", "Лекарства", "Прививки", "Груминг"),
            state.readyForExpenses, viewModel::onReadyForExpensesChange),
        Question.Text("Что вы будете делать, если питомец испортит мебель?", "", state.furnitureDamage, viewModel::onFurnitureDamageChange, singleLine = false),
        Question.Text("Что вы будете делать, если питомец будет шуметь?", "", state.noiseBehavior, viewModel::onNoiseBehaviorChange, singleLine = false),
        Question.Text("Что вы будете делать, если питомец окажется пугливым?", "", state.timidPet, viewModel::onTimidPetChange, singleLine = false),
        Question.Text("Что вы будете делать, если питомец долго адаптируется?", "", state.adaptation, viewModel::onAdaptationChange, singleLine = false),
        Question.YesNo("Готовы ли вы заниматься воспитанием и адаптацией питомца?",
            state.willingToTrain, viewModel::onWillingToTrainChange),
        Question.Text("Что вы будете делать, если у вас изменятся жизненные обстоятельства? (переезд, работа, рождение ребёнка)", "", state.lifeChanges, viewModel::onLifeChangesChange, singleLine = false),
        Question.Text("Есть ли что-то, что может помешать вам заботиться о питомце в ближайший год?", "", state.obstacles, viewModel::onObstaclesChange, singleLine = false)
    )

    // Секция 5: Безопасность (4 вопроса)
    val section5Questions = listOf(
        Question.CheckboxGroup("Установлены ли у вас:",
            listOf("Сетки на окнах", "Безопасные балконы", "Ограждения (для дома)"),
            state.safetyMeasures, viewModel::onSafetyMeasuresChange),
        Question.CheckboxGroup("Готовы ли вы:",
            listOf("Стерилизовать питомца (если нужно)", "Соблюдать рекомендации приюта", "Использовать адресник и поводок"),
            state.willingTo, viewModel::onWillingToChange),
        Question.YesNo("Готовы ли вы поддерживать связь после пристройства?",
            state.maintainContact, viewModel::onMaintainContactChange)
    )

    // Секция 6: Эмоциональная часть (3 вопроса)
    val section6Questions = listOf(
        Question.Text("Что для вас значит \"ответственный хозяин\"?", "", state.responsibleOwner, viewModel::onResponsibleOwnerChange, singleLine = false),
        Question.Text("Как вы представляете жизнь с питомцем?", "", state.lifeWithPet, viewModel::onLifeWithPetChange, singleLine = false),
        Question.Text("Почему, по вашему мнению, именно вы станете хорошим хозяином?", "", state.whyGoodOwner, viewModel::onWhyGoodOwnerChange, singleLine = false)
    )

    // Все секции
    val sections = listOf(
        "Основная информация" to section1Questions,
        "Жилищные условия" to section2Questions,
        "Опыт с животными" to section3Questions,
        "Ответственность и готовность" to section4Questions,
        "Безопасность" to section5Questions,
        "Эмоциональная часть" to section6Questions
    )

    val currentSection = sections[step]
    val currentQuestions = currentSection.second

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Завершить опросник?") },
            text = { Text("Вы уверены, что хотите завершить заполнение опросника? Все данные будут сохранены.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAndFinish(onFinish)
                    showConfirmation = false
                }) {
                    Text("Да, завершить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Нет")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Опросник", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Прогресс бар
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(24.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (step + 1).toFloat() / sections.size },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = Primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${step + 1}/${sections.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = currentSection.first,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(Modifier.height(24.dp))

            // Показываем все вопросы текущей секции
            Column {
                currentQuestions.forEachIndexed { index, question ->
                    when (question) {
                        is Question.Text -> {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = question.value,
                                onValueChange = question.onValueChange,
                                placeholder = { Text(question.hint, color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = question.singleLine,
                                minLines = if (question.singleLine) 1 else 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                )
                            )
                        }
                        is Question.Dropdown -> {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            var selected by remember { mutableStateOf(question.value) }
                            val onValueChange = question.onValueChange
                            
                            OutlinedTextField(
                                value = selected.ifEmpty { "Выберите вариант" },
                                onValueChange = { selected = it },
                                label = { Text(question.title) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true,
                                enabled = false,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                question.options.take(3).forEach { option ->
                                    FilterChip(
                                        selected = selected == option,
                                        onClick = {
                                            selected = option
                                            onValueChange(option)
                                        },
                                        label = { Text(option, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (question.options.size > 3) {
                                    FilterChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text("+${question.options.size - 3}", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        enabled = false
                                    )
                                }
                            }
                        }
                        is Question.YesNo -> {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilterChip(
                                    selected = question.value == "Да",
                                    onClick = { question.onValueChange("Да") },
                                    label = { Text("Да") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = question.value == "Нет",
                                    onClick = { question.onValueChange("Нет") },
                                    label = { Text("Нет") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        is Question.CheckboxGroup -> {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(12.dp))
                            Column {
                                question.options.forEach { option ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = option in question.selected,
                                            onCheckedChange = { checked ->
                                                val newSelected = if (checked) {
                                                    question.selected + option
                                                } else {
                                                    question.selected - option
                                                }
                                                question.onValueChange(newSelected)
                                            }
                                        )
                                        Text(option, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        is Question.TextArea -> {
                            Text(question.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = question.value,
                                onValueChange = question.onValueChange,
                                placeholder = { Text(question.hint, color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = question.minLines,
                                maxLines = 10
                            )
                        }
                    }
                    
                    if (index < currentQuestions.lastIndex) {
                        Spacer(Modifier.height(24.dp))
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

            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Назад")
                    }
                }

                PrimaryButton(
                    text = if (step == sections.lastIndex) "Завершить" else "Далее",
                    onClick = {
                        if (step == sections.lastIndex) {
                            showConfirmation = true
                        } else {
                            step++
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}