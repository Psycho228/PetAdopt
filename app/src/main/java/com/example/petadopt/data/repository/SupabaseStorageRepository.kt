package com.example.petadopt.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.petadopt.util.ImageUploadProcessor
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageRepository @Inject constructor(
) : StorageRepository {
    private val auth: Auth = SupabaseConfig.auth
    private val storage: Storage = SupabaseConfig.storage
    
    companion object {
        private const val BUCKET_PET_PHOTOS = "pet-photos"
        private const val TAG = "SupabaseStorageRepository"
    }

    override suspend fun uploadImage(context: Context, uri: Uri): String {
        val userId = auth.currentUserOrNull()?.id
            ?: throw Exception("Пользователь не авторизован")
        return uploadPetPhoto(context, uri, userId)
    }

    override suspend fun uploadImages(context: Context, uris: List<Uri>): List<String> {
        return uris.map { uri ->
            uploadImage(context, uri)
        }
    }

    override suspend fun deleteImage(imageUrl: String) {
        deletePhoto(imageUrl)
    }

    suspend fun uploadPetPhoto(context: Context, uri: Uri, userId: String): String = withContext(Dispatchers.IO) {
        try {
            val fileName = "${userId}_${java.util.UUID.randomUUID()}.jpg"
            val filePath = "$fileName"
            
            val bucket = storage.from(BUCKET_PET_PHOTOS)
            
            val bytes = ImageUploadProcessor.prepareJpeg(context, uri)
            Log.d(TAG, "Processed image: ${bytes.size} bytes")
            
            bucket.upload(filePath, bytes, upsert = true)
            
            val publicUrl = bucket.publicUrl(filePath)
            
            Log.d(TAG, "Photo uploaded: $filePath -> $publicUrl, mimeType: image/jpeg")
            publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo: ${e.message}", e)
            throw Exception("Ошибка загрузки фото: ${e.message}")
        }
    }

    suspend fun deletePhoto(imageUrl: String) = withContext(Dispatchers.IO) {
        try {
            val filePath = extractFilePath(imageUrl)
            if (filePath != null) {
                val bucket = storage.from(BUCKET_PET_PHOTOS)
                bucket.delete(filePath)
                Log.d(TAG, "Photo deleted: $filePath")
            } else {
                Log.w(TAG, "Could not extract file path from URL: $imageUrl")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo: ${e.message}", e)
        }
    }

    fun getPublicUrl(filePath: String): String {
        return storage.from(BUCKET_PET_PHOTOS).publicUrl(filePath)
    }

    private fun extractFilePath(imageUrl: String): String? {
        return try {
            if (imageUrl.contains("$BUCKET_PET_PHOTOS/")) {
                imageUrl.substringAfter("$BUCKET_PET_PHOTOS/")
            } else {
                imageUrl
            }
        } catch (e: Exception) {
            null
        }
    }

}
