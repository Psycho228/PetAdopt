package com.example.petadopt.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.example.petadopt.data.model.Pet
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.util.formatYears
import com.example.petadopt.viewmodel.AccountViewModel
import com.example.petadopt.viewmodel.AdminViewModel

@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShelterScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }

    // Автообновление при возврате на экран (после добавления/редактирования питомца)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadPets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.searchQuery) {
        searchQuery = uiState.searchQuery
    }

    // Подсчёт статистики
    val totalPets = uiState.pets.size
    val activePets = uiState.pets.count { it.is_active }
    val totalApplications = uiState.applicationsByPet.values.sumOf { it.size }
    val averageAge = if (totalPets > 0) uiState.pets.mapNotNull { it.age }.average().toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uiState.shelterName.ifBlank { "Кабинет приюта" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (uiState.shelterName.isNotBlank()) {
                                Text(
                                    text = "Панель управления",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(
                            if (searchExpanded) Icons.Default.SearchOff else Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("admin/addPet") },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить питомца")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Поисковая панель (сворачиваемая)
            AnimatedVisibility(
                visible = searchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchPets(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Поиск по имени, породе, виду...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.searchPets("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            cursorColor = Primary
                        )
                    )
                }
            }

            if (uiState.isLoading && uiState.pets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Загрузка питомцев...", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ===== СТАТИСТИКА =====
                    item {
                        Text(
                            text = "Обзор",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Outlined.Pets,
                                label = "Питомцы",
                                value = totalPets.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Outlined.Email,
                                label = "Заявки",
                                value = totalApplications.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Outlined.CheckCircle,
                                label = "Активные",
                                value = activePets.toString(),
                                accent = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Outlined.Timer,
                                label = "Ср. возраст",
                                value = "${averageAge} л.",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ===== ДЕЙСТВИЯ =====
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.loadPets() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Обновить")
                            }
                            FilledTonalButton(
                                onClick = {
                                    navController.navigate("admin/addPet")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Primary.copy(alpha = 0.15f),
                                    contentColor = Primary
                                )
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Добавить")
                            }
                        }
                    }

                    // ===== ЗАГОЛОВОК СПИСКА =====
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Питомцы",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "${uiState.filteredPets.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            // Ролевые чипы
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (uiState.isShelterAdmin) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Приют", fontSize = 11.sp) },
                                        icon = {
                                            Icon(Icons.Default.Business, null, modifier = Modifier.size(14.dp))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                                if (uiState.isAdminRole) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Админ", fontSize = 11.sp) },
                                        icon = {
                                            Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(14.dp))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ===== СПИСОК ПИТОМЦЕВ =====
                    if (uiState.filteredPets.isEmpty()) {
                        item {
                            EmptyState(
                                query = searchQuery,
                                modifier = Modifier.padding(top = 48.dp)
                            )
                        }
                    } else {
                        items(
                            count = uiState.filteredPets.size,
                            key = { uiState.filteredPets[it].id ?: it }
                        ) { index ->
                            val pet = uiState.filteredPets[index]
                            val applications = uiState.applicationsByPet[pet.id] ?: emptyList()

                            ShelterPetCard(
                                pet = pet,
                                applicationCount = applications.size,
                                onCardClick = {
                                    navController.navigate("admin/editPet/${pet.id}")
                                },
                                onViewApplications = {
                                    pet.name?.let { name ->
                                        navController.navigate("admin/applications/${pet.id}/${name}")
                                    }
                                },
                                onEdit = {
                                    navController.navigate("admin/editPet/${pet.id}")
                                },
                                onDelete = { showDeleteDialog = pet.id }
                            )
                        }
                    }

                    // Отступ для FAB
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    // Диалог удаления
    showDeleteDialog?.let { petId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Удалить питомца?", fontWeight = FontWeight.Bold) },
            text = { Text("Это действие нельзя отменить. Все данные, включая заявки, будут удалены.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePet(petId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Выйти из аккаунта?", fontWeight = FontWeight.Bold) },
            text = { Text("Вы будете перенаправлены на экран авторизации.") },
            confirmButton = {
                Button(
                    onClick = {
                        accountViewModel.logout()
                        showLogoutDialog = false
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ===================== STAT CARD =====================
@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.06f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

// ===================== EMPTY STATE =====================
@Composable
private fun EmptyState(query: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (query.isNotBlank()) Icons.Outlined.SearchOff else Icons.Outlined.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (query.isNotBlank()) "Ничего не найдено" else "Нет питомцев",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (query.isNotBlank()) "Попробуйте изменить запрос" else "Добавьте первого питомца!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

// ===================== PET CARD =====================
@Composable
private fun ShelterPetCard(
    pet: Pet,
    applicationCount: Int,
    onCardClick: () -> Unit,
    onViewApplications: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColors = if (!pet.is_active)
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    else
        CardDefaults.cardColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (pet.is_active) 2.dp else 0.dp)
    ) {
        Column {
            // Изображение
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = pet.photo_url.takeIf { it.isNotBlank() },
                    contentDescription = pet.name,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Градиентная подложка снизу для читаемости текста
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.05f),
                                    Color.Black.copy(alpha = 0.5f)
                                ),
                                startY = 60f,
                                endY = 180f
                            )
                        )
                )

                // Статусный бейдж
                if (!pet.is_active) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF9E9E9E).copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "Скрыт",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Информация поверх изображения
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pet.name ?: "Без имени",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (pet.is_active) Color.White else TextSecondary
                            )
                            Text(
                                text = "${pet.getTypeDisplay()} • ${formatYears(pet.age)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (pet.is_active) Color.White.copy(alpha = 0.9f) else TextSecondary.copy(alpha = 0.7f)
                            )
                        }

                        // Бейдж заявок
                        if (applicationCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Email,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = applicationCount.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Нижняя панель с информацией и кнопками
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Чипы с характеристиками
                if (pet.has_vaccination || pet.is_neutered || pet.size != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        if (pet.has_vaccination) {
                            InputChip(
                                selected = true,
                                onClick = {},
                                label = { Text("Вакцинирован", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp))
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        if (pet.is_neutered) {
                            InputChip(
                                selected = true,
                                onClick = {},
                                label = { Text("Стерилизован", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Favorite, null, Modifier.size(14.dp))
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        pet.size?.let { size ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(size, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }

                // Описание
                if (!pet.description.isNullOrBlank()) {
                    Text(
                        text = pet.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Кнопки действий
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Заявки
                    if (applicationCount > 0) {
                        TextButton(
                            onClick = onViewApplications,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Outlined.Email,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = Primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Заявки", color = Primary, fontSize = 13.sp)
                        }
                    }

                    // Редактировать
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = Primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Ред.", color = Primary, fontSize = 13.sp)
                    }

                    // Удалить
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Удалить", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
