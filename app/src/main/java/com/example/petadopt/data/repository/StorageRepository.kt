package com.example.petadopt.data.repository

import android.content.Context
import android.net.Uri

interface StorageRepository {
    suspend fun uploadImage(context: Context, uri: Uri): String
    suspend fun uploadImages(context: Context, uris: List<Uri>): List<String>
    suspend fun deleteImage(imageUrl: String)
}
