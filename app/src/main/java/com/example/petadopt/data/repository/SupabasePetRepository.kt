package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.data.model.Application
import com.example.petadopt.data.model.Pet
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabasePetRepository @Inject constructor(
) : PetRepository {
    private val postgrest: Postgrest = SupabaseConfig.postgrest
    
    companion object {
        private const val TABLE_PETS = "pets"
        private const val TABLE_APPLICATIONS = "applications"
        private const val TABLE_LIKES = "user_likes"
        private const val TAG = "SupabasePetRepository"
    }

    override suspend fun getPets(): List<Pet> {
        return try {
            val result = postgrest.from(TABLE_PETS)
                .select {
                    filter { eq("is_active", true) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Pet>()
            
            Log.d(TAG, "Found ${result.size} active pets")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in getPets: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getPetById(petId: String): Pet? {
        return try {
            val result = postgrest.from(TABLE_PETS)
                .select {
                    filter { eq("id", petId) }
                }
                .decodeSingleOrNull<Pet>()
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pet by ID: ${e.message}")
            null
        }
    }

    override suspend fun getPetsByType(type: String): List<Pet> {
        return try {
            val result = postgrest.from(TABLE_PETS)
                .select {
                    filter {
                        eq("type", type)
                        eq("is_active", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Pet>()
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pets by type: ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchPets(query: String): List<Pet> {
        return try {
            val allPets = getPets()
            val searchLower = query.lowercase()
            allPets.filter { pet ->
                pet.name.lowercase().contains(searchLower) ||
                pet.breed.lowercase().contains(searchLower) ||
                pet.description.lowercase().contains(searchLower)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching pets: ${e.message}")
            emptyList()
        }
    }

    override suspend fun filterPets(
        type: String?,
        gender: String?,
        size: String?,
        minAge: Int?,
        maxAge: Int?,
        isVaccinated: Boolean?,
        isSterilized: Boolean?
    ): List<Pet> {
        return try {
            var pets = getPets()
            
            type?.let { pets = pets.filter { pet -> pet.type == it } }
            gender?.let { pets = pets.filter { pet -> pet.gender == it } }
            size?.let { pets = pets.filter { pet -> pet.size == it } }
            minAge?.let { pets = pets.filter { pet -> pet.ageYearsInt >= it } }
            maxAge?.let { pets = pets.filter { pet -> pet.ageYearsInt <= it } }
            isVaccinated?.let { pets = pets.filter { pet -> pet.isVaccinated == it } }
            isSterilized?.let { pets = pets.filter { pet -> pet.isSterilized == it } }
            
            pets
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering pets: ${e.message}")
            emptyList()
        }
    }

    override suspend fun likePet(userId: String, petId: String) {
        try {
            postgrest.from(TABLE_LIKES).insert(buildJsonObject {
                put("user_id", userId)
                put("pet_id", petId)
            })
            Log.d(TAG, "Liked pet: $petId by user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error liking pet: ${e.message}")
            throw Exception("Ошибка лайка: ${e.message}")
        }
    }

    override suspend fun getLikedPets(userId: String): List<Pet> {
        return try {
            val likedPetIds = postgrest.from(TABLE_LIKES)
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["pet_id"] }
            
            val pets = mutableListOf<Pet>()
            for (petId in likedPetIds) {
                getPetById(petId)?.let { pets.add(it) }
            }
            
            pets
        } catch (e: Exception) {
            Log.e(TAG, "Error getting liked pets: ${e.message}")
            emptyList()
        }
    }

    override suspend fun unlikePet(userId: String, petId: String) {
        try {
            postgrest.from(TABLE_LIKES)
                .delete {
                    filter {
                        and {
                            eq("user_id", userId)
                            eq("pet_id", petId)
                        }
                    }
                }
            
            Log.d(TAG, "Unliked pet: $petId by user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error unliking pet: ${e.message}")
            throw Exception("Ошибка удаления лайка: ${e.message}")
        }
    }

    override suspend fun submitApplication(application: Application) {
        try {
            val appId = if (application.id.isEmpty()) UUID.randomUUID().toString() else application.id
            postgrest.from(TABLE_APPLICATIONS).upsert(buildJsonObject {
                put("id", appId)
                put("user_id", application.user_id)
                put("user_name", application.user_name)
                put("user_email", application.user_email)
                put("pet_id", application.pet_id)
                put("pet_name", application.pet_name)
                put("message", application.message)
                put("contact_time", application.contact_time)
                put("status", application.status)
                if (application.created_at != null) put("created_at", application.created_at)
            })
            Log.d(TAG, "Application submitted: $appId")
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting application: ${e.message}")
            throw Exception("Ошибка отправки заявки: ${e.message}")
        }
    }

    override suspend fun getUserApplications(userId: String): List<Application> {
        return try {
            val result = postgrest.from(TABLE_APPLICATIONS)
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Application>()
            
            Log.d(TAG, "Found ${result.size} applications for user: $userId")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user applications: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAppliedPetIds(userId: String): Set<String> {
        return try {
            val applications = getUserApplications(userId)
            applications.map { it.pet_id }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting applied pet IDs: ${e.message}")
            emptySet()
        }
    }

    override suspend fun getPendingApplications(petId: String): List<Application> {
        return try {
            val result = postgrest.from(TABLE_APPLICATIONS)
                .select {
                    filter {
                        eq("pet_id", petId)
                        eq("status", "pending")
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Application>()
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending applications: ${e.message}")
            emptyList()
        }
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String) {
        try {
            postgrest.from(TABLE_APPLICATIONS)
                .update(buildJsonObject { put("status", status) }) {
                    filter { eq("id", applicationId) }
                }
            Log.d(TAG, "Application status updated: $applicationId -> $status")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating application status: ${e.message}")
            throw Exception("Ошибка обновления статуса: ${e.message}")
        }
    }

    override suspend fun createPet(pet: Pet, userId: String): String {
        return try {
            val petId = UUID.randomUUID().toString()
            postgrest.from(TABLE_PETS).insert(buildJsonObject {
                put("id", petId)
                put("shelter_id", userId)
                put("name", pet.name)
                put("age", pet.age)
                put("type", pet.type)
                put("gender", pet.gender)
                put("size", pet.size)
                put("description", pet.description)
                put("photo_url", pet.photo_url)
                pet.additional_photos?.let { put("additional_photos", JsonArray(it.map { url -> JsonPrimitive(url) })) }
                put("breed", pet.breed)
                put("color", pet.color)
                if (pet.weight != null) put("weight", pet.weight)
                put("is_neutered", pet.is_neutered)
                put("has_vaccination", pet.has_vaccination)
                put("is_active", pet.is_active)
                pet.traits?.let { put("traits", JsonArray(it.map { trait -> JsonPrimitive(trait) })) }
            })
            petId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pet: ${e.message}")
            throw Exception("Ошибка создания питомца: ${e.message}")
        }
    }

    override suspend fun updatePet(pet: Pet) {
        try {
            Log.d(TAG, "Updating pet: id=${pet.id}, name=${pet.name}, shelter_id=${pet.shelter_id}")
            Log.d(TAG, "  traits: ${pet.traits}")
            Log.d(TAG, "  additional_photos: ${pet.additional_photos}")
            Log.d(TAG, "  photo_url: ${pet.photo_url}")
            
            postgrest.from(TABLE_PETS).update(buildJsonObject {
                put("name", pet.name)
                put("age", pet.age)
                put("type", pet.type)
                put("gender", pet.gender)
                put("size", pet.size)
                put("description", pet.description)
                put("photo_url", pet.photo_url)
                pet.additional_photos?.let { put("additional_photos", JsonArray(it.map { url -> JsonPrimitive(url) })) }
                put("breed", pet.breed)
                put("color", pet.color)
                if (pet.weight != null) put("weight", pet.weight)
                put("is_neutered", pet.is_neutered)
                put("has_vaccination", pet.has_vaccination)
                put("is_active", pet.is_active)
                pet.traits?.let { put("traits", JsonArray(it.map { trait -> JsonPrimitive(trait) })) }
            }) {
                filter { eq("id", pet.id) }
            }
            
            Log.d(TAG, "Successfully updated pet: ${pet.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pet: ${e.message}", e)
            throw Exception("Ошибка обновления питомца: ${e.message}")
        }
    }

    override suspend fun deletePet(petId: String) {
        try {
            postgrest.from(TABLE_PETS)
                .delete {
                    filter { eq("id", petId) }
                }
            Log.d(TAG, "Pet deleted: $petId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pet: ${e.message}")
            throw Exception("Ошибка удаления питомца: ${e.message}")
        }
    }

    override suspend fun getPetsByShelterId(shelterId: String): List<Pet> {
        return try {
            val result = postgrest.from(TABLE_PETS)
                .select {
                    filter {
                        eq("shelter_id", shelterId)
                        eq("is_active", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Pet>()
            
            Log.d(TAG, "Found ${result.size} pets for shelter: $shelterId")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pets by shelter ID: ${e.message}")
            emptyList()
        }
    }
}