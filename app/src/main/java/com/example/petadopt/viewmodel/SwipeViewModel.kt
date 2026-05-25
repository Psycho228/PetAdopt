package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.Pet
import com.example.petadopt.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val getPetsByTypeUseCase: GetPetsByTypeUseCase,
    private val searchPetsUseCase: SearchPetsUseCase,
    private val filterPetsUseCase: FilterPetsUseCase,
    private val getLikedPetsUseCase: GetLikedPetsUseCase,
    private val getAppliedPetIdsUseCase: GetAppliedPetIdsUseCase,
    private val likePetUseCase: LikePetUseCase,
    private val unlikePetUseCase: UnlikePetUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getPetByIdUseCase: GetPetByIdUseCase
) : ViewModel() {

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    private val _applicationPetId = MutableStateFlow<String?>(null)
    private val _applicationPetName = MutableStateFlow<String?>(null)

    private val _likedPets = MutableStateFlow<List<Pet>>(emptyList())
    val likedPets: StateFlow<List<Pet>> = _likedPets.asStateFlow()

    private val _seenPetIds = mutableSetOf<String>()
    private val _appliedPetIds = mutableSetOf<String>()

    init {
        loadLikedPets()
        loadAppliedPets()
    }

    private fun loadAppliedPets() {
        viewModelScope.launch {
            val uid = getCurrentUserIdUseCase() ?: return@launch
            try {
                val appliedIds = getAppliedPetIdsUseCase(uid)
                _appliedPetIds.clear()
                _appliedPetIds.addAll(appliedIds)
            } catch (_: Exception) { }
        }
    }

    fun loadLikedPets() {
        viewModelScope.launch {
            val uid = getCurrentUserIdUseCase() ?: return@launch
            try {
                val (liked, appliedIds) = kotlin.run {
                    val likedResult = getLikedPetsUseCase(uid)
                    val appliedResult = getAppliedPetIdsUseCase(uid)
                    Pair(likedResult, appliedResult)
                }
                val filteredLiked = liked.filter { it.id !in appliedIds }
                _likedPets.value = filteredLiked
                _seenPetIds.clear()
                _seenPetIds.addAll(filteredLiked.map { it.id })
                _appliedPetIds.clear()
                _appliedPetIds.addAll(appliedIds)
                
                val allPets = getPetsUseCase()
                _pets.value = allPets.filter { it.id !in _seenPetIds && it.id !in _appliedPetIds }
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun loadPetById(petId: String) {
        viewModelScope.launch {
            try {
                val pet = getPetByIdUseCase(petId)
                if (pet != null) {
                    _selectedPet.value = pet
                }
            } catch (_: Exception) { }
        }
    }

    fun loadPets() {
        viewModelScope.launch {
            try {
                val allPets = getPetsUseCase()
                val filtered = allPets.filter { it.id !in _seenPetIds && it.id !in _appliedPetIds }
                _pets.value = filtered
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun refreshPets() {
        viewModelScope.launch {
            try {
                val uid = getCurrentUserIdUseCase() ?: return@launch
                val (liked, appliedIds) = kotlin.run {
                    val likedResult = getLikedPetsUseCase(uid)
                    val appliedResult = getAppliedPetIdsUseCase(uid)
                    Pair(likedResult, appliedResult)
                }
                _likedPets.value = liked.filter { it.id !in appliedIds }
                _seenPetIds.clear()
                _seenPetIds.addAll(liked.map { it.id })
                _appliedPetIds.clear()
                _appliedPetIds.addAll(appliedIds)

                val allPets = getPetsUseCase()
                val filtered = allPets.filter { it.id !in _seenPetIds && it.id !in _appliedPetIds }
                _pets.value = filtered
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
    }

    fun prepareApplication(pet: Pet) {
        _selectedPet.value = pet
        _applicationPetId.value = pet.id
        _applicationPetName.value = pet.name
    }

    fun getApplicationPetId(): String? = _applicationPetId.value
    fun getApplicationPetName(): String? = _applicationPetName.value

    fun likePet(pet: Pet) {
        _seenPetIds.add(pet.id)
        val uid = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            try {
                likePetUseCase(uid, pet.id)
                _likedPets.update { current ->
                    if (current.none { it.id == pet.id }) current + pet else current
                }
            } catch (_: Exception) { }
        }
        _pets.update { it.filter { p -> p.id != pet.id } }
        if (_pets.value.isEmpty()) {
            _currentIndex.value = 0
        }
    }

    fun dislikePet() {
        val pet = _pets.value.getOrNull(_currentIndex.value) ?: return
        _seenPetIds.add(pet.id)
        _pets.update { it.filter { p -> p.id != pet.id } }
        if (_pets.value.isEmpty()) {
            _currentIndex.value = 0
        }
    }

    fun removeLike(pet: Pet) {
        val uid = getCurrentUserIdUseCase() ?: return
        viewModelScope.launch {
            try {
                unlikePetUseCase(uid, pet.id)
                _likedPets.update { current ->
                    current.filter { it.id != pet.id }
                }
                if (pet.id !in _appliedPetIds) {
                    _seenPetIds.remove(pet.id)
                    val allPets = getPetsUseCase()
                    _pets.update { current ->
                        if (current.none { p -> p.id == pet.id }) current + pet else current
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun filterByType(type: String) {
        viewModelScope.launch {
            try {
                val filtered = getPetsByTypeUseCase(type)
                val uid = getCurrentUserIdUseCase() ?: return@launch
                val appliedIds = getAppliedPetIdsUseCase(uid)
                _pets.value = filtered.filter { it.id !in _seenPetIds && it.id !in appliedIds }
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val results = searchPetsUseCase(query)
                val uid = getCurrentUserIdUseCase() ?: return@launch
                val appliedIds = getAppliedPetIdsUseCase(uid)
                _pets.value = results.filter { it.id !in _seenPetIds && it.id !in appliedIds }
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun applyAdvancedFilter(
        type: String? = null,
        gender: String? = null,
        size: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        isVaccinated: Boolean? = null,
        isSterilized: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                val filtered = filterPetsUseCase(
                    type = type,
                    gender = gender,
                    size = size,
                    minAge = minAge,
                    maxAge = maxAge,
                    isVaccinated = isVaccinated,
                    isSterilized = isSterilized
                )
                val uid = getCurrentUserIdUseCase() ?: return@launch
                val appliedIds = getAppliedPetIdsUseCase(uid)
                _pets.value = filtered.filter { it.id !in _seenPetIds && it.id !in appliedIds }
                _currentIndex.value = 0
            } catch (_: Exception) { }
        }
    }

    fun resetFilters() {
        loadPets()
    }
}
