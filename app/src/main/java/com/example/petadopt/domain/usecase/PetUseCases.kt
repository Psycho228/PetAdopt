package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.repository.PetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmitApplicationUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(application: Application) {
        repository.submitApplication(application)
    }
}

@Singleton
class GetPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(): List<Pet> {
        return repository.getPets()
    }
}

@Singleton
class GetPetByIdUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String): Pet? {
        return repository.getPetById(petId)
    }
}

@Singleton
class GetUserApplicationsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(userId: String): List<Application> {
        return repository.getUserApplications(userId)
    }
}

@Singleton
class GetAppliedPetIdsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(userId: String): Set<String> {
        return repository.getAppliedPetIds(userId)
    }
}

@Singleton
class LikePetUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(userId: String, petId: String) {
        repository.likePet(userId, petId)
    }
}

@Singleton
class UnlikePetUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(userId: String, petId: String) {
        repository.unlikePet(userId, petId)
    }
}

@Singleton
class UpdateApplicationStatusUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(applicationId: String, status: String) {
        repository.updateApplicationStatus(applicationId, status)
    }
}

@Singleton
class GetPendingApplicationsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String): List<Application> {
        return repository.getPendingApplications(petId)
    }
}

@Singleton
class GetApplicationsForPetUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String): List<Application> {
        return repository.getApplicationsForPet(petId)
    }
}

@Singleton
class AutoAcceptApplicationUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(applicationId: String): String? {
        return repository.autoAcceptApplication(applicationId)
    }
}

@Singleton
class GetPetsByTypeUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(type: String): List<Pet> {
        return repository.getPetsByType(type)
    }
}

@Singleton
class SearchPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(query: String): List<Pet> {
        return repository.searchPets(query)
    }
}

@Singleton
class FilterPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(
        type: String? = null,
        gender: String? = null,
        size: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        isVaccinated: Boolean? = null,
        isSterilized: Boolean? = null
    ): List<Pet> {
        return repository.filterPets(
            type = type,
            gender = gender,
            size = size,
            minAge = minAge,
            maxAge = maxAge,
            isVaccinated = isVaccinated,
            isSterilized = isSterilized
        )
    }
}

@Singleton
class GetPetsByShelterUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(shelterId: String): List<Pet> {
        return repository.getPetsByShelterId(shelterId)
    }
}
