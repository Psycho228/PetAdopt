package com.example.petadopt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadopt.data.model.ChatMessage
import com.example.petadopt.data.model.SenderInfo
import com.example.petadopt.data.model.SenderRole
import com.example.petadopt.data.repository.AuthRepository
import com.example.petadopt.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

private const val TAG = "ChatViewModel"

/**
 * Состояние экрана чата
 */
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val isShelterUser: Boolean = false
)

/**
 * ViewModel для экрана чата
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var applicationId: String? = null

    init {
        loadCurrentUserId()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUserId
                val role = if (userId != null) {
                    authRepository.getUserRole(userId)
                } else null
                val isShelter = role in listOf("shelter", "admin")
                
                _state.update { it.copy(currentUserId = userId, isShelterUser = isShelter) }
                Log.d(TAG, "Current user loaded: $userId, isShelter: $isShelter")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current user: ${e.message}", e)
            }
        }
    }

    /**
     * Загружает сообщения для заявки
     */
    fun loadMessages(appId: String) {
        applicationId = appId
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Загружаем сообщения
            val messages = chatRepository.getMessages(appId)
            _state.update { it.copy(messages = messages, isLoading = false) }
            Log.d(TAG, "Loaded ${messages.size} messages")
        }
    }

    /**
     * Очищает ошибку
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Отправляет сообщение
     */
    fun sendMessage(messageText: String) {
        val appId = applicationId ?: return
        if (messageText.isBlank()) return
        
        viewModelScope.launch {
            val result = chatRepository.sendMessage(appId, messageText)
            
            result.onSuccess { message ->
                // Добавляем сообщение в список сразу
                _state.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages + message
                    )
                }
                Log.d(TAG, "Message sent successfully")
            }
            result.onFailure { error ->
                _state.update { it.copy(error = error.message) }
                Log.e(TAG, "Error sending message: ${error.message}", error)
            }
        }
    }

    /**
     * Проверяет, является ли сообщение пользователя
     */
    fun isOwnMessage(message: ChatMessage): Boolean {
        return message.senderId == _state.value.currentUserId
    }

    /**
     * Получает отображаемое имя роли отправителя
     */
    fun getSenderDisplayName(message: ChatMessage): String {
        return when (message.senderRole) {
            SenderRole.USER -> "Вы"
            SenderRole.SHELTER -> "Приют"
            SenderRole.ADMIN -> "Администратор"
        }
    }

    /**
     * Получает информацию об отправителе
     */
    fun getSenderInfo(message: ChatMessage): SenderInfo {
        return SenderInfo(
            displayName = getSenderDisplayName(message)
        )
    }

    /**
     * Группирует сообщения по датам
     */
    fun groupMessagesByDate(messages: List<ChatMessage>): Map<String, List<ChatMessage>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        return messages.groupBy { message ->
            try {
                val date = parseTimestamp(message.createdAt)
                dateFormat.format(date)
            } catch (e: Exception) {
                "Неизвестная дата"
            }
        }
    }

    /**
     * Форматирует время сообщения
     */
    fun formatTime(timestamp: String): String {
        return try {
            val date = parseTimestamp(timestamp)
            val now = Calendar.getInstance().time
            
            // Если сегодня - показываем только время
            if (isSameDay(date, now)) {
                SimpleDateFormat("HH:mm", Locale("ru")).format(date)
            } else {
                // Иначе - дату и время
                SimpleDateFormat("dd.MM HH:mm", Locale("ru")).format(date)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: ${e.message}", e)
            "—"
        }
    }

    /**
     * Парсит timestamp из Supabase
     */
    private fun parseTimestamp(timestamp: String): Date {
        val normalized = timestamp.trim().replace(
            Regex("([+-]\\d{2})$"),
            "$1:00"
        )

        val instant = runCatching {
            Instant.parse(normalized)
        }.recoverCatching {
            OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
        }.recoverCatching {
            // Timestamp без offset в Supabase трактуем как UTC, а не локальное время устройства.
            LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .toInstant(ZoneOffset.UTC)
        }.getOrElse { error ->
            throw IllegalArgumentException("Неизвестный формат времени: $timestamp", error)
        }

        return Date.from(instant)
    }

    /**
     * Проверяет, являются ли две даты одним днём
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
