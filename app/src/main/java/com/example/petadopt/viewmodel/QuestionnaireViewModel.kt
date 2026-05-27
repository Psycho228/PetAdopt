package com.example.petadopt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.repository.QuestionnaireRepository
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.domain.usecase.AssessRiskUseCase
import com.example.petadopt.data.model.GigaChatRiskAssessment
import com.example.petadopt.data.model.RiskAssessmentRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Мапинг русских значений типа жилья в английские для БД
private val housingTypeMap = mapOf(
    "Квартира" to "apartment",
    "Частный дом" to "house",
    "Съёмное жильё" to "rented",
    "Другое" to "other"
)

// Обратный мапинг для валидации
private val validHousingTypes = setOf("apartment", "house", "rented", "other")

private fun mapHousingTypeToDb(value: String): String {
    if (value in validHousingTypes) {
        return value
    }
    return housingTypeMap[value] ?: "other"
}

// Маппинг "Да"/"Нет" в Boolean и обратно
private fun String.toBooleanFromYesNo(): Boolean? = when (this) {
    "Да" -> true
    "Нет" -> false
    else -> null
}

private fun Boolean?.toYesNo(): String = when (this) {
    true -> "Да"
    false -> "Нет"
    null -> ""
}

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val repository: QuestionnaireRepository,
    private val authRepository: AuthRepository,
    private val assessRiskUseCase: AssessRiskUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "QuestionnaireViewModel"
    }
    
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(QuestionnaireState())
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    // Раздел 1: Основная информация
    fun onNameChange(value: String) = updateState { copy(q1_full_name = value) }
    fun onAgeChange(value: String) = updateState { 
        // Разрешаем ввод только цифр
        if (value.all { it.isDigit() } || value.isEmpty()) {
            copy(q1_age = value)
        } else {
            copy(q1_age = value.filter { it.isDigit() })
        }
    }
    fun onCityChange(value: String) = updateState { copy(q1_city = value) }
    fun onOccupationChange(value: String) = updateState { copy(q1_occupation = value) }
    fun onPhoneChange(value: String) = updateState { 
        // Разрешаем ввод цифр, +, пробелов, скобок, тире
        val filtered = value.filter { it.isDigit() || it == '+' || it == ' ' || it == '(' || it == ')' || it == '-' }
        copy(q1_phone = filtered)
    }
    fun onEmailChange(value: String) = updateState { copy(q1_email = value) }

    // Раздел 2: Жилищные условия
    fun onHousingTypeChange(value: String) = updateState { copy(q2_housing_type = value) }
    fun onPetsAllowedChange(value: String) = updateState { copy(q2_pets_allowed = value) }
    fun onLivingWithChange(value: String) = updateState { copy(q2_living_with = value) }
    fun onFamilyAgreementChange(value: String) = updateState { copy(q2_family_consent = value) }
    fun onHasChildrenChange(value: String) = updateState { copy(q2_has_children = value) }
    fun onChildrenAgeChange(value: String) = updateState { copy(q2_children_ages = value) }
    fun onHasOtherAnimalsChange(value: String) = updateState { copy(q2_has_other_pets = value) }
    fun onOtherAnimalsChange(value: String) = updateState { copy(q2_other_pets_types = value) }
    fun onHoursAloneChange(value: String) = updateState { copy(q2_hours_alone = value) }
    fun onCaretakerChange(value: String) = updateState { copy(q2_caregiver = value) }

    // Раздел 3: Опыт с животными
    fun onHadPetsBeforeChange(value: String) = updateState { copy(q3_had_pets_before = value) }
    fun onPetsNowChange(value: String) = updateState { copy(q3_what_happened = value) }
    fun onExperienceDogsChange(value: String) = updateState { copy(q3_dog_experience = value) }
    fun onExperienceCatsChange(value: String) = updateState { copy(q3_cat_experience = value) }
    fun onExperienceSpecialNeedsChange(value: String) = updateState { copy(q3_special_needs_experience = value) }
    fun onReasonNowChange(value: String) = updateState { copy(q3_why_now = value) }

    // Раздел 4: Ответственность и готовность
    fun onUnderstandsNeedsChange(value: List<String>) = updateState { 
        copy(
            q4_understand_time = "Время" in value,
            q4_understand_attention = "Внимание" in value,
            q4_understand_training = "Обучение" in value,
            q4_understand_vet_care = "Ветеринарная помощь" in value
        ) 
    }
    fun onReadyForExpensesChange(value: List<String>) = updateState { 
        copy(
            q4_ready_food = "Корм" in value,
            q4_ready_vet = "Ветеринара" in value,
            q4_ready_medication = "Лекарства" in value,
            q4_ready_vaccinations = "Прививки" in value,
            q4_ready_grooming = "Груминг" in value
        ) 
    }
    fun onFurnitureDamageChange(value: String) = updateState { copy(q4_furniture_damage_plan = value) }
    fun onNoiseBehaviorChange(value: String) = updateState { copy(q4_noise_plan = value) }
    fun onTimidPetChange(value: String) = updateState { copy(q4_shy_pet_plan = value) }
    fun onAdaptationChange(value: String) = updateState { copy(q4_long_adaptation_plan = value) }
    fun onWillingToTrainChange(value: String) = updateState { copy(q4_ready_education = value == "Да") }
    fun onLifeChangesChange(value: String) = updateState { copy(q4_life_changes_plan = value) }
    fun onObstaclesChange(value: String) = updateState { copy(q4_obstacles_next_year = value) }

    // Раздел 5: Безопасность
    fun onSafetyMeasuresChange(value: List<String>) = updateState { 
        copy(q5_safety_measures = value) 
    }
    fun onWillingToNeuterChange(checked: Boolean) = updateState { copy(q5_ready_neuter = checked) }
    fun onWillingToRecommendationsChange(checked: Boolean) = updateState { copy(q5_ready_recommendations = checked) }
    fun onWillingToTrackerChange(checked: Boolean) = updateState { copy(q5_ready_tracker = checked) }
    fun onWillingToChange(selected: List<String>) = updateState {
        copy(
            q5_ready_neuter = selected.contains("Стерилизовать питомца (если нужно)"),
            q5_ready_recommendations = selected.contains("Соблюдать рекомендации приюта"),
            q5_ready_tracker = selected.contains("Использовать адресник и поводок")
        )
    }
    fun onMaintainContactChange(value: String) = updateState { copy(q5_ready_keep_contact = value == "Да") }

    // Раздел 6: Эмоциональная часть
    fun onResponsibleOwnerChange(value: String) = updateState { copy(q6_responsible_owner_meaning = value) }
    fun onLifeWithPetChange(value: String) = updateState { copy(q6_life_with_pet_vision = value) }
    fun onWhyGoodOwnerChange(value: String) = updateState { copy(q6_why_good_owner = value) }

    // Раздел 7: Желаемые виды животных
    fun onDesiredPetsChange(value: List<String>) = updateState { copy(q7_desired_pets = value) }

    private fun updateState(update: QuestionnaireState.() -> QuestionnaireState) {
        _state.value = _state.value.update()
    }

    fun saveAndFinish(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val userId = authRepository.currentUserId ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка: пользователь не авторизован"
                )
                return@launch
            }

            // Вычисляем сводные поля для чекбоксов
            val understandRequirements = _state.value.q4_understand_time || 
                _state.value.q4_understand_attention || 
                _state.value.q4_understand_training || 
                _state.value.q4_understand_vet_care
            
            val readyForExpenses = _state.value.q4_ready_food || 
                _state.value.q4_ready_vet || 
                _state.value.q4_ready_medication || 
                _state.value.q4_ready_vaccinations || 
                _state.value.q4_ready_grooming
            
            // Фильтруем заглушку "—" из списка мер безопасности
            val safetyMeasuresList = _state.value.q5_safety_measures
                .filter { it != "—" }
                .ifEmpty {
                    // Фоллбэк: собираем из отдельных чекбоксов q5_ready_*
                    buildList {
                        if (_state.value.q5_ready_neuter) add("Стерилизовать питомца (если нужно)")
                        if (_state.value.q5_ready_recommendations) add("Соблюдать рекомендации приюта")
                        if (_state.value.q5_ready_tracker) add("Использовать адресник и поводок")
                    }
                }

            val answer = QuestionnaireAnswer(
                user_id = userId,
                q1_full_name = _state.value.q1_full_name,
                q1_age = _state.value.q1_age.toIntOrNull(),
                q1_city = _state.value.q1_city,
                q1_occupation = _state.value.q1_occupation,
                q1_phone = _state.value.q1_phone,
                q1_email = _state.value.q1_email,
                q2_housing_type = mapHousingTypeToDb(_state.value.q2_housing_type),
                q2_pets_allowed = _state.value.q2_pets_allowed.toBooleanFromYesNo(),
                q2_living_with = _state.value.q2_living_with.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_family_consent = _state.value.q2_family_consent.toBooleanFromYesNo(),
                q2_has_children = _state.value.q2_has_children.toBooleanFromYesNo(),
                q2_children_ages = _state.value.q2_children_ages,
                q2_has_other_pets = _state.value.q2_has_other_pets.toBooleanFromYesNo(),
                q2_other_pets_types = _state.value.q2_other_pets_types.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_hours_alone = _state.value.q2_hours_alone.toIntOrNull(),
                q2_caregiver = _state.value.q2_caregiver,
                q3_had_pets_before = _state.value.q3_had_pets_before.toBooleanFromYesNo(),
                q3_what_happened = _state.value.q3_what_happened,
                q3_dog_experience = _state.value.q3_dog_experience.toBooleanFromYesNo(),
                q3_cat_experience = _state.value.q3_cat_experience.toBooleanFromYesNo(),
                q3_special_needs_experience = _state.value.q3_special_needs_experience.toBooleanFromYesNo(),
                q3_why_now = _state.value.q3_why_now,
                q4_understand_requirements = understandRequirements,
                q4_understand_time = _state.value.q4_understand_time,
                q4_understand_attention = _state.value.q4_understand_attention,
                q4_understand_training = _state.value.q4_understand_training,
                q4_understand_vet_care = _state.value.q4_understand_vet_care,
                q4_ready_expenses = readyForExpenses,
                q4_ready_food = _state.value.q4_ready_food,
                q4_ready_vet = _state.value.q4_ready_vet,
                q4_ready_medication = _state.value.q4_ready_medication,
                q4_ready_vaccinations = _state.value.q4_ready_vaccinations,
                q4_ready_grooming = _state.value.q4_ready_grooming,
                q4_furniture_damage_plan = _state.value.q4_furniture_damage_plan,
                q4_noise_plan = _state.value.q4_noise_plan,
                q4_shy_pet_plan = _state.value.q4_shy_pet_plan,
                q4_long_adaptation_plan = _state.value.q4_long_adaptation_plan,
                q4_ready_education = _state.value.q4_ready_education,
                q4_life_changes_plan = _state.value.q4_life_changes_plan,
                q4_obstacles_next_year = _state.value.q4_obstacles_next_year,
                q5_safety_measures = safetyMeasuresList,
                q5_ready_neuter = _state.value.q5_ready_neuter,
                q5_ready_recommendations = _state.value.q5_ready_recommendations,
                q5_ready_tracker = _state.value.q5_ready_tracker,
                q5_ready_keep_contact = _state.value.q5_ready_keep_contact,
                q6_responsible_owner_meaning = _state.value.q6_responsible_owner_meaning,
                q6_life_with_pet_vision = _state.value.q6_life_with_pet_vision,
                q6_why_good_owner = _state.value.q6_why_good_owner,
                q7_desired_pets = _state.value.q7_desired_pets
            )

            try {
                repository.saveAnswers(answer)
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }

    /**
     * Сохраняет опросник и запускает оценку рисков через GigaChat
     */
    fun saveWithRiskAssessment(
        onSuccess: (GigaChatRiskAssessment) -> Unit,
        onRiskAssessed: (Result<GigaChatRiskAssessment>) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val userId = authRepository.currentUserId ?: run {
                Log.e(TAG, "Пользователь не авторизован")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка: пользователь не авторизован"
                )
                return@launch
            }
            
            Log.d(TAG, "=== Запуск сохранения опросника и оценки рисков для userId: $userId ===")

            // Вычисляем сводные поля для чекбоксов
            val understandRequirements = _state.value.q4_understand_time || 
                _state.value.q4_understand_attention || 
                _state.value.q4_understand_training || 
                _state.value.q4_understand_vet_care
            
            val readyForExpenses = _state.value.q4_ready_food || 
                _state.value.q4_ready_vet || 
                _state.value.q4_ready_medication || 
                _state.value.q4_ready_vaccinations || 
                _state.value.q4_ready_grooming
            
            // Фильтруем заглушку "—" из списка мер безопасности
            val safetyMeasuresList = _state.value.q5_safety_measures
                .filter { it != "—" }
                .ifEmpty {
                    // Фоллбэк: собираем из отдельных чекбоксов q5_ready_*
                    buildList {
                        if (_state.value.q5_ready_neuter) add("Стерилизовать питомца (если нужно)")
                        if (_state.value.q5_ready_recommendations) add("Соблюдать рекомендации приюта")
                        if (_state.value.q5_ready_tracker) add("Использовать адресник и поводок")
                    }
                }

            val answer = QuestionnaireAnswer(
                user_id = userId,
                q1_full_name = _state.value.q1_full_name,
                q1_age = _state.value.q1_age.toIntOrNull(),
                q1_city = _state.value.q1_city,
                q1_occupation = _state.value.q1_occupation,
                q1_phone = _state.value.q1_phone,
                q1_email = _state.value.q1_email,
                q2_housing_type = mapHousingTypeToDb(_state.value.q2_housing_type),
                q2_pets_allowed = _state.value.q2_pets_allowed.toBooleanFromYesNo(),
                q2_living_with = _state.value.q2_living_with.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_family_consent = _state.value.q2_family_consent.toBooleanFromYesNo(),
                q2_has_children = _state.value.q2_has_children.toBooleanFromYesNo(),
                q2_children_ages = _state.value.q2_children_ages,
                q2_has_other_pets = _state.value.q2_has_other_pets.toBooleanFromYesNo(),
                q2_other_pets_types = _state.value.q2_other_pets_types.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_hours_alone = _state.value.q2_hours_alone.toIntOrNull(),
                q2_caregiver = _state.value.q2_caregiver,
                q3_had_pets_before = _state.value.q3_had_pets_before.toBooleanFromYesNo(),
                q3_what_happened = _state.value.q3_what_happened,
                q3_dog_experience = _state.value.q3_dog_experience.toBooleanFromYesNo(),
                q3_cat_experience = _state.value.q3_cat_experience.toBooleanFromYesNo(),
                q3_special_needs_experience = _state.value.q3_special_needs_experience.toBooleanFromYesNo(),
                q3_why_now = _state.value.q3_why_now,
                q4_understand_requirements = understandRequirements,
                q4_understand_time = _state.value.q4_understand_time,
                q4_understand_attention = _state.value.q4_understand_attention,
                q4_understand_training = _state.value.q4_understand_training,
                q4_understand_vet_care = _state.value.q4_understand_vet_care,
                q4_ready_expenses = readyForExpenses,
                q4_ready_food = _state.value.q4_ready_food,
                q4_ready_vet = _state.value.q4_ready_vet,
                q4_ready_medication = _state.value.q4_ready_medication,
                q4_ready_vaccinations = _state.value.q4_ready_vaccinations,
                q4_ready_grooming = _state.value.q4_ready_grooming,
                q4_furniture_damage_plan = _state.value.q4_furniture_damage_plan,
                q4_noise_plan = _state.value.q4_noise_plan,
                q4_shy_pet_plan = _state.value.q4_shy_pet_plan,
                q4_long_adaptation_plan = _state.value.q4_long_adaptation_plan,
                q4_ready_education = _state.value.q4_ready_education,
                q4_life_changes_plan = _state.value.q4_life_changes_plan,
                q4_obstacles_next_year = _state.value.q4_obstacles_next_year,
                q5_safety_measures = safetyMeasuresList,
                q5_ready_neuter = _state.value.q5_ready_neuter,
                q5_ready_recommendations = _state.value.q5_ready_recommendations,
                q5_ready_tracker = _state.value.q5_ready_tracker,
                q5_ready_keep_contact = _state.value.q5_ready_keep_contact,
                q6_responsible_owner_meaning = _state.value.q6_responsible_owner_meaning,
                q6_life_with_pet_vision = _state.value.q6_life_with_pet_vision,
                q6_why_good_owner = _state.value.q6_why_good_owner,
                q7_desired_pets = _state.value.q7_desired_pets
            )

            try {
                Log.d(TAG, "1. Сохранение ответов опросника...")
                repository.saveAnswers(answer)
                Log.d(TAG, "Ответы опросника сохранены успешно")
                
                Log.d(TAG, "2. Запуск оценки рисков через GigaChat...")
                val result = assessRiskUseCase(answer)
                
                result.onSuccess { (assessment, requestId) ->
                    Log.d(TAG, "Оценка рисков успешна! requestId=$requestId")
                    val assessmentRecord = RiskAssessmentRecord(
                        user_id = userId,
                        questionnaire_answer_id = answer.id.ifEmpty { userId },
                        overallRisk = assessment.overallRisk.name,
                        riskScore = assessment.riskScore,
                        recommendation = assessment.recommendation,
                        detailedAnalysis = assessment.detailedAnalysis,
                        riskFactorsJson = json.encodeToString(assessment.riskFactors),
                        positiveFactorsJson = json.encodeToString(assessment.positiveFactors),
                        recommendationsJson = json.encodeToString(assessment.recommendations),
                        gigachat_request_id = requestId
                    )
                    
                    Log.d(TAG, "3. Сохранение оценки рисков в БД...")
                    repository.saveRiskAssessment(assessmentRecord)
                    Log.d(TAG, "Оценка рисков сохранена в БД успешно!")
                    
                    _state.value = _state.value.copy(isLoading = false)
                    onSuccess(assessment)
                    onRiskAssessed(Result.success(assessment))
                }.onFailure { error ->
                    Log.e(TAG, "Ошибка оценки рисков: ${error.message}", error)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Ошибка оценки рисков: ${error.message}"
                    )
                    onRiskAssessed(Result.failure(error))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Критическая ошибка при сохранении: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка сохранения или оценки: ${e.message}"
                )
                onRiskAssessed(Result.failure(e))
            }
        }
    }

    fun loadAnswers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val userId = authRepository.currentUserId ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка: пользователь не авторизован"
                )
                return@launch
            }

            try {
                val answer = repository.getAnswers()
                
                answer?.let { a ->
                    // Восстанавливаем списки чекбоксов
                    val understandsNeeds = buildList {
                        if (a.q4_understand_requirements) add("Время")
                        if (a.q4_understand_attention) add("Внимание")
                        if (a.q4_understand_training) add("Обучение")
                        if (a.q4_understand_vet_care) add("Ветеринарная помощь")
                    }
                    
                    val readyForExpenses = buildList {
                        if (a.q4_ready_food) add("Корм")
                        if (a.q4_ready_vet) add("Ветеринара")
                        if (a.q4_ready_medication) add("Лекарства")
                        if (a.q4_ready_vaccinations) add("Прививки")
                        if (a.q4_ready_grooming) add("Груминг")
                    }
                    
                    // Читаем q5_safety_measures из БД (это массив "Сетки", "Балконы", "Ограждения")
                    val safetyMeasuresFromDb = a.q5_safety_measures ?: emptyList()
                    val q5_safety_screens = "Сетки на окнах" in safetyMeasuresFromDb
                    val q5_safety_balconies = "Безопасные балконы" in safetyMeasuresFromDb
                    val q5_safety_barriers = "Ограждения (для дома)" in safetyMeasuresFromDb
                    
                    val willingTo = buildList {
                        if (a.q5_ready_neuter) add("Стерилизовать питомца (если нужно)")
                        if (a.q5_ready_recommendations) add("Соблюдать рекомендации приюта")
                        if (a.q5_ready_tracker) add("Использовать адресник и поводок")
                    }
                    
                    // Обновляем state ОДИН РАЗ со всеми данными
                    _state.value = _state.value.copy(
                        isLoading = false,
                        q1_full_name = a.q1_full_name,
                        q1_age = a.q1_age?.toString() ?: "",
                        q1_city = a.q1_city,
                        q1_occupation = a.q1_occupation,
                        q1_phone = a.q1_phone,
                        q1_email = a.q1_email,
                        q2_housing_type = a.q2_housing_type,
                        q2_pets_allowed = a.q2_pets_allowed.toYesNo(),
                        q2_living_with = a.q2_living_with.joinToString(", "),
                        q2_family_consent = a.q2_family_consent.toYesNo(),
                        q2_has_children = a.q2_has_children.toYesNo(),
                        q2_children_ages = a.q2_children_ages,
                        q2_has_other_pets = a.q2_has_other_pets.toYesNo(),
                        q2_other_pets_types = a.q2_other_pets_types.joinToString(", "),
                        q2_hours_alone = a.q2_hours_alone?.toString() ?: "",
                        q2_caregiver = a.q2_caregiver,
                        q3_had_pets_before = a.q3_had_pets_before.toYesNo(),
                        q3_what_happened = a.q3_what_happened,
                        q3_dog_experience = a.q3_dog_experience.toYesNo(),
                        q3_cat_experience = a.q3_cat_experience.toYesNo(),
                        q3_special_needs_experience = a.q3_special_needs_experience.toYesNo(),
                        q3_why_now = a.q3_why_now,
                        q4_understand_requirements = a.q4_understand_requirements,
                        q4_understand_time = a.q4_understand_time,
                        q4_understand_attention = a.q4_understand_attention,
                        q4_understand_training = a.q4_understand_training,
                        q4_understand_vet_care = a.q4_understand_vet_care,
                        q4_ready_expenses = a.q4_ready_expenses,
                        q4_ready_food = a.q4_ready_food,
                        q4_ready_vet = a.q4_ready_vet,
                        q4_ready_medication = a.q4_ready_medication,
                        q4_ready_vaccinations = a.q4_ready_vaccinations,
                        q4_ready_grooming = a.q4_ready_grooming,
                        q4_furniture_damage_plan = a.q4_furniture_damage_plan,
                        q4_noise_plan = a.q4_noise_plan,
                        q4_shy_pet_plan = a.q4_shy_pet_plan,
                        q4_long_adaptation_plan = a.q4_long_adaptation_plan,
                        q4_ready_education = a.q4_ready_education,
                        q4_life_changes_plan = a.q4_life_changes_plan,
                        q4_obstacles_next_year = a.q4_obstacles_next_year,
                        q5_safety_measures = safetyMeasuresFromDb,
                        q5_ready_neuter = a.q5_ready_neuter,
                        q5_ready_recommendations = a.q5_ready_recommendations,
                        q5_ready_tracker = a.q5_ready_tracker,
                        q5_ready_keep_contact = a.q5_ready_keep_contact,
                        q6_responsible_owner_meaning = a.q6_responsible_owner_meaning,
                        q6_life_with_pet_vision = a.q6_life_with_pet_vision,
                        q6_why_good_owner = a.q6_why_good_owner,
                        q7_desired_pets = a.q7_desired_pets ?: emptyList()
                    )
                    
                    // Обновляем списки чекбоксов (вычисляются из bool-полей)
                    onUnderstandsNeedsChange(understandsNeeds)
                    onReadyForExpensesChange(readyForExpenses)
                } ?: run {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }
}