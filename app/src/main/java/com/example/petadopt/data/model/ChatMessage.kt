package com.example.petadopt.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Модель сообщения чата
 */
@Serializable
data class ChatMessage(
    @SerialName("id") val id: String? = null,
    @SerialName("application_id") val applicationId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sender_role") val senderRole: SenderRole,
    @SerialName("message") val message: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("status") val status: String = "sent" // sent, delivered, read, failed
)

/**
 * Роль отправителя сообщения
 */
enum class SenderRole {
    @SerialName("user")
    USER,
    
    @SerialName("shelter")
    SHELTER,
    
    @SerialName("admin")
    ADMIN
}

/**
 * DTO для создания нового сообщения
 */
@Serializable
data class CreateChatMessage(
    val applicationId: String,
    val message: String
)

/**
 * Информация об отправителе для UI
 */
data class SenderInfo(
    val displayName: String,
    val avatarUrl: String? = null
)
