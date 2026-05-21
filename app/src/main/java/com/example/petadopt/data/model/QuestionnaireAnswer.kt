package com.example.petadopt.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionnaireAnswer(
    val id: String = "",
    val user_id: String = "",
    
    // Раздел 1: Основная информация
    val q1_full_name: String = "",
    val q1_age: Int? = null,
    val q1_city: String = "",
    val q1_occupation: String = "",
    val q1_contact_method: String = "",
    
    // Раздел 2: Жилищные условия
    val q2_housing_type: String = "",
    val q2_pets_allowed: Boolean? = null,
    val q2_living_with: List<String> = emptyList(),
    val q2_family_consent: Boolean? = null,
    val q2_has_children: Boolean? = null,
    val q2_children_ages: String = "",
    val q2_has_other_pets: Boolean? = null,
    val q2_other_pets_types: List<String> = emptyList(),
    val q2_hours_alone: Int? = null,
    val q2_caregiver: String = "",
    
    // Раздел 3: Опыт с животными
    val q3_had_pets_before: Boolean? = null,
    val q3_what_happened: String = "",
    val q3_dog_experience: Boolean? = null,
    val q3_cat_experience: Boolean? = null,
    val q3_special_needs_experience: Boolean? = null,
    val q3_why_now: String = "",
    
    // Раздел 4: Ответственность и готовность
    val q4_understand_requirements: Boolean = false,
    val q4_understand_time: Boolean = false,
    val q4_understand_attention: Boolean = false,
    val q4_understand_training: Boolean = false,
    val q4_understand_vet_care: Boolean = false,
    val q4_ready_expenses: Boolean = false,
    val q4_ready_food: Boolean = false,
    val q4_ready_vet: Boolean = false,
    val q4_ready_medication: Boolean = false,
    val q4_ready_vaccinations: Boolean = false,
    val q4_ready_grooming: Boolean = false,
    val q4_furniture_damage_plan: String = "",
    val q4_noise_plan: String = "",
    val q4_shy_pet_plan: String = "",
    val q4_long_adaptation_plan: String = "",
    val q4_ready_education: Boolean = false,
    val q4_life_changes_plan: String = "",
    val q4_obstacles_next_year: String = "",
    
    // Раздел 5: Безопасность
    val q5_safety_measures: List<String> = emptyList(),
    val q5_ready_neuter: Boolean = false,
    val q5_ready_recommendations: Boolean = false,
    val q5_ready_tracker: Boolean = false,
    val q5_ready_keep_contact: Boolean = false,
    
    // Раздел 6: Эмоциональная часть
    val q6_responsible_owner_meaning: String = "",
    val q6_life_with_pet_vision: String = "",
    val q6_why_good_owner: String = "",
    
    val created_at: String = "",
    val updated_at: String = ""
)

// Удобные расширения для доступа к данным
val QuestionnaireAnswer.displayName: String get() = q1_full_name
val QuestionnaireAnswer.displayAge: Int? get() = q1_age
val QuestionnaireAnswer.displayCity: String get() = q1_city
val QuestionnaireAnswer.displayOccupation: String get() = q1_occupation
val QuestionnaireAnswer.displayContactMethod: String get() = q1_contact_method
val QuestionnaireAnswer.displayHousingType: String get() = q2_housing_type
val QuestionnaireAnswer.displayPetsAllowed: String get() = if (q2_pets_allowed == true) "Да" else if (q2_pets_allowed == false) "Нет" else "Не указано"
val QuestionnaireAnswer.displayHasChildren: String get() = if (q2_has_children == true) "Да" else if (q2_has_children == false) "Нет" else "Не указано"
val QuestionnaireAnswer.displayHasOtherPets: String get() = if (q2_has_other_pets == true) "Да" else if (q2_has_other_pets == false) "Нет" else "Не указано"
val QuestionnaireAnswer.displayDogExperience: String get() = if (q3_dog_experience == true) "Да" else if (q3_dog_experience == false) "Нет" else "Не указано"
val QuestionnaireAnswer.displayCatExperience: String get() = if (q3_cat_experience == true) "Да" else if (q3_cat_experience == false) "Нет" else "Не указано"
