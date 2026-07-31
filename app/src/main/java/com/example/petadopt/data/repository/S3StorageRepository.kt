package com.example.petadopt.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.petadopt.util.ImageUploadProcessor
import com.example.petadopt.util.S3Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3StorageRepository @Inject constructor() : StorageRepository {
    companion object {
        private const val BUCKET_PET_PHOTOS = "pet-photos"
        private const val TAG = "S3StorageRepository"
    }

    override suspend fun uploadImage(context: Context, uri: Uri): String {
        return uploadPetPhoto(context, uri)
    }

    override suspend fun uploadImages(context: Context, uris: List<Uri>): List<String> {
        return uris.map { uploadImage(context, it) }
    }

    override suspend fun deleteImage(imageUrl: String) {
        deletePhoto(imageUrl)
    }

    private suspend fun uploadPetPhoto(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
            val filePath = "$BUCKET_PET_PHOTOS/$fileName"
            val bytes = ImageUploadProcessor.prepareJpeg(context, uri)

            Log.d(TAG, "Original size: ${bytes.size} bytes")

            // Загружаем в S3
            S3Config.uploadFile(filePath, bytes, "image/jpeg")

            // Возвращаем публичный URL
            val publicUrl = S3Config.getPublicUrl(filePath)
            
            Log.d(TAG, "Photo uploaded: $filePath -> $publicUrl, mimeType: image/jpeg")
            publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo: ${e.message}", e)
            throw Exception("Ошибка загрузки фото: ${e.message}")
        }
    }

    private suspend fun deletePhoto(imageUrl: String) = withContext(Dispatchers.IO) {
        try {
            val filePath = extractFilePath(imageUrl)
            if (filePath != null) {
                S3Config.deleteFile(filePath)
                Log.d(TAG, "Photo deleted: $filePath")
            } else {
                Log.w(TAG, "Could not extract file path from URL: $imageUrl")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo: ${e.message}", e)
        }
    }

    private fun extractFilePath(imageUrl: String): String? {
        return try {
            if (imageUrl.contains("s3.regru.cloud")) {
                val parts = imageUrl.split("/")
                // Ищем позицию после домена и бакета
                val startIndex = imageUrl.indexOf("pet-photos/")
                if (startIndex != -1) {
                    val path = imageUrl.substring(startIndex)
                    if (path.startsWith("/")) path.substring(1) else path
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
