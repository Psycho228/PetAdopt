package com.example.petadopt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.petadopt.data.model.Pet
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import com.example.petadopt.viewmodel.AdminUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = if (uiState.isAdminRole) "Админ-панель" else "Кабинет приюта",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.shelterName.isNotBlank()) {
                            Text(
                                text = uiState.shelterName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Верхняя панель с заголовком, поиском и кнопкой добавления
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Заголовок и кнопки в одну строку
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isAdminRole) "Все питомцы" else "Мои питомцы",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Кнопка "Обновить"
                            FilledIconButton(
                                onClick = { viewModel.loadPets() },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            // Кнопка "Добавить"
                            FilledIconButton(
                                onClick = { navController.navigate("admin/addPet") },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.White)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Поиск и статистика в одной строке
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Поле поиска
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.searchPets(it) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            placeholder = { 
                                Text("Поиск по имени, породе...", style = MaterialTheme.typography.bodyMedium)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = uiState.searchQuery.isNotBlank(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(onClick = { viewModel.searchPets("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Очистить",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        // Карточка статистики
                        Surface(
                            modifier = Modifier
                                .width(140.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (uiState.isAdminRole) Icons.Default.People else Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = uiState.pets.size.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Бейджи ролей
                    AnimatedVisibility(
                        visible = uiState.isShelterAdmin || uiState.isAdminRole,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.isShelterAdmin) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Приют", style = MaterialTheme.typography.labelMedium) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Business,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                            if (uiState.isAdminRole) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Админ", style = MaterialTheme.typography.labelMedium) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Список питомцев
            if (uiState.isLoading && uiState.pets.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        uiState.filteredPets,
                        key = { it.id ?: "" }
                    ) { pet ->
                        val applications = uiState.applicationsByPet[pet.id] ?: emptyList()
                        val activeApplications = applications.count { it.status == "approved" || it.status == "pending" }
                        PetAdminItem(
                            pet = pet,
                            onClick = { 
                                // Авто-принимаем заявки при первом открытии карточки
                                if (activeApplications > 0) {
                                    viewModel.autoAcceptApplications(pet.id ?: return@PetAdminItem)
                                }
                                navController.navigate("admin/editPet/${pet.id}")
                            },
                            onDelete = { showDeleteDialog = pet.id },
                            applicationCount = activeApplications
                        )
                    }

                    if (uiState.filteredPets.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Icon(
                                            if (uiState.searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.Pets,
                                            null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        if (uiState.searchQuery.isNotBlank()) "Ничего не найдено" else "У вас пока нет питомцев",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        if (uiState.searchQuery.isNotBlank()) "Попробуйте изменить поисковый запрос" else "Нажмите кнопку +, чтобы добавить питомца",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления
    showDeleteDialog?.let { petId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить питомца") },
            text = { Text("Вы уверены, что хотите удалить этого питомца? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePet(petId)
                        showDeleteDialog = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetAdminItem(
    pet: Pet,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    applicationCount: Int = 0
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть с информацией о питомце (кликабельная)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (pet.status) {
                            Pet.STATUS_AVAILABLE -> MaterialTheme.colorScheme.primaryContainer
                            Pet.STATUS_PENDING -> MaterialTheme.colorScheme.secondaryContainer
                            Pet.STATUS_ADOPTED -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> Color.Gray
                        }
                    ) {
                        Text(
                            pet.getStatusDisplay(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = when (pet.status) {
                                Pet.STATUS_AVAILABLE -> MaterialTheme.colorScheme.onPrimaryContainer
                                Pet.STATUS_PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                                Pet.STATUS_ADOPTED -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> Color.White
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Индикатор количества заявок
                    if (applicationCount > 0) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (applicationCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (applicationCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else TextSecondary
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = applicationCount.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (applicationCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else TextSecondary
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Text(
                        text = pet.getTypeDisplay(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = pet.age.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    if (pet.breed.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = pet.breed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Индикаторы прививок и стерилизации
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pet.isVaccinated) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(18.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Привит",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (pet.isSterilized) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(18.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Стерилизован",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (pet.size.isNotBlank()) {
                        Spacer(Modifier.width(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = pet.getSizeDisplay(),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Кнопки действий (некликабельная область)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                // Кнопка редактирования
                FilledTonalIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Кнопка удаления
                FilledTonalIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
