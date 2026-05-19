package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.data.model.Pet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val petRepository: PetRepository
) {
    companion object {
        private const val TAG = "AdminRepository"
    }

    suspend fun createPet(pet: Pet, userId: String): String {
        return try {
            val petId = petRepository.createPet(pet, userId)
            Log.d(TAG, "Pet created: $petId")
            petId
        } catch (e: Exception) {
            throw Exception("Ошибка создания питомца: ${e.message}")
        }
    }

    suspend fun updatePet(pet: Pet) {
        try {
            petRepository.updatePet(pet)
            Log.d(TAG, "Pet updated: ${pet.id}")
        } catch (e: Exception) {
            throw Exception("Ошибка обновления питомца: ${e.message}")
        }
    }

    suspend fun deletePet(petId: String) {
        try {
            petRepository.deletePet(petId)
            Log.d(TAG, "Pet deleted: $petId")
        } catch (e: Exception) {
            throw Exception("Ошибка удаления питомца: ${e.message}")
        }
    }

    suspend fun getAllPets(): List<Pet> {
        return try {
            val pets = petRepository.getPets()
            Log.d(TAG, "Found ${pets.size} total pets")
            pets
        } catch (e: Exception) {
            Log.e(TAG, "Error in getAllPets: ${e.message}")
            emptyList()
        }
    }

    suspend fun updatePetStatus(petId: String, status: String) {
        try {
            val pet = petRepository.getPetById(petId)
            if (pet != null) {
                val isActive = when (status) {
                    Pet.STATUS_AVAILABLE -> true
                    Pet.STATUS_PENDING, Pet.STATUS_ADOPTED -> false
                    else -> pet.is_active
                }
                petRepository.updatePet(pet.copy(is_active = isActive))
                Log.d(TAG, "Pet status updated: $petId -> $status (is_active=$isActive)")
            } else {
                throw Exception("Питомец не найден")
            }
        } catch (e: Exception) {
            throw Exception("Ошибка обновления статуса: ${e.message}")
        }
    }
}