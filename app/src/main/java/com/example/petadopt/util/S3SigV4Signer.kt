package com.example.petadopt.util

import android.util.Log
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.example.petadopt.BuildConfig

object S3SigV4Signer {
    private const val SERVICE = "s3"
    private const val ALGORITHM = "AWS4-HMAC-SHA256"
    private const val DATE_FORMAT = "yyyyMMdd"
    private const val DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'"
    private const val TAG = "S3SigV4Signer"

    data class SignedRequest(
        val url: String,
        val headers: Map<String, String>
    )

    fun signPutRequest(
        url: String,
        method: String = "PUT",
        bodyChecksum: String? = null,
        contentType: String? = null
    ): SignedRequest {
        val uri = URI(url)
        val path = uri.path ?: "/"
        val query = uri.query ?: ""
        val host = uri.host

        val now = Date()
        val utc = TimeZone.getTimeZone("UTC")
        val dateStamp = SimpleDateFormat(DATE_FORMAT, Locale.US).apply { timeZone = utc }.format(now)
        val timeStamp = SimpleDateFormat(DATE_TIME_FORMAT, Locale.US).apply { timeZone = utc }.format(now)

        val accessKey = BuildConfig.S3_ACCESS_KEY
        val secretKey = BuildConfig.S3_SECRET_KEY
        val region = "default"

        Log.d(TAG, "Current UTC time: $timeStamp")

        val canonicalHeaders = buildCanonicalHeaders(host, timeStamp, bodyChecksum, contentType)
        val signedHeaders = extractSignedHeaders(canonicalHeaders)
        val canonicalRequest = buildCanonicalRequest(method, path, query, canonicalHeaders, signedHeaders, bodyChecksum)

        val credentialScope = "$dateStamp/$region/$SERVICE/aws4_request"
        val stringToSign = buildStringToSign(ALGORITHM, timeStamp, credentialScope, canonicalRequest)

        val signature = calculateSignature(secretKey, dateStamp, region, stringToSign)

        val authorization = "$ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

        val signedHeadersMap = mutableMapOf(
            "Host" to host,
            "x-amz-date" to timeStamp,
            "Authorization" to authorization
        )
        contentType?.let { signedHeadersMap["Content-Type"] = it }
        bodyChecksum?.let { signedHeadersMap["x-amz-content-sha256"] = it }

        Log.d(TAG, "Signed URL: $url")
        Log.d(TAG, "Authorization: $authorization")

        return SignedRequest(url, signedHeadersMap)
    }

    private fun buildCanonicalHeaders(host: String, timeStamp: String, bodyChecksum: String?, contentType: String?): String {
        val headers = mutableMapOf<String, String>()
        headers["host"] = host
        headers["x-amz-date"] = timeStamp
        bodyChecksum?.let { headers["x-amz-content-sha256"] = it }
        contentType?.let { headers["content-type"] = it }

        return headers.entries.sortedBy { it.key.lowercase() }
            .joinToString("\n") { "${it.key}:${it.value.trim()}" } + "\n"
    }

    private fun extractSignedHeaders(canonicalHeaders: String): String {
        return canonicalHeaders.split("\n")
            .filter { it.isNotBlank() }
            .map { it.substringBefore(":").lowercase() }
            .sorted()
            .joinToString(";")
    }

    private fun buildCanonicalRequest(
        method: String,
        path: String,
        query: String,
        canonicalHeaders: String,
        signedHeaders: String,
        bodyChecksum: String?
    ): String {
        val hashedPayload = bodyChecksum ?: "UNSIGNED-PAYLOAD"
        val canonicalUri = path.ifEmpty { "/" }
        val canonicalQueryString = query.ifEmpty { "" }

        return buildString {
            append(method)
            append("\n")
            append(canonicalUri)
            append("\n")
            append(canonicalQueryString)
            append("\n")
            append(canonicalHeaders)
            append("\n")
            append(signedHeaders)
            append("\n")
            append(hashedPayload)
        }
    }

    private fun buildStringToSign(algorithm: String, timeStamp: String, credentialScope: String, canonicalRequest: String): String {
        val hashedCanonicalRequest = hashString(canonicalRequest)
        return buildString {
            append(algorithm)
            append("\n")
            append(timeStamp)
            append("\n")
            append(credentialScope)
            append("\n")
            append(hashedCanonicalRequest)
        }
    }

    private fun calculateSignature(secretKey: String, dateStamp: String, region: String, stringToSign: String): String {
        val keyDate = sign(("AWS4" + secretKey).toByteArray(), dateStamp.toByteArray())
        val keyRegion = sign(keyDate, region.toByteArray())
        val keyService = sign(keyRegion, SERVICE.toByteArray())
        val signingKey = sign(keyService, "aws4_request".toByteArray())
        return hexString(sign(signingKey, stringToSign.toByteArray()))
    }

    private fun sign(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun hashString(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return hexString(digest.digest(data.toByteArray()))
    }

    private fun hexString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
