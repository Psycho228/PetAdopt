package com.example.petadopt.util

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import com.example.petadopt.BuildConfig
import android.util.Log

@Singleton
object S3Config {
    private const val ENDPOINT_URL = "https://s3.regru.cloud"
    private const val BUCKET_NAME = BuildConfig.S3_BUCKET_NAME
    private const val ACCESS_KEY = BuildConfig.S3_ACCESS_KEY
    private const val SECRET_KEY = BuildConfig.S3_SECRET_KEY
    private const val TAG = "S3Config"

    private val httpClient by lazy {
        HttpClient(Android)
    }

    // Path-style URL: https://s3.regru.cloud/pet-photos/key
    fun getPublicUrl(key: String): String {
        return "$ENDPOINT_URL/$BUCKET_NAME/$key"
    }

    suspend fun uploadFile(key: String, data: ByteArray, contentType: String) = withContext(Dispatchers.IO) {
        val url = "$ENDPOINT_URL/$BUCKET_NAME/$key"
        Log.d(TAG, "Uploading to: $url")
        Log.d(TAG, "Bucket: $BUCKET_NAME")
        
        try {
            // Создаём подписанный запрос с SigV4
            val bodyChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" // SHA256 пустой строки для unsigned payload
            val signedRequest = S3SigV4Signer.signPutRequest(url, "PUT", null, contentType)
            
            val response: HttpResponse = httpClient.put(url) {
                headers {
                    signedRequest.headers.forEach { (name, value) ->
                        append(name, value)
                    }
                }
                setBody(data)
            }
            
            Log.d(TAG, "Upload response status: ${response.status}")
            if (!response.status.isSuccess()) {
                val body = response.bodyAsText()
                Log.e(TAG, "Upload failed: $body")
                throw Exception("S3 upload failed: ${response.status} - $body")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteFile(key: String) = withContext(Dispatchers.IO) {
        val url = "$ENDPOINT_URL/$BUCKET_NAME/$key"
        try {
            val signedRequest = S3SigV4Signer.signPutRequest(url, "DELETE")
            val response = httpClient.delete(url) {
                headers {
                    signedRequest.headers.forEach { (name, value) ->
                        append(name, value)
                    }
                }
            }
            Log.d(TAG, "Delete response status: ${response.status}")
        } catch (e: Exception) {
            Log.e(TAG, "Delete error: ${e.message}")
        }
    }
}