package com.example.petadopt.domain.usecase

import com.example.petadopt.data.repository.StorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    /**
     * Загружает одно изображение
     */
    suspend operator fun invoke(context: android.content.Context, uri: android.net.Uri): String {
        return storageRepository.uploadImage(context, uri)
    }
}

@Singleton
class UploadImagesUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    /**
     * Загружает несколько изображений
     */
    suspend operator fun invoke(context: android.content.Context, uris: List<android.net.Uri>): List<String> {
        return storageRepository.uploadImages(context, uris)
    }
}

@Singleton
class DeleteImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    /**
     * Удаляет изображение
     */
    suspend operator fun invoke(imageUrl: String) {
        storageRepository.deleteImage(imageUrl)
    }
}
