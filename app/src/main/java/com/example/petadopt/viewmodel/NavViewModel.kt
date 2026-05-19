package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.repository.QuestionnaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            try {
                val answer = repo.getAnswers()
                _startDestination.update {
                    if (answer != null) StartDestination.SWIPE else StartDestination.QUESTIONNAIRE
                }
            } catch (_: Exception) {
                _startDestination.update { StartDestination.QUESTIONNAIRE }
            }
        }
    }
}
