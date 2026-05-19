package com.example.petadopt.viewmodel

data class QuestionnaireState(
    // Раздел 1: Основная информация
    val q1_full_name: String = "",
    val q1_age: String = "",
    val q1_city: String = "",
    val q1_occupation: String = "",
    val q1_contact_method: String = "",
    
    // Раздел 2: Жилищные условия
    val q2_housing_type: String = "",
    val q2_pets_allowed: String = "",
    val q2_living_with: String = "",
    val q2_family_consent: String = "",
    val q2_has_children: String = "",
    val q2_children_ages: String = "",
    val q2_has_other_pets: String = "",
    val q2_other_pets_types: String = "",
    val q2_hours_alone: String = "",
    val q2_caregiver: String = "",
    
    // Раздел 3: Опыт с животными
    val q3_had_pets_before: String = "",
    val q3_what_happened: String = "",
    val q3_dog_experience: String = "",
    val q3_cat_experience: String = "",
    val q3_special_needs_experience: String = "",
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
    
    val isLoading: Boolean = false,
    val error: String? = null
)

// Расширения для удобного доступа к полям QuestionnaireState
val QuestionnaireState.name: String get() = q1_full_name
val QuestionnaireState.age: String get() = q1_age
val QuestionnaireState.city: String get() = q1_city
val QuestionnaireState.occupation: String get() = q1_occupation
val QuestionnaireState.contactMethod: String get() = q1_contact_method

val QuestionnaireState.housingType: String get() = q2_housing_type
val QuestionnaireState.petsAllowed: String get() = q2_pets_allowed
val QuestionnaireState.livingWith: String get() = q2_living_with
val QuestionnaireState.familyAgreement: String get() = q2_family_consent
val QuestionnaireState.hasChildren: String get() = q2_has_children
val QuestionnaireState.childrenAge: String get() = q2_children_ages
val QuestionnaireState.hasOtherAnimals: String get() = q2_has_other_pets
val QuestionnaireState.otherAnimals: String get() = q2_other_pets_types
val QuestionnaireState.hoursAlone: String get() = q2_hours_alone
val QuestionnaireState.caretaker: String get() = q2_caregiver

val QuestionnaireState.hadPetsBefore: String get() = q3_had_pets_before
val QuestionnaireState.petsNow: String get() = q3_what_happened
val QuestionnaireState.experienceDogs: String get() = q3_dog_experience
val QuestionnaireState.experienceCats: String get() = q3_cat_experience
val QuestionnaireState.experienceSpecialNeeds: String get() = q3_special_needs_experience
val QuestionnaireState.reasonNow: String get() = q3_why_now

val QuestionnaireState.understandsNeeds: List<String> get() = buildList {
    if (q4_understand_requirements) add("Время")
    if (q4_understand_time) add("Время")
    if (q4_understand_attention) add("Внимание")
    if (q4_understand_training) add("Обучение")
    if (q4_understand_vet_care) add("Ветеринарная помощь")
}
val QuestionnaireState.readyForExpenses: List<String> get() = buildList {
    if (q4_ready_expenses) add("Общие расходы")
    if (q4_ready_food) add("Корм")
    if (q4_ready_vet) add("Ветеринара")
    if (q4_ready_medication) add("Лекарства")
    if (q4_ready_vaccinations) add("Прививки")
    if (q4_ready_grooming) add("Груминг")
}
val QuestionnaireState.furnitureDamage: String get() = q4_furniture_damage_plan
val QuestionnaireState.noiseBehavior: String get() = q4_noise_plan
val QuestionnaireState.timidPet: String get() = q4_shy_pet_plan
val QuestionnaireState.adaptation: String get() = q4_long_adaptation_plan
val QuestionnaireState.willingToTrain: String get() = if (q4_ready_education) "Да" else "Нет"
val QuestionnaireState.lifeChanges: String get() = q4_life_changes_plan
val QuestionnaireState.obstacles: String get() = q4_obstacles_next_year

val QuestionnaireState.safetyMeasures: List<String> get() = q5_safety_measures
val QuestionnaireState.willingTo: List<String> get() = buildList {
    if (q5_ready_neuter) add("Стерилизовать")
    if (q5_ready_recommendations) add("Соблюдать рекомендации")
    if (q5_ready_tracker) add("Использовать адресник")
}
val QuestionnaireState.maintainContact: String get() = if (q5_ready_keep_contact) "Да" else "Нет"

val QuestionnaireState.responsibleOwner: String get() = q6_responsible_owner_meaning
val QuestionnaireState.lifeWithPet: String get() = q6_life_with_pet_vision
val QuestionnaireState.whyGoodOwner: String get() = q6_why_good_owner