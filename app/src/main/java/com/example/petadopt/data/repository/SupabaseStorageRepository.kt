package com.example.petadopt.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
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
            val file = uriToFile(context, uri)
            val mimeType = getMimeType(context, uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val fileName = "${userId}_${java.util.UUID.randomUUID()}.$extension"
            val filePath = "$fileName"
            
            val bucket = storage.from(BUCKET_PET_PHOTOS)
            
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
            
            Log.d(TAG, "Original: ${file.length()} bytes, Compressed: ${bytes.size} bytes")
            
            bucket.upload(filePath, bytes, upsert = true)
            
            val publicUrl = bucket.publicUrl(filePath)
            
            Log.d(TAG, "Photo uploaded: $filePath -> $publicUrl, mimeType: $mimeType")
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

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Не удалось открыть InputStream для изображения")
        
        val tempFile = File(context.cacheDir, "temp_upload_${java.util.UUID.randomUUID()}")
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
}
