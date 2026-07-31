package com.example.petadopt.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUploadProcessor {
    private const val DEFAULT_MAX_SIZE = 1024
    private const val DEFAULT_QUALITY = 80

    fun prepareJpeg(
        context: Context,
        uri: Uri,
        maxSize: Int = DEFAULT_MAX_SIZE,
        quality: Int = DEFAULT_QUALITY
    ): ByteArray {
        val orientation = readOrientation(context, uri)
        val decoded = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: error("Не удалось открыть изображение")

        val oriented = applyOrientation(decoded, orientation)
        if (oriented !== decoded) decoded.recycle()

        val scale = (maxSize.toFloat() / maxOf(oriented.width, oriented.height)).coerceAtMost(1f)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                oriented,
                (oriented.width * scale).toInt().coerceAtLeast(1),
                (oriented.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            oriented
        }

        return try {
            ByteArrayOutputStream().use { output ->
                check(scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                    "Не удалось обработать изображение"
                }
                output.toByteArray()
            }
        } finally {
            if (scaled !== oriented) scaled.recycle()
            oriented.recycle()
        }
    }

    private fun readOrientation(context: Context, uri: Uri): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun applyOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return source
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
