package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExitToApp
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
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.example.petadopt.data.model.Pet
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import com.example.petadopt.viewmodel.AccountViewModel

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
    
    // Загружаем питомцы при первом отображении экрана
    LaunchedEffect(Unit) {
        viewModel.loadPets()
    }
    
    // Обновляем searchQuery при изменении в ViewModel
    LaunchedEffect(uiState.searchQuery) {
        searchQuery = uiState.searchQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Кабинет приюта",
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.shelterName.isNotBlank()) {
                            Text(
                                text = uiState.shelterName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Кнопка поиска
                    IconButton(onClick = { 
                        // Фокус на поиск (можно расширить)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Outlined.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.pets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Панель поиска и действий
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Поиск
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.searchPets(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Поиск по имени, породе...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { 
                                        searchQuery = ""
                                        viewModel.searchPets("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Кнопки действий
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.loadPets() },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Refresh
                            ) {
                                Text("Обновить")
                            }
                            PrimaryButton(
                                text = "Добавить питомца",
                                onClick = { navController.navigate("admin/addPet") },
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Add
                            )
                        }

                        // Бейджи ролей
                        if (uiState.isShelterAdmin || uiState.isAdminRole) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (uiState.isShelterAdmin) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Приют") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Business,
                                                null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                                if (uiState.isAdminRole) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Админ") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Список питомцев
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Заголовок списка с статистикой
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Мои питомцы",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${uiState.filteredPets.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Список питомцев
                    if (uiState.filteredPets.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.Pets,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (searchQuery.isNotBlank()) "Ничего не найдено" else "У вас пока нет питомцев",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (searchQuery.isNotBlank()) "Попробуйте изменить запрос" else "Добавьте первого питомца!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.filteredPets.size, key = { uiState.filteredPets[it].id ?: it }) { index ->
                            val pet = uiState.filteredPets[index]
                            val applications = uiState.applicationsByPet[pet.id] ?: emptyList()
                            val totalApplications = applications.size
                            
                            ShelterPetCard(
                                pet = pet,
                                applicationCount = totalApplications,
                                onEdit = { 
                                    navController.navigate("admin/editPet/${pet.id}") 
                                },
                                onViewApplications = {
                                    pet.name?.let { name ->
                                        navController.navigate("admin/applications/${pet.id}/${name}")
                                    }
                                },
                                onDelete = { showDeleteDialog = pet.id }
                            )
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
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(onClick = {
                    accountViewModel.logout()
                    showLogoutDialog = false
                    // Выход на экран авторизации
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ShelterPetCard(
    pet: Pet,
    applicationCount: Int = 0,
    onEdit: () -> Unit,
    onViewApplications: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Изображение
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pet.photo_url.ifBlank { null },
                    contentDescription = pet.name,
                    modifier = Modifier.size(80.dp),
                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                )
                
                // Статус
                if (!pet.is_active) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF9E9E9E).copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            // Информация
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    // Индикатор количества заявок
                    if (applicationCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = applicationCount.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${pet.getTypeDisplay()} • ${pet.age} лет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (pet.has_vaccination) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (pet.is_neutered) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Кнопки действий
            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End
            ) {
                // Кнопка просмотра заявок (если есть заявки)
                if (applicationCount > 0) {
                    IconButton(
                        onClick = onViewApplications,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Заявки",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text)
    }
}

@Composable
private fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        content()
    }
}
