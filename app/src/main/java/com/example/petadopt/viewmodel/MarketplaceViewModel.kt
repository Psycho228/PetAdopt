package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.BreederProfile
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.data.repository.BreederMarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceState(
    val listings: List<SaleListing> = emptyList(),
    val selectedListing: SaleListing? = null,
    val selectedBreeder: BreederProfile? = null,
    val myProfile: BreederProfile? = null,
    val myListings: List<SaleListing> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: BreederMarketplaceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MarketplaceState())
    val state: StateFlow<MarketplaceState> = _state.asStateFlow()

    fun loadCatalog() = launchLoading {
        _state.value = _state.value.copy(listings = repository.getAvailableListings())
    }

    fun loadListing(listingId: String) = launchLoading {
        val listing = repository.getListing(listingId)
        _state.value = _state.value.copy(
            selectedListing = listing,
            selectedBreeder = listing?.let { repository.getBreederProfile(it.breederId) }
        )
    }

    fun loadCabinet() = launchLoading {
        _state.value = _state.value.copy(
            myProfile = repository.getMyProfile(),
            myListings = repository.getMyListings()
        )
    }

    fun saveProfile(profile: BreederProfile) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, saved = false)
            runCatching { repository.saveProfile(profile) }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false, saved = true)
                    loadCabinet()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = error.message ?: "Не удалось сохранить профиль"
                    )
                }
        }
    }

    fun saveListing(listing: SaleListing) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, saved = false)
            runCatching { repository.saveListing(listing) }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false, saved = true)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = error.message ?: "Не удалось сохранить объявление"
                    )
                }
        }
    }

    fun updateListingStatus(listingId: String, status: String) {
        viewModelScope.launch {
            runCatching { repository.updateListingStatus(listingId, status) }
                .onSuccess { loadCabinet() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Не удалось изменить статус"
                    )
                }
        }
    }

    fun consumeSaved() {
        _state.value = _state.value.copy(saved = false)
    }

    private fun launchLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { block() }
                .onSuccess { _state.value = _state.value.copy(isLoading = false) }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить данные"
                    )
                }
        }
    }
}
