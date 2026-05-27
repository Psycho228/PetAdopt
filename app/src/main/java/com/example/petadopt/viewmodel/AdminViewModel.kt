package com.example.petadopt.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.RiskAssessmentRecord
import com.example.petadopt.domain.usecase.*
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.PetRepository
import com.example.petadopt.data.repository.QuestionnaireRepository
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
    val filteredPets: List<Pet> = emptyList(),
    val searchQuery: String = "",
    val isSaveSuccessful: Boolean = false,
    val uploadingImages: Boolean = false,
    val uploadedImages: List<String> = emptyList(),
    val currentPet: Pet? = null,
    val existingImageUrls: List<String> = emptyList(),
    val shelterName: String = "",
    val isShelterAdmin: Boolean = false,
    val isAdminRole: Boolean = false,
    val applicationsByPet: Map<String, List<Application>> = emptyMap(),
    val currentQuestionnaire: QuestionnaireAnswer? = null,
    val currentRiskAssessment: RiskAssessmentRecord? = null
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
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val getPetsByShelterUseCase: GetPetsByShelterUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val searchPetsUseCase: SearchPetsUseCase,
    private val getApplicationsForPetUseCase: GetApplicationsForPetUseCase,
    private val autoAcceptApplicationUseCase: AutoAcceptApplicationUseCase,
    private val questionnaireRepository: QuestionnaireRepository,
    private val updateApplicationStatusUseCase: UpdateApplicationStatusUseCase,
    private val getRiskAssessmentUseCase: GetRiskAssessmentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    private fun loadShelterInfo() {
        viewModelScope.launch {
            val user = getUserUseCase()
            _uiState.value = _uiState.value.copy(
                shelterName = user?.name ?: "Приют",
                isShelterAdmin = user?.isShelter() == true,
                isAdminRole = user?.isAdmin() == true
            )
        }
    }

    fun loadPets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                loadShelterInfo()
                val currentRole = _uiState.value.isAdminRole
                val pets = if (currentRole) {
                    // Admin видит всех питомцев
                    getAllPetsUseCase()
                } else {
                    // Shelter видит только своих
                    val shelterId = authRepository.currentUserId ?: ""
                    getPetsByShelterUseCase(shelterId)
                }
                _uiState.value = _uiState.value.copy(
                    pets = pets,
                    filteredPets = pets,
                    isLoading = false
                )
                // Загружаем заявки для всех питомцев
                loadApplicationsForPets(pets)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun loadApplicationsForPets(pets: List<Pet>) {
        viewModelScope.launch {
            val applicationsMap = mutableMapOf<String, List<Application>>()
            for (pet in pets) {
                try {
                    val applications = getApplicationsForPetUseCase(pet.id ?: continue)
                    applicationsMap[pet.id] = applications
                } catch (e: Exception) {
                    android.util.Log.e("AdminViewModel", "Error loading applications for pet ${pet.id}: ${e.message}")
                }
            }
            _uiState.value = _uiState.value.copy(applicationsByPet = applicationsMap)
        }
    }

    fun autoAcceptApplications(petId: String) {
        viewModelScope.launch {
            val currentRole = _uiState.value.isAdminRole
            val shelterId = if (!currentRole) authRepository.currentUserId else null
            
            // Получаем все заявки на питомца
            val applications = _uiState.value.applicationsByPet[petId] ?: emptyList()
            
            // Авто-принимаем только pending заявки от пользователей своего приюта (если это shelter)
            for (application in applications) {
                if (application.status == "pending") {
                    // Если это shelter, проверяем, что заявка от пользователя (не админ)
                    if (!currentRole && shelterId != null) {
                        // Проверяем, что пользователь не из другого приюта (опционально)
                        autoAcceptApplicationUseCase(application.id)
                    } else {
                        // Admin принимает все заявки
                        autoAcceptApplicationUseCase(application.id)
                    }
                }
            }
            // Перезагружаем заявки
            loadApplicationsForPets(_uiState.value.pets)
        }
    }

    fun searchPets(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query)
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    filteredPets = _uiState.value.pets
                )
                return@launch
            }
            
            try {
                val searchResults = searchPetsUseCase(query)
                // Фильтруем по текущим правам доступа
                val filtered = if (_uiState.value.isAdminRole) {
                    searchResults
                } else {
                    val shelterId = _uiState.value.pets.firstOrNull()?.shelter_id ?: ""
                    searchResults.filter { it.shelter_id == shelterId }
                }
                _uiState.value = _uiState.value.copy(filteredPets = filtered)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
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
                android.util.Log.d("AdminViewModel", "Updating pet: id=${pet.id}, shelter_id=${pet.shelter_id}")
                updatePetUseCase(pet)
                _uiState.value = _uiState.value.copy(
                    isSaveSuccessful = true,
                    isLoading = false
                )
                loadPets()
            } catch (e: Exception) {
                android.util.Log.e("AdminViewModel", "Error updating pet: ${e.message}", e)
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

    fun loadQuestionnaireForUser(userId: String) {
        viewModelScope.launch {
            // Сбрасываем предыдущий опросник и оценку рисков перед загрузкой новых
            _uiState.value = _uiState.value.copy(currentQuestionnaire = null, currentRiskAssessment = null)
            try {
                val questionnaire = questionnaireRepository.getQuestionnaireByUserId(userId)
                _uiState.value = _uiState.value.copy(currentQuestionnaire = questionnaire)
                
                // Загружаем оценку рисков для этого пользователя (не для текущего!)
                if (questionnaire != null) {
                    // Используем репозиторий напрямую с правильным userId
                    val riskAssessment = questionnaireRepository.getLatestRiskAssessmentByUserId(userId)
                    _uiState.value = _uiState.value.copy(currentRiskAssessment = riskAssessment)
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminViewModel", "Error loading questionnaire for user $userId: ${e.message}")
            }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: String) {
        viewModelScope.launch {
            try {
                updateApplicationStatusUseCase(applicationId, status)
                // Перезагружаем заявки для всех питомцев
                loadPets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка обновления статуса: ${e.message}")
            }
        }
    }
}
