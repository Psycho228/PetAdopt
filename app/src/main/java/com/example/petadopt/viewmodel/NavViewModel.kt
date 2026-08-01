package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.petadopt.data.model.User
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.BreederMarketplaceRepository
import com.example.petadopt.data.repository.QuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NavViewModel"

enum class StartDestination {
    LOADING,
    AUTH,
    QUESTIONNAIRE,
    SWIPE,
    SHELTER,
    BREEDER
}

@HiltViewModel
class NavViewModel @Inject constructor(
    private val repo: QuestionnaireRepository,
    private val authRepository: AuthRepository,
    private val breederRepository: BreederMarketplaceRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow(StartDestination.LOADING)
    val startDestination: StateFlow<StartDestination> = _startDestination

    fun checkQuestionnaire() {
        _startDestination.update { StartDestination.LOADING }
        viewModelScope.launch {
            // Ждем восстановления сессии (повторяем проверку несколько раз)
            repeat(5) { attempt ->
                delay(500)
                try {
                    val user = authRepository.getUser()
                    val userId = authRepository.currentUserId
                    
                    if (user == null || userId == null) {
                        Log.d(TAG, "User not logged in. Opening guest catalog")
                        _startDestination.update { StartDestination.SWIPE }
                        return@launch
                    }
                    
                    // Проверяем роль пользователя
                    val isShelterOrAdmin = user.role == User.ROLE_SHELTER || user.role == User.ROLE_ADMIN
                    if (isShelterOrAdmin) {
                        // Приюты и админы не проходят опросник, идут в shelter screen
                        Log.d(TAG, "User is shelter/admin. Going to SHELTER")
                        _startDestination.update { StartDestination.SHELTER }
                        return@launch
                    }

                    if (isBreederAccount(user)) {
                        Log.d(TAG, "User is breeder. Going to BREEDER")
                        _startDestination.update { StartDestination.BREEDER }
                        return@launch
                    }
                    
                    // Для обычных пользователей проверяем опросник
                    val answer = repo.getAnswers()
                    Log.d(TAG, "Attempt $attempt: answer = ${answer?.q1_full_name}")
                    if (answer != null) {
                        Log.d(TAG, "Questionnaire found! Going to SWIPE")
                        _startDestination.update { StartDestination.SWIPE }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Attempt $attempt error: ${e.message}")
                }
            }
            // Проверяем роль пользователя в последний раз
            try {
                val user = authRepository.getUser()
                val isShelterOrAdmin = user?.role == User.ROLE_SHELTER || user?.role == User.ROLE_ADMIN
                val isBreeder = user != null && !isShelterOrAdmin && isBreederAccount(user)
                
                if (isShelterOrAdmin) {
                    Log.d(TAG, "User is shelter/admin (final check). Going to SHELTER")
                    _startDestination.update { StartDestination.SHELTER }
                } else if (isBreeder) {
                    Log.d(TAG, "User is breeder (final check). Going to BREEDER")
                    _startDestination.update { StartDestination.BREEDER }
                } else {
                    // Если не удалось получить данные - показываем опросник
                    Log.d(TAG, "No questionnaire found. Going to QUESTIONNAIRE")
                    _startDestination.update { StartDestination.QUESTIONNAIRE }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Final check error: ${e.message}")
                _startDestination.update { StartDestination.QUESTIONNAIRE }
            }
        }
    }

    fun isAuthenticated(): Boolean = authRepository.isLoggedIn

    suspend fun getAccountRoute(): String {
        return try {
            val user = authRepository.getUser() ?: return "loading"
            when {
                user.isShelter() -> "shelter"
                isBreederAccount(user) -> "breeder_cabinet"
                else -> "account"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve account route: ${e.message}")
            "loading"
        }
    }

    suspend fun getPostLoginRoute(): String = when (val route = getAccountRoute()) {
        "account" -> "swipe"
        else -> route
    }

    private suspend fun isBreederAccount(user: User): Boolean {
        if (user.isBreeder()) return true

        return runCatching { breederRepository.getMyProfile() != null }
            .onFailure { error ->
                Log.w(TAG, "Failed to check breeder profile: ${error.message}")
            }
            .getOrDefault(false)
    }
}
