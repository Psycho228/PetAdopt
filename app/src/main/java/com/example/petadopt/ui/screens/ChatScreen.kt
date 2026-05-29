package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petadopt.data.model.ChatMessage
import com.example.petadopt.data.model.SenderRole
import com.example.petadopt.ui.theme.*
import com.example.petadopt.viewmodel.ChatState
import com.example.petadopt.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Современный экран чата между пользователем и приютом
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    applicationId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Загружаем сообщения при первом композиции
    LaunchedEffect(applicationId) {
        viewModel.loadMessages(applicationId)
    }
    
    // Автоматическая прокрутка к последнему сообщению
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }
    
    // Обработка ошибок через Snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopAppBar(
                onBack = onBack,
                onRefresh = { viewModel.loadMessages(applicationId) },
                isLoading = state.isLoading
            )
        },
        containerColor = Background,
        bottomBar = {
            ChatInputBar(
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                canSend = !state.isLoading
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.messages.isEmpty()) {
                ChatLoadingScreen()
            } else {
                ChatMessageList(
                    messages = state.messages,
                    currentUserId = state.currentUserId,
                    viewModel = viewModel,
                    listState = listState
                )
            }
            
            // Плавающая кнопка обновления
            AnimatedVisibility(
                visible = state.messages.isNotEmpty() && !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.loadMessages(applicationId) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp),
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Верхняя панель чата с улучшенным дизайном
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text(
                        text = "Чат с приютом",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isLoading) "Обновление..." else "Онлайн",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF66BB6A)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Ещё",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/**
 * Экран загрузки чата
 */
@Composable
private fun ChatLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Загрузка сообщений...",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Подождите немного",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary.copy(alpha = 0.7f)
        )
    }
}

/**
 * Список сообщений чата с группировкой по датам
 */
@Composable
private fun ChatMessageList(
    messages: List<ChatMessage>,
    currentUserId: String?,
    viewModel: ChatViewModel,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                EmptyChatState()
            }
        } else {
            // Группировка сообщений по дням
            val groupedMessages = viewModel.groupMessagesByDate(messages)
            
            groupedMessages.forEach { (date, dayMessages) ->
                item {
                    DateDivider(date)
                }
                
                items(
                    items = dayMessages,
                    key = { it.id ?: it.createdAt }
                ) { message ->
                    ChatMessageBubble(
                        message = message,
                        isOwn = message.senderId == currentUserId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/**
 * Разделитель даты
 */
@Composable
private fun DateDivider(date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TextSecondary.copy(alpha = 0.2f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary.copy(alpha = 0.8f),
            modifier = Modifier
                .background(
                    color = SurfaceLight,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TextSecondary.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}

/**
 * Пузырёк сообщения с современным дизайном
 */
@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isOwn: Boolean,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val senderInfo = viewModel.getSenderInfo(message)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwn) {
            // Аватар отправителя
            ChatAvatar(
                name = senderInfo.displayName,
                role = message.senderRole,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        Column(
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Имя отправителя для чужих сообщений
            if (!isOwn) {
                Text(
                    text = senderInfo.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            
            // Пузырёк сообщения
            MessageBubble(
                message = message.message,
                timestamp = message.createdAt,
                isOwn = isOwn,
                status = message.status
            )
        }
        
        if (isOwn) {
            Spacer(modifier = Modifier.width(8.dp))
            // Статус сообщения
            MessageStatusIndicator(status = message.status)
        }
    }
}

/**
 * Аватар отправителя
 */
@Composable
private fun ChatAvatar(
    name: String,
    role: SenderRole,
    modifier: Modifier = Modifier,
    imageUrl: String? = null
) {
    val backgroundColor = when (role) {
        SenderRole.USER -> Primary.copy(alpha = 0.8f)
        SenderRole.SHELTER -> Color(0xFFFF7043)
        SenderRole.ADMIN -> Color(0xFF7E57C2)
    }
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.6f))
                )
            )
            .border(
                width = 2.dp,
                color = Color.White,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.take(2).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Пузырёк сообщения с градиентом и тенями
 */
@Composable
private fun MessageBubble(
    message: String,
    timestamp: String,
    isOwn: Boolean,
    status: String
) {
    val backgroundColor = if (isOwn) {
        Brush.verticalGradient(
            colors = listOf(
                Primary.copy(alpha = 0.95f),
                PrimaryVariant.copy(alpha = 0.9f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5))
        )
    }
    
    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .shadow(
                elevation = if (isOwn) 4.dp else 2.dp,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isOwn) 4.dp else 16.dp,
                    bottomEnd = if (isOwn) 16.dp else 4.dp
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwn) Color.Transparent else Color.White
        ),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isOwn) 4.dp else 16.dp,
            bottomEnd = if (isOwn) 16.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .padding(14.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwn) Color.White else TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOwn) {
                        Color.White.copy(alpha = 0.7f)
                    } else {
                        TextSecondary.copy(alpha = 0.6f)
                    }
                )
                if (isOwn && status != "sent") {
                    Spacer(modifier = Modifier.width(4.dp))
                    MessageStatusIcon(status = status)
                }
            }
        }
    }
}

