package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApplicationsState(
    val applications: List<Application> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ApplicationsState())
    val state: StateFlow<ApplicationsState> = _state.asStateFlow()

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val uid = authRepository.currentUserId ?: throw Exception("Пользователь не авторизован")
                val applications = petRepository.getUserApplications(uid)
                _state.value = _state.value.copy(
                    applications = applications,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки заявок: ${e.message}"
                )
            }
        }
    }

    fun getFormattedTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }

    fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "В ожидании"
            "approved" -> "Одобрено"
            "rejected" -> "Отклонено"
            else -> status
        }
    }

    fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
        return when (status) {
            "pending" -> androidx.compose.ui.graphics.Color(0xFFFFA726)
            "approved" -> androidx.compose.ui.graphics.Color(0xFF66BB6A)
            "rejected" -> androidx.compose.ui.graphics.Color(0xFFEF5350)
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}
