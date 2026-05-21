package com.example.petadopt.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Pet
import com.example.petadopt.domain.usecase.*
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.PetRepository
import com.example.petadopt.domain.usecase.DeleteImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pets: List<Pet> = emptyList(),
    val isSaveSuccessful: Boolean = false,
    val uploadingImages: Boolean = false,
    val uploadedImages: List<String> = emptyList(),
    val currentPet: Pet? = null,
    val existingImageUrls: List<String> = emptyList()
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val createPetUseCase: CreatePetUseCase,
    private val updatePetUseCase: UpdatePetUseCase,
    private val deletePetUseCase: DeletePetUseCase,
    private val getAllPetsUseCase: GetAllPetsUseCase,
    private val updatePetStatusUseCaseAdmin: UpdatePetStatusUseCaseAdmin,
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val authRepository: AuthRepository,
    private val getPetByIdUseCase: GetPetByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    fun loadPets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val pets = getAllPetsUseCase()
                _uiState.value = _uiState.value.copy(
                    pets = pets,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun loadPetById(petId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val pet = getPetByIdUseCase(petId)
                val existingPhotos = mutableListOf(pet?.photo_url ?: "").filter { it.isNotBlank() } + 
                                     (pet?.additional_photos ?: emptyList())
                _uiState.value = _uiState.value.copy(
                    currentPet = pet,
                    existingImageUrls = existingPhotos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun uploadImages(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadingImages = true, error = null)
            try {
                val urls = uploadImagesUseCase(context, uris)
                _uiState.value = _uiState.value.copy(
                    uploadedImages = urls,
                    uploadingImages = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    uploadingImages = false
                )
            }
        }
    }

    fun createPet(pet: Pet) {
        val userId = authRepository.currentUserId ?: run {
            _uiState.value = _uiState.value.copy(
                error = "Ошибка: пользователь не авторизован",
                isLoading = false
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSaveSuccessful = false)
            try {
                createPetUseCase(pet, userId)
                _uiState.value = _uiState.value.copy(
                    isSaveSuccessful = true,
                    isLoading = false
                )
                loadPets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updatePet(pet: Pet) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSaveSuccessful = false)
            try {
                updatePetUseCase(pet)
                _uiState.value = _uiState.value.copy(
                    isSaveSuccessful = true,
                    isLoading = false
                )
                loadPets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                deletePetUseCase(petId)
                _uiState.value = _uiState.value.copy(isLoading = false)
                loadPets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updatePetStatus(petId: String, status: String) {
        viewModelScope.launch {
            try {
                updatePetStatusUseCaseAdmin(petId, status)
                loadPets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(isSaveSuccessful = false)
    }

    fun clearUploadedImages() {
        _uiState.value = _uiState.value.copy(uploadedImages = emptyList())
    }

    fun clearCurrentPet() {
        _uiState.value = _uiState.value.copy(currentPet = null)
    }

    fun removeImage(index: Int) {
        val allImages = _uiState.value.existingImageUrls + _uiState.value.uploadedImages
        val (existingPart, uploadedPart) = allImages.partition { it in _uiState.value.existingImageUrls }
        
        if (index < _uiState.value.existingImageUrls.size) {
            // Удаляем из существующих фото
            _uiState.value = _uiState.value.copy(
                existingImageUrls = _uiState.value.existingImageUrls.filterIndexed { i, _ -> i != index }
            )
        } else {
            // Удаляем из новых загруженных фото
            val uploadedIndex = index - _uiState.value.existingImageUrls.size
            _uiState.value = _uiState.value.copy(
                uploadedImages = _uiState.value.uploadedImages.filterIndexed { i, _ -> i != uploadedIndex }
            )
        }
    }

    fun deleteImageFromServer(imageUrl: String) {
        viewModelScope.launch {
            try {
                deleteImageUseCase(imageUrl)
                _uiState.value = _uiState.value.copy(
                    existingImageUrls = _uiState.value.existingImageUrls.filter { it != imageUrl }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка удаления фото: ${e.message}")
            }
        }
    }
}
