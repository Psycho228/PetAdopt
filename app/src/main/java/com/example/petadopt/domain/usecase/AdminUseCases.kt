package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.repository.AdminRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatePetUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(pet: Pet, userId: String): String {
        return repository.createPet(pet, userId)
    }
}

@Singleton
class UpdatePetUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(pet: Pet) {
        repository.updatePet(pet)
    }
}

@Singleton
class DeletePetUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(petId: String) {
        repository.deletePet(petId)
    }
}

@Singleton
class GetAllPetsUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): List<Pet> {
        return repository.getAllPets()
    }
}

@Singleton
class UpdatePetStatusUseCaseAdmin @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(petId: String, status: String) {
        repository.updatePetStatus(petId, status)
    }
}
