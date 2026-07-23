package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.data.model.Application
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.ApplicationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    viewModel: ApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Обновляем список при открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadApplications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заявки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadApplications() }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Обновить",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Primary,
                    actionIconContentColor = Primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (state.applications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = TextSecondary.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Пока нет заявок",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Подать заявку на питомца",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "${state.applications.size} ${declinationApplications(state.applications.size)}",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.applications, key = { it.id }) { application ->
                        ApplicationCard(
                            application = application,
                            formattedTime = viewModel.getFormattedTime(application.timestamp),
                            statusText = viewModel.getStatusText(application.status),
                            statusColor = viewModel.getStatusColor(application.status),
                            onClick = { onChatClick(application.id) },
                            onChatClick = { onChatClick(application.id) }
                        )
                    }
                }
            }

            if (state.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private fun declinationApplications(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "заявка"
    count % 10 in 2..4 && (count % 100 !in 12..14) -> "заявки"
    else -> "заявок"
}

@Composable
private fun ApplicationCard(
    application: Application,
    formattedTime: String,
    statusText: String,
    statusColor: Color,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = application.petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(text = statusText, color = statusColor)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = formattedTime,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = application.message.ifEmpty { "Нет сообщения" },
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Связь: ${application.contactTime}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = application.userName,
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    // Кнопка чата
                    IconButton(
                        onClick = { onChatClick() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        androidx.compose.material.icons.Icons.Default.Chat
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
