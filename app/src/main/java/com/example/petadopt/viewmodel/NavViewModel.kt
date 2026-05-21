package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
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
    QUESTIONNAIRE,
    SWIPE
}

@HiltViewModel
class NavViewModel @Inject constructor(
    private val repo: QuestionnaireRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow(StartDestination.LOADING)
    val startDestination: StateFlow<StartDestination> = _startDestination

    fun checkQuestionnaire() {
        viewModelScope.launch {
            // Ждем восстановления сессии (повторяем проверку несколько раз)
            repeat(5) { attempt ->
                delay(500)
                try {
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
            // Если не удалось получить данные - показываем опросник
            Log.d(TAG, "No questionnaire found. Going to QUESTIONNAIRE")
            _startDestination.update { StartDestination.QUESTIONNAIRE }
        }
    }
}
