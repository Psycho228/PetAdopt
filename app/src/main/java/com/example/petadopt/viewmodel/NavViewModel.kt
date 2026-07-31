package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.petadopt.data.model.User
import com.example.petadopt.data.repository.QuestionnaireRepository
import com.example.petadopt.data.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow(StartDestination.LOADING)
    val startDestination: StateFlow<StartDestination> = _startDestination

    fun checkQuestionnaire() {
        viewModelScope.launch {
            // Ждем восстановления сессии (повторяем проверку несколько раз)
            repeat(5) { attempt ->
                delay(500)
                try {
                    val user = authRepository.getUser()
                    val userId = authRepository.currentUserId
                    
                    if (user == null || userId == null) {
                        Log.d(TAG, "User not logged in. Attempt $attempt")
                        _startDestination.update { StartDestination.AUTH }
                        return@launch
                    }
                    
                    // Проверяем роль пользователя
                    val isShelterOrAdmin = user.role == User.ROLE_SHELTER || user.role == User.ROLE_ADMIN
                    val isBreeder = user.role == User.ROLE_BREEDER
                    
                    if (isShelterOrAdmin) {
                        // Приюты и админы не проходят опросник, идут в shelter screen
                        Log.d(TAG, "User is shelter/admin. Going to SHELTER")
                        _startDestination.update { StartDestination.SHELTER }
                        return@launch
                    }

                    if (isBreeder) {
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
                val isBreeder = user?.role == User.ROLE_BREEDER
                
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
}
