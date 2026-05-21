package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.repository.QuestionnaireRepository
import com.example.petadopt.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    // Если уже английское значение — проверяем валидность
    if (value in validHousingTypes) {
        return value
    }
    // Пытаемся перевести руское значение
    return housingTypeMap[value] ?: "other"
}

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val repository: QuestionnaireRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState())
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    // Раздел 1: Основная информация
    fun onNameChange(value: String) = updateState { copy(q1_full_name = value) }
    fun onAgeChange(value: String) = updateState { copy(q1_age = value) }
    fun onCityChange(value: String) = updateState { copy(q1_city = value) }
    fun onOccupationChange(value: String) = updateState { copy(q1_occupation = value) }
    fun onContactMethodChange(value: String) = updateState { copy(q1_contact_method = value) }

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
            q4_understand_requirements = "Время" in value,
            q4_understand_time = "Время" in value,
            q4_understand_attention = "Внимание" in value,
            q4_understand_training = "Обучение" in value,
            q4_understand_vet_care = "Ветеринарная помощь" in value
        ) 
    }
    fun onReadyForExpensesChange(value: List<String>) = updateState { 
        copy(
            q4_ready_expenses = true,
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
    fun onWillingToChange(value: List<String>) = updateState { 
        copy(
            q5_ready_neuter = "Стерилизовать" in value,
            q5_ready_recommendations = "Соблюдать рекомендации" in value,
            q5_ready_tracker = "Использовать адресник" in value
        ) 
    }
    fun onMaintainContactChange(value: String) = updateState { copy(q5_ready_keep_contact = value == "Да") }

    // Раздел 6: Эмоциональная часть
    fun onResponsibleOwnerChange(value: String) = updateState { copy(q6_responsible_owner_meaning = value) }
    fun onLifeWithPetChange(value: String) = updateState { copy(q6_life_with_pet_vision = value) }
    fun onWhyGoodOwnerChange(value: String) = updateState { copy(q6_why_good_owner = value) }

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

            val answer = QuestionnaireAnswer(
                user_id = userId,
                q1_full_name = _state.value.q1_full_name,
                q1_age = _state.value.q1_age.toIntOrNull(),
                q1_city = _state.value.q1_city,
                q1_occupation = _state.value.q1_occupation,
                q1_contact_method = _state.value.q1_contact_method,
                q2_housing_type = mapHousingTypeToDb(_state.value.q2_housing_type),
                q2_pets_allowed = _state.value.q2_pets_allowed.toBooleanStrictOrNull(),
                q2_living_with = _state.value.q2_living_with.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_family_consent = _state.value.q2_family_consent.toBooleanStrictOrNull(),
                q2_has_children = _state.value.q2_has_children.toBooleanStrictOrNull(),
                q2_children_ages = _state.value.q2_children_ages,
                q2_has_other_pets = _state.value.q2_has_other_pets.toBooleanStrictOrNull(),
                q2_other_pets_types = _state.value.q2_other_pets_types.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                q2_hours_alone = _state.value.q2_hours_alone.toIntOrNull(),
                q2_caregiver = _state.value.q2_caregiver,
                q3_had_pets_before = _state.value.q3_had_pets_before.toBooleanStrictOrNull(),
                q3_what_happened = _state.value.q3_what_happened,
                q3_dog_experience = _state.value.q3_dog_experience.toBooleanStrictOrNull(),
                q3_cat_experience = _state.value.q3_cat_experience.toBooleanStrictOrNull(),
                q3_special_needs_experience = _state.value.q3_special_needs_experience.toBooleanStrictOrNull(),
                q3_why_now = _state.value.q3_why_now,
                q4_understand_requirements = _state.value.q4_understand_requirements,
                q4_understand_time = _state.value.q4_understand_time,
                q4_understand_attention = _state.value.q4_understand_attention,
                q4_understand_training = _state.value.q4_understand_training,
                q4_understand_vet_care = _state.value.q4_understand_vet_care,
                q4_ready_expenses = _state.value.q4_ready_expenses,
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
                q5_safety_measures = _state.value.q5_safety_measures,
                q5_ready_neuter = _state.value.q5_ready_neuter,
                q5_ready_recommendations = _state.value.q5_ready_recommendations,
                q5_ready_tracker = _state.value.q5_ready_tracker,
                q5_ready_keep_contact = _state.value.q5_ready_keep_contact,
                q6_responsible_owner_meaning = _state.value.q6_responsible_owner_meaning,
                q6_life_with_pet_vision = _state.value.q6_life_with_pet_vision,
                q6_why_good_owner = _state.value.q6_why_good_owner
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
                _state.value = _state.value.copy(isLoading = false)
                
                answer?.let { a ->
                    _state.value = _state.value.copy(
                        q1_full_name = a.q1_full_name,
                        q1_age = a.q1_age?.toString() ?: "",
                        q1_city = a.q1_city,
                        q1_occupation = a.q1_occupation,
                        q1_contact_method = a.q1_contact_method,
                        q2_housing_type = a.q2_housing_type,
                        q2_pets_allowed = a.q2_pets_allowed?.toString() ?: "",
                        q2_living_with = a.q2_living_with.joinToString(", "),
                        q2_family_consent = a.q2_family_consent?.toString() ?: "",
                        q2_has_children = a.q2_has_children?.toString() ?: "",
                        q2_children_ages = a.q2_children_ages,
                        q2_has_other_pets = a.q2_has_other_pets?.toString() ?: "",
                        q2_other_pets_types = a.q2_other_pets_types.joinToString(", "),
                        q2_hours_alone = a.q2_hours_alone?.toString() ?: "",
                        q2_caregiver = a.q2_caregiver,
                        q3_had_pets_before = a.q3_had_pets_before?.toString() ?: "",
                        q3_what_happened = a.q3_what_happened,
                        q3_dog_experience = a.q3_dog_experience?.toString() ?: "",
                        q3_cat_experience = a.q3_cat_experience?.toString() ?: "",
                        q3_special_needs_experience = a.q3_special_needs_experience?.toString() ?: "",
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
                        q5_safety_measures = a.q5_safety_measures,
                        q5_ready_neuter = a.q5_ready_neuter,
                        q5_ready_recommendations = a.q5_ready_recommendations,
                        q5_ready_tracker = a.q5_ready_tracker,
                        q5_ready_keep_contact = a.q5_ready_keep_contact,
                        q6_responsible_owner_meaning = a.q6_responsible_owner_meaning,
                        q6_life_with_pet_vision = a.q6_life_with_pet_vision,
                        q6_why_good_owner = a.q6_why_good_owner
                    )
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
