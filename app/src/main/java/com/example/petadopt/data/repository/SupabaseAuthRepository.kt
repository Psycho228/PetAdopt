package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.data.model.User
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepository @Inject constructor(
) : AuthRepository {
    private val auth: Auth = SupabaseConfig.auth
    private val postgrest: Postgrest = SupabaseConfig.postgrest
    
    companion object {
        private const val TABLE_USERS = "users"
        private const val TAG = "SupabaseAuthRepository"
    }

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _sessionFlow = MutableStateFlow<String?>(null)
    override val currentSession: StateFlow<String?> = _sessionFlow.asStateFlow()

    override val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override val isLoggedIn: Boolean
        get() = auth.currentUserOrNull() != null

    private fun usersTable() = postgrest.from(TABLE_USERS)

    init {
        // Инициализация без асинхронных операций
    }

    private suspend fun checkCurrentSession() {
        try {
            if (isLoggedIn) {
                val userId = auth.currentUserOrNull()?.id
                _sessionFlow.value = userId
                if (userId != null) {
                    loadUserProfile(userId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session: ${e.message}")
        }
    }

    override suspend fun register(email: String, password: String, name: String): User {
        try {
            // Регистрируем пользователя через Supabase Auth
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("name", name)
                }
            }

            // Даем небольшую задержку для инициализации сессии
            kotlinx.coroutines.delay(500)

            // Получаем текущего пользователя из сессии
            val userId = auth.currentUserOrNull()?.id 
                ?: throw Exception("Ошибка получения ID пользователя после регистрации. Проверьте настройки подтверждения email в Supabase.")

            val newUser = User(
                id = userId,
                email = email,
                name = name,
                role = User.ROLE_USER
            )

            // Сохраняем пользователя в таблицу users
            usersTable().upsert(buildJsonObject {
                put("id", userId)
                put("email", email)
                put("name", name)
                // role добавляется триггером или вручную в БД
            })

            _currentUserFlow.value = newUser
            _sessionFlow.value = userId

            Log.d(TAG, "User registered successfully: $userId")
            return newUser
        } catch (e: Exception) {
            Log.e(TAG, "Error during registration: ${e.message}")
            throw Exception("Ошибка регистрации: ${e.message}")
        }
    }

    override suspend fun login(email: String, password: String): User {
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = auth.currentUserOrNull()?.id 
                ?: throw Exception("Ошибка получения ID пользователя")
            val user = loadUserProfile(userId)
            _sessionFlow.value = userId
            
            Log.d(TAG, "User logged in successfully: $userId")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Error during login: ${e.message}")
            throw Exception("Ошибка входа: ${e.message}")
        }
    }

    private suspend fun loadUserProfile(userId: String): User {
        return try {
            val response = usersTable()
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()
            
            _currentUserFlow.value = response
            Log.d(TAG, "Profile loaded for user: $userId")
            response
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error loading profile: ${e.message}")
            throw Exception("Ошибка загрузки профиля: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile: ${e.message}")
            throw Exception("Ошибка загрузки профиля: ${e.message}")
        }
    }

    override suspend fun getUser(): User? {
        val userId = currentUserId ?: return null
        return try {
            loadUserProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: ${e.message}")
            null
        }
    }

    override suspend fun getUserRole(userId: String): String {
        return try {
            val user = usersTable()
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<User>()
            
            user?.role ?: User.ROLE_USER
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error getting user role: ${e.message}")
            User.ROLE_USER
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user role: ${e.message}")
            User.ROLE_USER
        }
    }

    override suspend fun isCurrentUserAdmin(): Boolean {
        val user = getUser()
        return user?.role == User.ROLE_ADMIN
    }

    override suspend fun isCurrentUserShelter(): Boolean {
        val user = getUser()
        return user?.role == User.ROLE_SHELTER
    }

    override suspend fun setUserRole(role: String) {
        val userId = currentUserId ?: throw Exception("Пользователь не авторизован")
        try {
            usersTable()
                .update(buildJsonObject { put("role", role) }) {
                    filter { eq("id", userId) }
                }
            // Обновляем локальный кэш
            _currentUserFlow.value = _currentUserFlow.value?.copy(role = role)
            Log.d(TAG, "User role updated: $userId -> $role")
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error setting user role: ${e.message}")
            throw Exception("Ошибка сети при обновлении роли: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user role: ${e.message}")
            throw Exception("Ошибка обновления роли: ${e.message}")
        }
    }

    override suspend fun updateUserProfile(name: String, email: String) {
        val userId = currentUserId ?: throw Exception("Пользователь не авторизован")
        try {
            // Обновляем данные в Auth (name через user_metadata)
            auth.updateUser {
                data = buildJsonObject {
                    put("name", name)
                }
            }

            // Обновляем в таблице users
            usersTable()
                .update(buildJsonObject {
                    put("name", name)
                    put("email", email)
                }) {
                    filter { eq("id", userId) }
                }

            _currentUserFlow.value = _currentUserFlow.value?.copy(
                name = name,
                email = email
            )

            Log.d(TAG, "Profile updated for user: $userId")
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error updating profile: ${e.message}")
            throw Exception("Ошибка сети при обновлении профиля: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile: ${e.message}")
            throw Exception("Ошибка обновления профиля: ${e.message}")
        }
    }

    override suspend fun updateEmail(newEmail: String) {
        val userId = currentUserId ?: throw Exception("Пользователь не авторизован")
        try {
            auth.updateUser {
                email = newEmail
            }

            // Обновляем email в таблице users
            usersTable()
                .update(buildJsonObject { put("email", newEmail) }) {
                    filter { eq("id", userId) }
                }

            Log.d(TAG, "Email update requested for user: $userId -> $newEmail")
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error updating email: ${e.message}")
            throw Exception("Ошибка сети при обновлении email: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating email: ${e.message}")
            throw Exception("Ошибка обновления email: ${e.message}")
        }
    }

    override suspend fun logout() {
        try {
            auth.signOut()
            _currentUserFlow.value = null
            _sessionFlow.value = null
            Log.d(TAG, "User logged out")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
        }
    }

    override suspend fun resetPassword(email: String) {
        try {
            auth.resetPasswordForEmail(email, "https://your-app.com/reset-password")
            Log.d(TAG, "Password reset email sent to: $email")
        } catch (e: HttpRequestException) {
            Log.e(TAG, "HTTP error sending password reset email: ${e.message}")
            throw Exception("Ошибка сети при отправке письма: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset email: ${e.message}")
            throw Exception("Ошибка отправки письма для сброса пароля: ${e.message}")
        }
    }
}