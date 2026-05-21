package com.example.petadopt.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.petadopt.util.S3Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
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
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.$extension"
            val filePath = "$BUCKET_PET_PHOTOS/$fileName"

            // Читаем и сжимаем изображение
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Не удалось открыть изображение")

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Сжимаем до разумного размера (макс 1024px по большей стороне)
            val maxSize = 1024
            val scale = if (bitmap.width > bitmap.height) {
                maxSize.toFloat() / bitmap.width
            } else {
                maxSize.toFloat() / bitmap.height
            }

            val scaledBitmap = if (scale < 1) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // Конвертируем в JPEG с качеством 80%
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            Log.d(TAG, "Original size: ${bytes.size} bytes")

            // Загружаем в S3
            S3Config.uploadFile(filePath, bytes, mimeType)

            // Возвращаем публичный URL
            val publicUrl = S3Config.getPublicUrl(filePath)
            
            Log.d(TAG, "Photo uploaded: $filePath -> $publicUrl, mimeType: $mimeType")
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