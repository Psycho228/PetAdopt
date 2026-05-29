package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _application = MutableStateFlow<Application?>(null)
    val application: StateFlow<Application?> = _application

    private val _pet = MutableStateFlow<Pet?>(null)
    val pet: StateFlow<Pet?> = _pet

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun loadApplicationAndPet(applicationId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val uid = authRepository.currentUserId ?: throw Exception("Пользователь не авторизован")
                
                // Получаем все заявки пользователя и ищем нужную
                val applications = petRepository.getUserApplications(uid)
                _application.value = applications.find { it.id == applicationId }
                
                // Получаем информацию о питомце
                _application.value?.let { app ->
                    val petData = petRepository.getPetById(app.petId)
                    _pet.value = petData
                }
            } catch (e: Exception) {
                // Обработка ошибки
            } finally {
                _loading.value = false
            }
        }
    }
}
