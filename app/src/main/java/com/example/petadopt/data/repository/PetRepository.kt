package com.example.petadopt.data.repository

import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.Pet

interface PetRepository {
    suspend fun getPets(): List<Pet>
    suspend fun getPetById(petId: String): Pet?
    suspend fun getPetsByType(type: String): List<Pet>
    suspend fun searchPets(query: String): List<Pet>
    suspend fun filterPets(
        type: String? = null,
        gender: String? = null,
        size: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        isVaccinated: Boolean? = null,
        isSterilized: Boolean? = null
    ): List<Pet>
    suspend fun likePet(userId: String, petId: String)
    suspend fun getLikedPets(userId: String): List<Pet>
    suspend fun unlikePet(userId: String, petId: String)
    suspend fun submitApplication(application: Application)
    suspend fun getUserApplications(userId: String): List<Application>
    suspend fun getAppliedPetIds(userId: String): Set<String>
    suspend fun getApplicationById(applicationId: String): Application?
    suspend fun getPendingApplications(petId: String): List<Application>
    suspend fun getApplicationsForPet(petId: String): List<Application>
    suspend fun autoAcceptApplication(applicationId: String): String?
    suspend fun updateApplicationStatus(applicationId: String, status: String)
    
    // Методы для администратора/приюта
    suspend fun createPet(pet: Pet, userId: String): String
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(petId: String)
    suspend fun getPetsByShelterId(shelterId: String): List<Pet>
}
