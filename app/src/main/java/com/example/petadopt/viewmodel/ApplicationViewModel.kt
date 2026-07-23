package com.example.petadopt.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_PET_ID = "petId"
private const val KEY_PET_NAME = "petName"

data class ApplicationState(
    val userName: String = "",
    val userEmail: String = "",
    val petName: String = "",
    val petId: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val petRepository: PetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ApplicationState())
    val state: StateFlow<ApplicationState> = _state

    init {
        loadUserData()
        // Получаем данные питомца из SavedStateHandle
        savedStateHandle.get<String>(KEY_PET_ID)?.let { petId ->
            savedStateHandle.get<String>(KEY_PET_NAME)?.let { petName ->
                setPetData(petId, petName)
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val user = authRepo.getUser()
                _state.update {
                    it.copy(
                        userName = user?.name ?: "Неизвестно",
                        userEmail = user?.email ?: "Нет email"
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(userName = "Неизвестно", userEmail = "Нет email") }
            }
        }
    }

    fun setPetData(petId: String, petName: String) {
        _state.update { it.copy(petName = petName, petId = petId) }
        savedStateHandle[KEY_PET_ID] = petId
        savedStateHandle[KEY_PET_NAME] = petName
    }

    fun setPetName(name: String) {
        _state.update { it.copy(petName = name) }
    }

    fun setPetId(id: String) {
        _state.update { it.copy(petId = id) }
    }

    fun submitApplication(
        message: String,
        contactTime: String,
        contactDays: String,
        onSuccess: () -> Unit
    ) {
        val uid = authRepo.currentUserId ?: run {
            _state.update { it.copy(error = "Ошибка: пользователь не авторизован") }
            return
        }
        val petId = _state.value.petId
        if (petId.isBlank()) {
            _state.update { it.copy(error = "Ошибка: питомец не выбран") }
            return
        }
        if (message.isBlank()) {
            _state.update { it.copy(error = "Введите сообщение") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            try {
                val application = Application(
                    user_id = uid,
                    user_name = _state.value.userName,
                    user_email = _state.value.userEmail,
                    pet_id = petId,
                    pet_name = _state.value.petName,
                    message = message,
                    contact_time = contactTime,
                    contact_days = contactDays,
                    status = "pending",
                    created_at = java.time.Instant.now().toString()
                )

                petRepository.submitApplication(application)

                _state.update { it.copy(isSubmitting = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, error = "Ошибка: ${e.message}") }
            }
        }
    }
}
