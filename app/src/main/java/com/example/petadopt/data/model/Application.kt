package com.example.petadopt.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Application(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val user_id: String = "",
    @SerialName("user_name") val user_name: String = "",
    @SerialName("user_email") val user_email: String = "",
    @SerialName("pet_id") val pet_id: String = "",
    @SerialName("pet_name") val pet_name: String = "",
    @SerialName("message") val message: String = "",
    @SerialName("contact_time") val contact_time: String = "",
    @SerialName("status") val status: String = "pending",
    @SerialName("created_at") val created_at: String? = null,
    @SerialName("updated_at") val updated_at: String? = null
) {
    // Удобные свойства для совместимости со старым кодом
    val userId: String get() = user_id
    val userName: String get() = user_name
    val userEmail: String get() = user_email
    val petId: String get() = pet_id
    val petName: String get() = pet_name
    val contactTime: String get() = contact_time
    val timestamp: Long get() = runCatching {
        // Пробуем parse как ISO строку (от Supabase), иначе как Unix timestamp
        java.time.Instant.parse(created_at).toEpochMilli()
    }.getOrNull() ?: runCatching { created_at?.toLongOrNull() }.getOrNull() ?: 0L
}