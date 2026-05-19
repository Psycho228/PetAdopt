package com.example.petadopt.data.model

// Расширения для удобного доступа к полям QuestionnaireAnswer
val QuestionnaireAnswer.name: String get() = q1_full_name
val QuestionnaireAnswer.age: String get() = q1_age?.toString() ?: "—"
val QuestionnaireAnswer.city: String get() = q1_city.ifBlank { "—" }
val QuestionnaireAnswer.occupation: String get() = q1_occupation.ifBlank { "—" }
val QuestionnaireAnswer.contactMethod: String get() = q1_contact_method.ifBlank { "—" }

val QuestionnaireAnswer.housingType: String get() = q2_housing_type.ifBlank { "—" }
val QuestionnaireAnswer.petsAllowed: String get() = when (q2_pets_allowed) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.livingWith: String get() = q2_living_with.joinToString(", ") { it.ifBlank { "—" } }
val QuestionnaireAnswer.familyAgreement: String get() = when (q2_family_consent) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.hasChildren: String get() = when (q2_has_children) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.childrenAge: String get() = q2_children_ages.ifBlank { "—" }
val QuestionnaireAnswer.hasOtherAnimals: String get() = when (q2_has_other_pets) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.otherAnimals: String get() = q2_other_pets_types.joinToString(", ") { it.ifBlank { "—" } }
val QuestionnaireAnswer.hoursAlone: String get() = q2_hours_alone?.toString() ?: "—"
val QuestionnaireAnswer.caretaker: String get() = q2_caregiver.ifBlank { "—" }

val QuestionnaireAnswer.hadPetsBefore: String get() = when (q3_had_pets_before) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.petsNow: String get() = q3_what_happened.ifBlank { "—" }
val QuestionnaireAnswer.experienceDogs: String get() = when (q3_dog_experience) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.experienceCats: String get() = when (q3_cat_experience) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.experienceSpecialNeeds: String get() = when (q3_special_needs_experience) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.reasonNow: String get() = q3_why_now.ifBlank { "—" }

val QuestionnaireAnswer.understandsNeeds: List<String> get() {
    val list = mutableListOf<String>()
    if (q4_understand_requirements) list.add("Требования")
    if (q4_understand_time) list.add("Время")
    if (q4_understand_attention) list.add("Внимание")
    if (q4_understand_training) list.add("Обучение")
    if (q4_understand_vet_care) list.add("Ветпомощь")
    return list.ifEmpty { listOf("—") }
}
val QuestionnaireAnswer.readyForExpenses: List<String> get() {
    val list = mutableListOf<String>()
    if (q4_ready_expenses) list.add("Расходы")
    if (q4_ready_food) list.add("Корм")
    if (q4_ready_vet) list.add("Ветеринар")
    if (q4_ready_medication) list.add("Лекарства")
    if (q4_ready_vaccinations) list.add("Прививки")
    if (q4_ready_grooming) list.add("Груминг")
    return list.ifEmpty { listOf("—") }
}
val QuestionnaireAnswer.furnitureDamage: String get() = q4_furniture_damage_plan.ifBlank { "—" }
val QuestionnaireAnswer.noiseBehavior: String get() = q4_noise_plan.ifBlank { "—" }
val QuestionnaireAnswer.timidPet: String get() = q4_shy_pet_plan.ifBlank { "—" }
val QuestionnaireAnswer.adaptation: String get() = q4_long_adaptation_plan.ifBlank { "—" }
val QuestionnaireAnswer.willingToTrain: String get() = when (q4_ready_education) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}
val QuestionnaireAnswer.lifeChanges: String get() = q4_life_changes_plan.ifBlank { "—" }
val QuestionnaireAnswer.obstacles: String get() = q4_obstacles_next_year.ifBlank { "—" }

val QuestionnaireAnswer.safetyMeasures: List<String> get() = q5_safety_measures.ifEmpty { listOf("—") }
val QuestionnaireAnswer.willingTo: List<String> get() {
    val list = mutableListOf<String>()
    if (q5_ready_neuter) list.add("Стерилизация")
    if (q5_ready_recommendations) list.add("Рекомендации")
    if (q5_ready_tracker) list.add("Адресник")
    return list.ifEmpty { listOf("—") }
}
val QuestionnaireAnswer.maintainContact: String get() = when (q5_ready_keep_contact) {
    true -> "Да"
    false -> "Нет"
    null -> "—"
}

val QuestionnaireAnswer.responsibleOwner: String get() = q6_responsible_owner_meaning.ifBlank { "—" }
val QuestionnaireAnswer.lifeWithPet: String get() = q6_life_with_pet_vision.ifBlank { "—" }
val QuestionnaireAnswer.whyGoodOwner: String get() = q6_why_good_owner.ifBlank { "—" }