package com.example.petadopt.viewmodel

// Класс для хранения ошибок валидации
data class ValidationError(
    val field: String,
    val message: String
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError>
)

data class QuestionnaireState(
    // Раздел 1: Основная информация
    val q1_full_name: String = "",
    val q1_age: String = "",
    val q1_city: String = "",
    val q1_occupation: String = "",
    val q1_phone: String = "",
    val q1_email: String = "",
    
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
    
    // Раздел 7: Желаемые виды животных
    val q7_desired_pets: List<String> = emptyList(),
    
    val isLoading: Boolean = false,
    val error: String? = null
)

// Расширения для удобного доступа к полям QuestionnaireState
val QuestionnaireState.name: String get() = q1_full_name
val QuestionnaireState.age: String get() = q1_age
val QuestionnaireState.city: String get() = q1_city
val QuestionnaireState.occupation: String get() = q1_occupation
val QuestionnaireState.phone: String get() = q1_phone
val QuestionnaireState.email: String get() = q1_email

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
    if (q4_understand_time) add("Время")
    if (q4_understand_attention) add("Внимание")
    if (q4_understand_training) add("Обучение")
    if (q4_understand_vet_care) add("Ветеринарная помощь")
}
val QuestionnaireState.readyForExpenses: List<String> get() = buildList {
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
val QuestionnaireState.willingToTrain: String get() = if (q4_ready_education) "Да" else ""
val QuestionnaireState.lifeChanges: String get() = q4_life_changes_plan
val QuestionnaireState.obstacles: String get() = q4_obstacles_next_year

val QuestionnaireState.safetyMeasures: List<String> get() = q5_safety_measures.ifEmpty { listOf("—") }
val QuestionnaireState.willingTo: List<String> 
    get() = buildList {
        if (q5_ready_neuter) add("Стерилизовать питомца (если нужно)")
        if (q5_ready_recommendations) add("Соблюдать рекомендации приюта")
        if (q5_ready_tracker) add("Использовать адресник и поводок")
    }
val QuestionnaireState.maintainContact: String get() = if (q5_ready_keep_contact) "Да" else ""

val QuestionnaireState.responsibleOwner: String get() = q6_responsible_owner_meaning
val QuestionnaireState.lifeWithPet: String get() = q6_life_with_pet_vision
val QuestionnaireState.whyGoodOwner: String get() = q6_why_good_owner

val QuestionnaireState.desiredPets: List<String> get() = q7_desired_pets.ifEmpty { listOf("—") }

// Функция валидации всех полей опросника
fun QuestionnaireState.validateSection(sectionIndex: Int): ValidationResult {
    val errors = mutableListOf<ValidationError>()
    
    when (sectionIndex) {
        0 -> { // Раздел 1: Основная информация
            if (q1_full_name.isBlank()) {
                errors.add(ValidationError("q1_full_name", "Введите ваше имя"))
            }
            if (q1_age.isBlank()) {
                errors.add(ValidationError("q1_age", "Введите ваш возраст"))
            } else if (q1_age.toIntOrNull() == null || q1_age.toInt() < 18 || q1_age.toInt() > 120) {
                errors.add(ValidationError("q1_age", "Возраст должен быть от 18 до 120"))
            }
            if (q1_city.isBlank()) {
                errors.add(ValidationError("q1_city", "Выберите город"))
            }
            if (q1_occupation.isBlank()) {
                errors.add(ValidationError("q1_occupation", "Введите вашу профессию"))
            }
            if (q1_phone.isBlank()) {
                errors.add(ValidationError("q1_phone", "Введите номер телефона"))
            } else if (!q1_phone.matches(Regex("""^\+?\d{10,15}$"""))) {
                errors.add(ValidationError("q1_phone", "Некорректный формат номера"))
            }
            if (q1_email.isBlank()) {
                errors.add(ValidationError("q1_email", "Введите email"))
            } else if (!q1_email.contains("@")) {
                errors.add(ValidationError("q1_email", "Некорректный формат email"))
            }
        }
        1 -> { // Раздел 2: Жилищные условия
            if (q2_housing_type.isBlank()) {
                errors.add(ValidationError("q2_housing_type", "Выберите тип жилья"))
            }
            if (q2_pets_allowed.isBlank()) {
                errors.add(ValidationError("q2_pets_allowed", "Укажите, разрешены ли животные"))
            }
            if (q2_living_with.isBlank()) {
                errors.add(ValidationError("q2_living_with", "Выберите, с кем вы живёте"))
            }
            if (q2_family_consent.isBlank()) {
                errors.add(ValidationError("q2_family_consent", "Укажите согласие семьи"))
            }
            if (q2_has_children.isBlank()) {
                errors.add(ValidationError("q2_has_children", "Укажите, есть ли дети"))
            } else if (q2_has_children == "Да" && q2_children_ages.isBlank()) {
                errors.add(ValidationError("q2_children_ages", "Укажите возраст детей"))
            }
            if (q2_has_other_pets.isBlank()) {
                errors.add(ValidationError("q2_has_other_pets", "Укажите, есть ли другие животные"))
            } else if (q2_has_other_pets == "Да" && q2_other_pets_types.isBlank()) {
                errors.add(ValidationError("q2_other_pets_types", "Укажите виды других животных"))
            }
            if (q2_hours_alone.isBlank()) {
                errors.add(ValidationError("q2_hours_alone", "Введите количество часов"))
            } else if (q2_hours_alone.toIntOrNull() == null || q2_hours_alone.toInt() < 0 || q2_hours_alone.toInt() > 24) {
                errors.add(ValidationError("q2_hours_alone", "Введите корректное количество часов (0-24)"))
            }
            if (q2_caregiver.isBlank()) {
                errors.add(ValidationError("q2_caregiver", "Укажите, кто будет ухаживать"))
            }
        }
        2 -> { // Раздел 3: Опыт с животными
            if (q3_had_pets_before.isBlank()) {
                errors.add(ValidationError("q3_had_pets_before", "Укажите, были ли у вас питомцы"))
            }
            if (q3_what_happened.isBlank() && q3_had_pets_before == "Да") {
                errors.add(ValidationError("q3_what_happened", "Опишите, что с прошлыми питомцами"))
            }
            if (q3_dog_experience.isBlank()) {
                errors.add(ValidationError("q3_dog_experience", "Укажите опыт с собаками"))
            }
            if (q3_cat_experience.isBlank()) {
                errors.add(ValidationError("q3_cat_experience", "Укажите опыт с кошками"))
            }
            if (q3_special_needs_experience.isBlank()) {
                errors.add(ValidationError("q3_special_needs_experience", "Укажите опыт с особенными животными"))
            }
            if (q3_why_now.isBlank()) {
                errors.add(ValidationError("q3_why_now", "Объясните, почему сейчас"))
            }
        }
        3 -> { // Раздел 4: Ответственность и готовность
            // Проверки на обязательность чекбоксов удалены - теперь они не обязательны
            
            if (q4_furniture_damage_plan.isBlank()) {
                errors.add(ValidationError("q4_furniture_damage_plan", "Опишите ваши действия"))
            }
            if (q4_noise_plan.isBlank()) {
                errors.add(ValidationError("q4_noise_plan", "Опишите ваши действия"))
            }
            if (q4_shy_pet_plan.isBlank()) {
                errors.add(ValidationError("q4_shy_pet_plan", "Опишите ваши действия"))
            }
            if (q4_long_adaptation_plan.isBlank()) {
                errors.add(ValidationError("q4_long_adaptation_plan", "Опишите ваши действия"))
            }
            if (q4_life_changes_plan.isBlank()) {
                errors.add(ValidationError("q4_life_changes_plan", "Опишите ваши действия"))
            }
            if (q4_obstacles_next_year.isBlank()) {
                errors.add(ValidationError("q4_obstacles_next_year", "Опишите возможные препятствия"))
            }
        }
        4 -> { // Раздел 5: Безопасность
            // Проверки на обязательность чекбоксов удалены - теперь они не обязательны
        }
        5 -> { // Раздел 6: Эмоциональная часть
            // Проверки на обязательность удалены - теперь эти поля не обязательны
        }
        6 -> { // Раздел 7: Желаемые виды животных
            if (q7_desired_pets.isEmpty()) {
                errors.add(ValidationError("q7_desired_pets", "Выберите хотя бы один вид животного"))
            }
        }
    }
    
    return ValidationResult(errors.isEmpty(), errors)
}

// Валидация всех разделов
fun QuestionnaireState.validateAllSections(): ValidationResult {
    val allErrors = mutableListOf<ValidationError>()
    for (i in 0..5) {
        val result = validateSection(i)
        allErrors.addAll(result.errors)
    }
    return ValidationResult(allErrors.isEmpty(), allErrors)
}