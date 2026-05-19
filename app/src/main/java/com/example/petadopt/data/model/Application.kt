package com.example.petadopt.data.model

data class Application(
    val id: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val user_email: String = "",
    val pet_id: String = "",
    val pet_name: String = "",
    val message: String = "",
    val contact_time: String = "",
    val status: String = "pending", // pending, approved, rejected
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
) {
    // Удобные свойства для совместимости со старым кодом
    val userId: String get() = user_id
    val userName: String get() = user_name
    val userEmail: String get() = user_email
    val petId: String get() = pet_id
    val petName: String get() = pet_name
    val contactTime: String get() = contact_time
    val timestamp: Long get() = created_at
}