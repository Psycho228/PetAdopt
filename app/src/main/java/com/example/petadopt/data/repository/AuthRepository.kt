package com.example.petadopt.data.repository

import com.example.petadopt.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserId: String?
    val isLoggedIn: Boolean
    val currentUser: StateFlow<User?>
    val currentSession: StateFlow<String?>

    suspend fun register(email: String, password: String, name: String): User
    suspend fun login(email: String, password: String): User
    suspend fun getUser(): User?
    suspend fun getUserRole(userId: String): String
    suspend fun isCurrentUserAdmin(): Boolean
    suspend fun isCurrentUserShelter(): Boolean
    suspend fun setUserRole(role: String)
    suspend fun updateUserProfile(name: String, email: String)
    suspend fun updateEmail(newEmail: String)
    suspend fun logout()
    suspend fun resetPassword(email: String)
}