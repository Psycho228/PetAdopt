package com.example.petadopt.data.repository

import com.example.petadopt.data.model.BreederProfile
import com.example.petadopt.data.model.SaleListing

interface BreederMarketplaceRepository {
    suspend fun getAvailableListings(): List<SaleListing>
    suspend fun getListing(listingId: String): SaleListing?
    suspend fun getBreederProfile(profileId: String): BreederProfile?
    suspend fun getMyProfile(): BreederProfile?
    suspend fun getMyListings(): List<SaleListing>
    suspend fun saveProfile(profile: BreederProfile): String
    suspend fun saveListing(listing: SaleListing): String
    suspend fun updateListingStatus(listingId: String, status: String)
}
