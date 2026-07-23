package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.data.model.ChatMessage
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Singleton

@Singleton
class ChatRepository(
    private val authRepository: AuthRepository
) {

    private val postgrest: Postgrest = SupabaseConfig.postgrest

    companion object {
        private const val TABLE_CHAT_MESSAGES = "chat_messages"
        private const val TAG = "ChatRepository"
    }

    /**
     * Получает список сообщений для заявки
     */
    suspend fun getMessages(applicationId: String): Result<List<ChatMessage>> {
        return try {
            val result = postgrest.from(TABLE_CHAT_MESSAGES)
                .select {
                    filter {
                        eq("application_id", applicationId)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<ChatMessage>()

            Log.d(TAG, "Found ${result.size} messages for application $applicationId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat messages: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Отправляет сообщение в чат
     */
    suspend fun sendMessage(applicationId: String, message: String): Result<ChatMessage> {
        val senderId = authRepository.currentUserId
            ?: return Result.failure(Exception("Пользователь не авторизован"))

        return try {
            val response = postgrest.from(TABLE_CHAT_MESSAGES)
                .insert(
                    mapOf(
                        "application_id" to applicationId,
                        "sender_id" to senderId,
                        "message" to message,
                        "status" to "sent"
                    )
                ) {
                    select()
                }

            val messages = response.decodeList<ChatMessage>()
            if (messages.isNotEmpty()) {
                Log.d(TAG, "Message sent successfully: ${messages.first().id}")
                Result.success(messages.first())
            } else {
                Result.failure(Exception("Сообщение не было создано"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Помечает сообщение как прочитанное
     */
    suspend fun markAsRead(messageId: String): Boolean {
        return try {
            postgrest.from(TABLE_CHAT_MESSAGES)
                .update(
                    mapOf("is_read" to true)
                ) {
                    filter {
                        eq("id", messageId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking message as read: ${e.message}", e)
            false
        }
    }
}
