package com.example.petadopt.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreederProfile(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("kennel_name") val kennelName: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("city") val city: String = "",
    @SerialName("phone") val phone: String = "",
    @SerialName("website") val website: String? = null,
    @SerialName("breeds") val breeds: List<String> = emptyList(),
    @SerialName("verification_status") val verificationStatus: String = STATUS_PENDING,
    @SerialName("moderation_note") val moderationNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_VERIFIED = "verified"
        const val STATUS_REJECTED = "rejected"
    }
}

@Serializable
data class SaleListing(
    @SerialName("id") val id: String = "",
    @SerialName("breeder_id") val breederId: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = Pet.TYPE_CAT,
    @SerialName("gender") val gender: String = Pet.GENDER_MALE,
    @SerialName("breed") val breed: String = "",
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("price") val price: Double = 0.0,
    @SerialName("currency") val currency: String = "RUB",
    @SerialName("description") val description: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("additional_photos") val additionalPhotos: List<String> = emptyList(),
    @SerialName("vaccinated") val vaccinated: Boolean = false,
    @SerialName("vet_passport") val vetPassport: Boolean = false,
    @SerialName("pedigree") val pedigree: Boolean = false,
    @SerialName("chipped") val chipped: Boolean = false,
    @SerialName("delivery_available") val deliveryAvailable: Boolean = false,
    @SerialName("status") val status: String = STATUS_DRAFT,
    @SerialName("moderation_note") val moderationNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_PENDING = "pending"
        const val STATUS_AVAILABLE = "available"
        const val STATUS_RESERVED = "reserved"
        const val STATUS_SOLD = "sold"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_ARCHIVED = "archived"
    }
}
