package com.example.petadopt.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String? = null,
    val city: String? = null,
    val avatar_url: String? = null,
    val role: String = "user", // "user", "shelter", "admin"
    val created_at: String? = null,
    val updated_at: String? = null
) {
    // Удобные свойства для совместимости со старым кодом
    val uid: String get() = id
    val createdAt: Long get() = created_at?.toLongOrNull() ?: 0L

    companion object {
        const val ROLE_USER = "user"
        const val ROLE_SHELTER = "shelter"
        const val ROLE_ADMIN = "admin"
    }

    fun isAdmin(): Boolean = role == ROLE_ADMIN
    fun isShelter(): Boolean = role == ROLE_SHELTER || role == ROLE_ADMIN
    fun canManagePets(): Boolean = isShelter() || isAdmin()
}