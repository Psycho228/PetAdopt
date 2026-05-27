package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.User
import com.example.petadopt.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountState(
    val user: User? = null,
    val questionnaire: com.example.petadopt.data.model.QuestionnaireAnswer? = null,
    val riskAssessment: com.example.petadopt.data.model.RiskAssessmentRecord? = null,
    val riskAssessmentHistory: List<com.example.petadopt.data.model.RiskAssessmentRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAdmin: Boolean = false,
    val isShelter: Boolean = false
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getQuestionnaireUseCase: GetQuestionnaireUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val isCurrentUserAdminUseCase: IsCurrentUserAdminUseCase,
    private val isCurrentUserShelterUseCase: IsCurrentUserShelterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getRiskAssessmentUseCase: GetRiskAssessmentUseCase,
    private val getRiskAssessmentHistoryUseCase: GetRiskAssessmentHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val user = getUserUseCase()
                val questionnaire = getQuestionnaireUseCase()
                val riskAssessment = getRiskAssessmentUseCase()
                val riskHistory = getRiskAssessmentHistoryUseCase()
                val isAdmin = isCurrentUserAdminUseCase()
                val isShelter = isCurrentUserShelterUseCase()

                _state.value = _state.value.copy(
                    user = user,
                    questionnaire = questionnaire,
                    riskAssessment = riskAssessment,
                    riskAssessmentHistory = riskHistory,
                    isAdmin = isAdmin,
                    isShelter = isShelter,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки данных: ${e.message}"
                )
            }
        }
    }

    fun updateUserProfile(name: String, email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                updateUserProfileUseCase(name, email)
                val updatedUser = _state.value.user?.copy(name = name, email = email)
                _state.value = _state.value.copy(
                    user = updatedUser,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка обновления профиля: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}