/**
 * Индикатор статуса сообщения
 */
@Composable
private fun MessageStatusIndicator(status: String) {
    val (icon, color) = when (status) {
        "delivered" -> Pair(Icons.Default.Check, Color(0xFF4CAF50))
        "read" -> Pair(Icons.Default.CheckCircle, Color(0xFF2196F3))
        "failed" -> Pair(Icons.Default.Error, Color(0xFFF44336))
        else -> Pair(Icons.Default.Check, Color.Gray)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = status,
        tint = color,
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun MessageStatusIcon(status: String) {
    val (icon, color) = when (status) {
        "delivered" -> Pair(Icons.Default.Check, Color(0xFF69F0AE))
        "read" -> Pair(Icons.Default.CheckCircle, Color(0xFF2979FF))
        "failed" -> Pair(Icons.Default.Error, Color(0xFFFF5252))
        else -> Pair(Icons.Default.Check, Color.White.copy(alpha = 0.7f))
    }
    
    Icon(
        imageVector = icon,
        contentDescription = status,
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}

/**
 * Информация об отправителе
 */
data class SenderInfo(
    val displayName: String,
    val avatarUrl: String? = null
)

/**
 * Пустое состояние чата
 */
@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Градиентный круг с иконкой
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.15f),
                            Primary.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Начните общение",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Напишите первое сообщение приюту",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Декоративные пузырьки
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BubblePreview()
            BubblePreview()
        }
    }
}

@Composable
private fun BubblePreview() {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(40.dp)
            .background(
                color = SurfaceLight,
                shape = RoundedCornerShape(12.dp)
            )
    )
    
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(40.dp)
            .background(
                color = Primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
    )
}

/**
 * Панель ввода сообщения
 */
@Composable
private fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    canSend: Boolean
) {
    var messageText by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Быстрые ответы (опционально)
            QuickReplies(
                onQuickReply = { reply ->
                    messageText = reply
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Поле ввода и кнопка отправки
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка вложения (опционально)
                OutlinedIconButton(
                    onClick = { /* TODO: Прикрепить файл */ },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = SurfaceLight
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Прикрепить",
                        tint = TextSecondary
                    )
                }
                
                // Текстовое поле
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    placeholder = {
                        Text(
                            "Напишите сообщение...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = SurfaceLight,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        cursorColor = Primary
                    ),
                    maxLines = 4,
                    enabled = canSend
                )
                
                // Кнопка отправки
                FilledIconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && canSend,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Primary,
                        contentColor = Color.White,
                        disabledContainerColor = SurfaceLight,
                        disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Send,
                        contentDescription = "Отправить",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Быстрые ответы (опционально)
 */
@Composable
private fun QuickReplies(
    onQuickReply: (String) -> Unit
) {
    val quickReplies = listOf(
        "Спасибо!",
        "Уточните, пожалуйста",
        "Я подумаю"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            items = quickReplies,
            key = { it }
        ) { reply: String ->
            FilterChip(
                selected = false,
                onClick = { onQuickReply(reply) },
                label = {
                    Text(
                        text = reply,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.1f),
                    selectedLabelColor = Primary,
                    labelColor = TextSecondary
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = true
            )
        }
    }
}

