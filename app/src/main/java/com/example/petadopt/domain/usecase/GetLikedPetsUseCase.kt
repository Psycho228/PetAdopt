package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.repository.PetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetLikedPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(userId: String): List<Pet> {
        return repository.getLikedPets(userId)
    }
}
