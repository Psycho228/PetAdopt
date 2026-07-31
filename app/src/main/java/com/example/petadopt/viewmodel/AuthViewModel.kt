package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.BreederProfile
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.BreederMarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val kennelName: String = "",
    val breederCity: String = "",
    val breederPhone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val breederRepository: BreederMarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    val isLoggedIn: Boolean get() = repo.isLoggedIn

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onKennelNameChange(value: String) = _state.update { it.copy(kennelName = value, error = null) }
    fun onBreederCityChange(value: String) = _state.update { it.copy(breederCity = value, error = null) }
    fun onBreederPhoneChange(value: String) = _state.update { it.copy(breederPhone = value, error = null) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        if (!validate(requireName = false)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.login(email = s.email.trim(), password = s.password)
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val s = _state.value
        if (!validate(requireName = true)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.register(
                    email = s.email.trim(),
                    password = s.password,
                    name = s.name.trim()
                )
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loginBreeder(onSuccess: () -> Unit) {
        val s = _state.value
        if (!validate(requireName = false)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.login(email = s.email.trim(), password = s.password)
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun registerBreeder(onSuccess: () -> Unit) {
        val s = _state.value
        if (!validate(requireName = true) || !validateBreederProfile()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.register(
                    email = s.email.trim(),
                    password = s.password,
                    name = s.name.trim()
                )
                breederRepository.saveProfile(
                    BreederProfile(
                        kennelName = s.kennelName.trim(),
                        city = s.breederCity.trim(),
                        phone = s.breederPhone.trim()
                    )
                )
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun validateBreederProfile(): Boolean {
        val s = _state.value
        val error = when {
            s.kennelName.isBlank() -> "Введите название питомника"
            s.breederCity.isBlank() -> "Введите город"
            s.breederPhone.isBlank() -> "Введите телефон"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return false
        }
        return true
    }

    private fun validate(requireName: Boolean): Boolean {
        val s = _state.value
        return when {
            requireName && s.name.isBlank() -> {
                _state.update { it.copy(error = "Введите имя") }
                false
            }
            s.email.isBlank() || !s.email.contains("@") -> {
                _state.update { it.copy(error = "Введите корректный email") }
                false
            }
            s.password.length < 6 -> {
                _state.update { it.copy(error = "Пароль минимум 6 символов") }
                false
            }
            else -> true
        }
    }
}
