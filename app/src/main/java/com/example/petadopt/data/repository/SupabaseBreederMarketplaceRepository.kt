package com.example.petadopt.data.repository

import com.example.petadopt.data.model.BreederProfile
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseBreederMarketplaceRepository @Inject constructor(
    private val authRepository: AuthRepository
) : BreederMarketplaceRepository {
    private val postgrest: Postgrest = SupabaseConfig.postgrest

    override suspend fun getAvailableListings(): List<SaleListing> =
        postgrest.from(TABLE_LISTINGS)
            .select {
                filter {
                    isIn("status", listOf(SaleListing.STATUS_AVAILABLE, SaleListing.STATUS_RESERVED))
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    override suspend fun getListing(listingId: String): SaleListing? =
        postgrest.from(TABLE_LISTINGS)
            .select { filter { eq("id", listingId) } }
            .decodeSingleOrNull()

    override suspend fun getBreederProfile(profileId: String): BreederProfile? =
        postgrest.from(TABLE_PROFILES)
            .select { filter { eq("id", profileId) } }
            .decodeSingleOrNull()

    override suspend fun getMyProfile(): BreederProfile? {
        val userId = requireUserId()
        return postgrest.from(TABLE_PROFILES)
            .select { filter { eq("user_id", userId) } }
            .decodeSingleOrNull()
    }

    override suspend fun getMyListings(): List<SaleListing> {
        val userId = requireUserId()
        return postgrest.from(TABLE_LISTINGS)
            .select {
                filter { eq("owner_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    override suspend fun saveProfile(profile: BreederProfile): String {
        val userId = requireUserId()
        val profileId = profile.id.ifBlank { UUID.randomUUID().toString() }
        val payload = buildJsonObject {
            put("user_id", userId)
            put("kennel_name", profile.kennelName.trim())
            put("description", profile.description.trim())
            put("city", profile.city.trim())
            put("phone", profile.phone.trim())
            val website = profile.website?.trim()
            if (website.isNullOrBlank()) put("website", JsonNull) else put("website", website)
            put("breeds", JsonArray(profile.breeds.map(::JsonPrimitive)))
            put("verification_status", BreederProfile.STATUS_PENDING)
        }

        if (profile.id.isBlank()) {
            postgrest.from(TABLE_PROFILES).insert(
                buildJsonObject {
                    payload.forEach { (key, value) -> put(key, value) }
                    put("id", profileId)
                }
            )
        } else {
            postgrest.from(TABLE_PROFILES).update(payload) {
                filter { eq("id", profileId) }
            }
        }
        return profileId
    }

    override suspend fun saveListing(listing: SaleListing): String {
        val userId = requireUserId()
        val listingId = listing.id.ifBlank { UUID.randomUUID().toString() }
        val payload = buildJsonObject {
            put("breeder_id", listing.breederId)
            put("owner_id", userId)
            put("name", listing.name.trim())
            put("type", listing.type)
            put("gender", listing.gender)
            put("breed", listing.breed.trim())
            if (listing.birthDate.isNullOrBlank()) {
                put("birth_date", JsonNull)
            } else {
                put("birth_date", listing.birthDate)
            }
            put("price", listing.price)
            put("currency", "RUB")
            put("description", listing.description.trim())
            put("photo_url", listing.photoUrl.trim())
            put("additional_photos", JsonArray(listing.additionalPhotos.map(::JsonPrimitive)))
            put("vaccinated", listing.vaccinated)
            put("vet_passport", listing.vetPassport)
            put("pedigree", listing.pedigree)
            put("chipped", listing.chipped)
            put("delivery_available", listing.deliveryAvailable)
            put("status", SaleListing.STATUS_PENDING)
        }

        if (listing.id.isBlank()) {
            postgrest.from(TABLE_LISTINGS).insert(
                buildJsonObject {
                    payload.forEach { (key, value) -> put(key, value) }
                    put("id", listingId)
                }
            )
        } else {
            postgrest.from(TABLE_LISTINGS).update(payload) {
                filter { eq("id", listingId) }
            }
        }
        return listingId
    }

    override suspend fun updateListingStatus(listingId: String, status: String) {
        postgrest.from(TABLE_LISTINGS).update(
            buildJsonObject { put("status", status) }
        ) {
            filter { eq("id", listingId) }
        }
    }

    private fun requireUserId(): String =
        authRepository.currentUserId ?: error("Требуется авторизация")

    private companion object {
        const val TABLE_PROFILES = "breeder_profiles"
        const val TABLE_LISTINGS = "sale_listings"
    }
}
