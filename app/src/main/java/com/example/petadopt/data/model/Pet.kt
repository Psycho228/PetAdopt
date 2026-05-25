package com.example.petadopt.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    @SerialName("id") val id: String = "",
    @SerialName("shelter_id") val shelter_id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("age") val age: Int = 0,
    @SerialName("type") val type: String = "cat",
    @SerialName("gender") val gender: String = "male",
    @SerialName("size") val size: String = "medium",
    @SerialName("description") val description: String = "",
    @SerialName("photo_url") val photo_url: String = "",
    @SerialName("additional_photos") val additional_photos: List<String>? = null,
    @SerialName("breed") val breed: String = "",
    @SerialName("color") val color: String = "",
    @SerialName("weight") val weight: Double? = null,
    @SerialName("is_neutered") val is_neutered: Boolean = false,
    @SerialName("has_vaccination") val has_vaccination: Boolean = false,
    @SerialName("is_active") val is_active: Boolean = true,
    @SerialName("traits") val traits: List<String>? = null,
    @SerialName("created_at") val created_at: String? = null,
    @SerialName("updated_at") val updated_at: String? = null
) {
    // Удобные свойства для отображения (совместимость со старым кодом)
    val imageUrl: String get() = photo_url
    val images: List<String> get() = additional_photos ?: emptyList()
    val petTraits: List<String> get() = traits ?: emptyList()
    val isVaccinated: Boolean get() = has_vaccination
    val isSterilized: Boolean get() = is_neutered
    val isHouseTrained: Boolean get() = true // Пока не в БД
    val goodWithKids: Boolean get() = true // Пока не в БД
    val goodWithPets: Boolean get() = true // Пока не в БД
    val energyLevel: String get() = "medium" // Пока не в БД
    val specialNeeds: String get() = "" // Пока не в БД
    val location: String get() = "" // Пока не в БД
    val shelterName: String get() = "" // Пока не в БД
    val shelterContact: String get() = "" // Пока не в БД
    val addedAt: Long get() = created_at?.toLongOrNull() ?: 0L
    val status: String get() = if (is_active) STATUS_AVAILABLE else STATUS_ADOPTED
    val ageYears: Any get() = age
    val ageYearsInt: Int get() = age

    companion object {
        const val TYPE_CAT = "cat"
        const val TYPE_DOG = "dog"
        const val TYPE_BIRD = "bird"
        const val TYPE_OTHER = "other"
        
        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"
        
        const val SIZE_SMALL = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE = "large"
        
        const val STATUS_AVAILABLE = "available"
        const val STATUS_PENDING = "pending"
        const val STATUS_ADOPTED = "adopted"
    }
    
    fun getTypeDisplay(): String = when (type) {
        TYPE_CAT -> "Кошка"
        TYPE_DOG -> "Собака"
        TYPE_BIRD -> "Птица"
        else -> "Другое"
    }
    
    fun getGenderDisplay(): String = when (gender) {
        GENDER_MALE -> "Мальчик"
        GENDER_FEMALE -> "Девочка"
        else -> "Не указано"
    }
    
    fun getSizeDisplay(): String = when (size) {
        SIZE_SMALL -> "Маленький"
        SIZE_MEDIUM -> "Средний"
        SIZE_LARGE -> "Большой"
        else -> "Не указано"
    }
    
    fun getEnergyLevelDisplay(): String = when (energyLevel) {
        "low" -> "Низкая"
        "medium" -> "Средняя"
        "high" -> "Высокая"
        else -> "Не указано"
    }
    
    fun getStatusDisplay(): String = when (status) {
        STATUS_AVAILABLE -> "Доступен"
        STATUS_PENDING -> "На рассмотрении"
        STATUS_ADOPTED -> "Найден дом"
        else -> status
    }
}
