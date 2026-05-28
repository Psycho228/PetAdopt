package com.example.petadopt.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.petadopt.data.model.Application
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Статусы и цвета ────────────────────────────────────────────
private enum class AppStatus(val key: String, val label: String, val color: Color) {
    ALL("all", "Все", Primary),
    PENDING("pending", "В ожидании", Color(0xFFFF9800)),
    PROCESSING("processing", "В работе", Color(0xFF2196F3)),
    APPROVED("approved", "Принято", Color(0xFF4CAF50)),
    REJECTED("rejected", "Отклонено", Color(0xFFF44336));

    companion object {
        fun fromKey(key: String): AppStatus = entries.find { it.key == key } ?: ALL
    }
}

// ─── Основной экран ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetApplicationsScreen(
    navController: NavHostController,
    petId: String,
    petName: String,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allApplications = uiState.applicationsByPet[petId] ?: emptyList()
    var selectedStatus by remember { mutableStateOf(AppStatus.ALL) }

    // Перевод pending → processing при открытии
    LaunchedEffect(Unit) {
        allApplications.filter { it.status == "pending" }.forEach { app ->
            viewModel.updateApplicationStatus(app.id, "processing")
        }
    }

    // Фильтрация
    val filteredApplications = remember(allApplications, selectedStatus) {
        if (selectedStatus == AppStatus.ALL) allApplications
        else allApplications.filter { it.status == selectedStatus.key }
    }

    // Подсчёт по статусам
    val counts = remember(allApplications) {
        mapOf(
            AppStatus.PENDING to allApplications.count { it.status == "pending" },
            AppStatus.PROCESSING to allApplications.count { it.status == "processing" },
            AppStatus.APPROVED to allApplications.count { it.status == "approved" },
            AppStatus.REJECTED to allApplications.count { it.status == "rejected" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Заявки", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = petName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPets() }) {
                        Icon(Icons.Default.Refresh, "Обновить", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading && allApplications.isEmpty()) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (allApplications.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Статистика ──
                item {
                    StatisticsPanel(
                        counts = counts,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // ── Фильтр-чипы ──
                item {
                    StatusFilterRow(
                        selected = selectedStatus,
                        counts = counts,
                        onSelect = { selectedStatus = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // ── Количество результатов ──
                item {
                    Text(
                        text = "${filteredApplications.size} ${declineApplications(filteredApplications.size)}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Список заявок ──
                if (filteredApplications.isEmpty()) {
                    item {
                        NoResultsForFilter(modifier = Modifier.padding(top = 32.dp))
                    }
                } else {
                    items(
                        count = filteredApplications.size,
                        key = { filteredApplications[it].id }
                    ) { index ->
                        val app = filteredApplications[index]
                        ApplicationCard(
                            application = app,
                            onCardClick = {
                                val detailRoute = buildString {
                                    append("admin/application/detail/${app.id}")
                                    append("/${app.user_id}")
                                    append("/${app.user_name}")
                                    append("/${app.user_email}")
                                    append("/${app.pet_id}")
                                    append("/${app.pet_name}")
                                    append("/${app.message}")
                                    append("/${app.contact_time}")
                                    append("/${app.status}")
                                }
                                navController.navigate(detailRoute)
                            },
                            onApprove = {
                                viewModel.updateApplicationStatus(app.id, "approved")
                            },
                            onReject = {
                                viewModel.updateApplicationStatus(app.id, "rejected")
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

// ─── Панель статистики ──────────────────────────────────────────
@Composable
private fun StatisticsPanel(
    counts: Map<AppStatus, Int>,
    modifier: Modifier = Modifier
) {
    val total = counts.values.sum()

    Column(modifier = modifier) {
        Text(
            text = "Обзор",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                icon = Icons.Outlined.Email,
                label = "Всего",
                value = total.toString(),
                accent = Primary,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.HourglassTop,
                label = "В ожидании",
                value = (counts[AppStatus.PENDING] ?: 0).toString(),
                accent = AppStatus.PENDING.color,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                icon = Icons.Outlined.CheckCircle,
                label = "Принято",
                value = (counts[AppStatus.APPROVED] ?: 0).toString(),
                accent = AppStatus.APPROVED.color,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.Cancel,
                label = "Отклонено",
                value = (counts[AppStatus.REJECTED] ?: 0).toString(),
                accent = AppStatus.REJECTED.color,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.06f),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ─── Фильтр-чипы ────────────────────────────────────────────────
@Composable
private fun StatusFilterRow(
    selected: AppStatus,
    counts: Map<AppStatus, Int>,
    onSelect: (AppStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AppStatus.entries.toList()) { status ->
            val count = if (status == AppStatus.ALL) counts.values.sum()
            else counts[status] ?: 0

            val isSelected = selected == status
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(status) },
                label = {
                    Text(
                        text = "${status.label} ($count)",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    if (isSelected && status != AppStatus.ALL) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = status.color.copy(alpha = 0.15f),
                    selectedLabelColor = status.color
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) status.color.copy(alpha = 0.5f) else Color.Transparent,
                    selectedBorderColor = status.color.copy(alpha = 0.5f),
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

// ─── Карточка заявки ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationCard(
    application: Application,
    onCardClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = AppStatus.fromKey(application.status)
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Левая цветная полоса-индикатор статуса
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(status.color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // ── Верхняя строка: аватар + имя + статус ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Аватар с инициалами
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(status.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.userName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = status.color
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (application.userEmail.isNotBlank()) {
                            Text(
                                text = application.userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Статусный чип
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = status.color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = status.color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Сообщение ──
                if (application.message.isNotBlank()) {
                    Text(
                        text = application.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // ── Мета-информация ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Время контакта
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            null,
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = application.contactTime.ifBlank { "—" },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    // Дата создания
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            null,
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatDate(application.created_at),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // ── Кнопки быстрых действий (только для pending/processing) ──
                if (application.status == "pending" || application.status == "processing") {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppStatus.REJECTED.color
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(AppStatus.REJECTED.color.copy(alpha = 0.5f))
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Отклонить", fontSize = 13.sp)
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { showApproveDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppStatus.APPROVED.color
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Check,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Принять", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // ── Диалог подтверждения принятия ──
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = AppStatus.APPROVED.color,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Принять заявку?", fontWeight = FontWeight.Bold) },
            text = { Text("Заявка от ${application.userName} будет одобрена. Вы сможете изменить решение позже.") },
            confirmButton = {
                Button(
                    onClick = {
                        onApprove()
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppStatus.APPROVED.color)
                ) {
                    Text("Принять")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // ── Диалог подтверждения отклонения ──
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(
                    Icons.Default.Cancel,
                    null,
                    tint = AppStatus.REJECTED.color,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Отклонить заявку?", fontWeight = FontWeight.Bold) },
            text = { Text("Заявка от ${application.userName} будет отклонена. Это действие можно отменить на экране деталей.") },
            confirmButton = {
                Button(
                    onClick = {
                        onReject()
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppStatus.REJECTED.color)
                ) {
                    Text("Отклонить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ─── Состояние загрузки ─────────────────────────────────────────
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(16.dp))
            Text("Загрузка заявок...", color = TextSecondary, fontSize = 15.sp)
        }
    }
}

// ─── Пустое состояние ───────────────────────────────────────────
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Inbox,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Нет заявок на этого питомца",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Когда кто-то подаст заявку, она появится здесь",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Нет результатов по фильтру ─────────────────────────────────
@Composable
private fun NoResultsForFilter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.FilterAltOff,
                null,
                tint = TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Нет заявок с таким статусом",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

// ─── Утилиты ────────────────────────────────────────────────────
private fun formatDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return runCatching {
        val instant = Instant.parse(isoString)
        val local = instant.atZone(ZoneId.systemDefault())
        local.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.getOrElse { "—" }
}

private fun declineApplications(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "заявка"
    count % 10 in 2..4 && (count % 100 !in 12..14) -> "заявки"
    else -> "заявок"
}
