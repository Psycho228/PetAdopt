package com.example.petadopt.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.petadopt.data.model.Pet
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AdminViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    navController: NavHostController,
    petId: String? = null,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isEditing = petId != null
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf(listOf<android.net.Uri>()) }

    val allImageUrls = remember(uiState.existingImageUrls, uiState.uploadedImages) {
        uiState.existingImageUrls + uiState.uploadedImages
    }

    // Поля формы
    var petName by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petAgeYears by remember { mutableStateOf(0) }
    var petType by remember { mutableStateOf(Pet.TYPE_CAT) }
    var petGender by remember { mutableStateOf(Pet.GENDER_MALE) }
    var petSize by remember { mutableStateOf(Pet.SIZE_MEDIUM) }
    var petBreed by remember { mutableStateOf("") }
    var petColor by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petLocation by remember { mutableStateOf("") }
    var petShelterName by remember { mutableStateOf("") }
    var petShelterContact by remember { mutableStateOf("") }
    var petEnergyLevel by remember { mutableStateOf("medium") }
    var isVaccinated by remember { mutableStateOf(false) }
    var isSterilized by remember { mutableStateOf(false) }
    var isHouseTrained by remember { mutableStateOf(false) }
    var goodWithKids by remember { mutableStateOf(true) }
    var goodWithPets by remember { mutableStateOf(true) }

    var petTraitsInput by remember { mutableStateOf("") }
    var petTraits by remember { mutableStateOf(emptyList<String>()) }

    var petLoaded by remember { mutableStateOf(false) }

    // Загрузка данных при редактировании
    LaunchedEffect(petId) {
        if (petId != null) viewModel.loadPetById(petId)
    }

    LaunchedEffect(uiState.currentPet) {
        val pet = uiState.currentPet ?: return@LaunchedEffect
        if (petLoaded) return@LaunchedEffect
        petLoaded = true
        petName = pet.name
        petAge = pet.age.toString()
        petType = pet.type
        petGender = pet.gender ?: Pet.GENDER_MALE
        petSize = pet.size ?: Pet.SIZE_MEDIUM
        petBreed = pet.breed ?: ""
        petColor = pet.color ?: ""
        petDescription = pet.description ?: ""
        petLocation = ""
        petShelterName = ""
        petShelterContact = ""
        petEnergyLevel = "medium"
        isVaccinated = pet.has_vaccination
        isSterilized = pet.is_neutered
        isHouseTrained = false
        goodWithKids = true
        goodWithPets = true
        petTraits = pet.petTraits
        petTraitsInput = pet.petTraits.joinToString(", ")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = selectedImages + uris
            viewModel.uploadImages(context, uris)
        }
    }

    LaunchedEffect(uiState.uploadedImages) {
        if (uiState.uploadedImages.isNotEmpty()) {
            selectedImages = emptyList()
            delay(100)
        }
    }

    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            delay(500)
            viewModel.clearCurrentPet()
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCurrentPet() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Редактирование" else "Новый питомец",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // ===== ФОТОГРАФИИ =====
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "Фотографии",
                        subtitle = "Добавьте до 6 фото питомца",
                        icon = Icons.Outlined.PhotoCamera
                    )

                    Spacer(Modifier.height(12.dp))

                    if (allImageUrls.isNotEmpty() || selectedImages.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (allImageUrls.size + selectedImages.size < 3) 120.dp else 240.dp)
                        ) {
                            // Существующие фото
                            items(uiState.existingImageUrls) { url ->
                                PhotoThumbnail(
                                    url = url,
                                    onDelete = {
                                        viewModel.removeImage(uiState.existingImageUrls.indexOf(url))
                                        viewModel.deleteImageFromServer(url)
                                    }
                                )
                            }
                            // Загруженные
                            items(uiState.uploadedImages) { url ->
                                val idx = uiState.existingImageUrls.size + uiState.uploadedImages.indexOf(url)
                                PhotoThumbnail(
                                    url = url,
                                    onDelete = {
                                        viewModel.removeImage(idx)
                                        viewModel.deleteImageFromServer(url)
                                    }
                                )
                            }
                            // Выбранные (ещё не загружены)
                            items(selectedImages) { uri ->
                                val idx = uiState.existingImageUrls.size + uiState.uploadedImages.size + selectedImages.indexOf(uri)
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(model = uri, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = {
                                            viewModel.removeImage(idx)
                                            selectedImages = selectedImages.filterIndexed { i, _ -> i != selectedImages.indexOf(uri) }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, "Удалить", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            // Кнопка добавления
                            item {
                                AddPhotoButton(onClick = { imagePickerLauncher.launch("image/*") })
                            }
                        }
                    } else {
                        // Крупная кнопка добавления
                        AddPhotoPlaceholder(onClick = { imagePickerLauncher.launch("image/*") })
                    }

                    if (uiState.uploadingImages) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                        Spacer(Modifier.height(4.dp))
                        Text("Загрузка фото...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== ОСНОВНАЯ ИНФОРМАЦИЯ =====
            SectionCard {
                SectionHeader(
                    title = "Основная информация",
                    subtitle = "Имя, возраст и базовые данные",
                    icon = Icons.Outlined.Info
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Имя питомца") },
                    placeholder = { Text("Например: Барсик") },
                    leadingIcon = { Icon(Icons.Outlined.Pets, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = petAge,
                    onValueChange = {
                        petAge = it
                        petAgeYears = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                    },
                    label = { Text("Возраст") },
                    placeholder = { Text("Например: 3 года") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                // Тип — чипы с иконками
                Text("Тип питомца", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        Triple("cat", "Кошка", Icons.Outlined.Pets),
                        Triple("dog", "Собака", Icons.Outlined.Park),
                        Triple("bird", "Птица", Icons.Outlined.Flight),
                        Triple("other", "Другое", Icons.Outlined.MoreHoriz)
                    ).forEach { (type, label, icon) ->
                        FilterChip(
                            selected = petType == type,
                            onClick = { petType = type },
                            label = { Text(label, fontSize = 11.sp, textAlign = TextAlign.Center) },
                            leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Пол
                Text("Пол", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = petGender == Pet.GENDER_MALE,
                        onClick = { petGender = Pet.GENDER_MALE },
                        label = { Text("♂ Мальчик") },
                        leadingIcon = { Icon(Icons.Outlined.Male, null, Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = petGender == Pet.GENDER_FEMALE,
                        onClick = { petGender = Pet.GENDER_FEMALE },
                        label = { Text("♀ Девочка") },
                        leadingIcon = { Icon(Icons.Outlined.Female, null, Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Размер
                Text("Размер", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Pet.SIZE_SMALL to "Маленький", Pet.SIZE_MEDIUM to "Средний", Pet.SIZE_LARGE to "Большой").forEach { (size, label) ->
                        FilterChip(
                            selected = petSize == size,
                            onClick = { petSize = size },
                            label = { Text(label) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = petBreed,
                        onValueChange = { petBreed = it },
                        label = { Text("Порода") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                    OutlinedTextField(
                        value = petColor,
                        onValueChange = { petColor = it },
                        label = { Text("Окрас") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== ХАРАКТЕРИСТИКИ =====
            SectionCard {
                SectionHeader(
                    title = "Характер и особенности",
                    subtitle = "Помогите найти идеального хозяина",
                    icon = Icons.Outlined.EmojiEmotions
                )

                Spacer(Modifier.height(16.dp))

                // Уровень активности
                Text("Уровень активности", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("low", "Спокойный", Icons.Outlined.SelfImprovement),
                        Triple("medium", "Умеренный", Icons.Outlined.DirectionsWalk),
                        Triple("high", "Энергичный", Icons.Outlined.Bolt)
                    ).forEach { (level, label, icon) ->
                        FilterChip(
                            selected = petEnergyLevel == level,
                            onClick = { petEnergyLevel = level },
                            label = { Text(label, fontSize = 12.sp) },
                            leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(Modifier.height(8.dp))

                // Чекбоксы в 2 колонки
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        SwitchRow("Приучен", isHouseTrained, Icons.Outlined.House) { isHouseTrained = it }
                        SwitchRow("Ладит с детьми", goodWithKids, Icons.Outlined.ChildCare) { goodWithKids = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SwitchRow("Ладит с животными", goodWithPets, Icons.Outlined.Groups) { goodWithPets = it }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== ЗДОРОВЬЕ =====
            SectionCard {
                SectionHeader(
                    title = "Здоровье",
                    subtitle = "Медицинская информация",
                    icon = Icons.Outlined.LocalHospital
                )

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        SwitchRow("Вакцинирован", isVaccinated, Icons.Outlined.Vaccines) { isVaccinated = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SwitchRow("Стерилизован", isSterilized, Icons.Outlined.FavoriteBorder) { isSterilized = it }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== ТЕГИ =====
            SectionCard {
                SectionHeader(
                    title = "Теги",
                    subtitle = "До 3 ключевых слов",
                    icon = Icons.Outlined.Label
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = petTraitsInput,
                    onValueChange = { input ->
                        petTraitsInput = input
                        petTraits = input.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3)
                    },
                    label = { Text("Введите через запятую") },
                    placeholder = { Text("Дружелюбный, игривый, спокойный") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors(),
                    supportingText = {
                        Text("${petTraits.size} / 3", color = if (petTraits.size >= 3) MaterialTheme.colorScheme.error else TextSecondary)
                    }
                )

                if (petTraits.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        petTraits.forEach { trait ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    petTraitsInput = petTraitsInput.removePrefix("$trait,").removePrefix("$trait ").removeSuffix(", $trait").removeSuffix(",$trait").trim()
                                    petTraits = petTraitsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3)
                                },
                                label = { Text(trait, fontSize = 12.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, "Удалить", Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== ОПИСАНИЕ =====
            SectionCard {
                SectionHeader(
                    title = "Описание",
                    subtitle = "Расскажите о питомце подробнее",
                    icon = Icons.Outlined.Description
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = petDescription,
                    onValueChange = { petDescription = it },
                    label = { Text("Опишите характер, привычки, историю") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ===== ОШИБКА =====
            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(uiState.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ===== КНОПКИ ДЕЙСТВИЙ =====
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            val mainImageUrl = allImageUrls.firstOrNull() ?: ""
                            val additionalPhotos = allImageUrls.drop(1).filter { it.isNotBlank() }
                            val currentShelterId = if (isEditing && uiState.currentPet != null) {
                                uiState.currentPet!!.shelter_id
                            } else ""
                            val pet = Pet(
                                id = petId ?: "",
                                shelter_id = currentShelterId,
                                name = petName,
                                age = petAge.toIntOrNull() ?: 0,
                                type = petType,
                                gender = petGender,
                                size = petSize,
                                breed = petBreed,
                                color = petColor,
                                description = petDescription,
                                photo_url = mainImageUrl,
                                additional_photos = additionalPhotos,
                                traits = petTraits.ifEmpty { null },
                                is_neutered = isSterilized,
                                has_vaccination = isVaccinated,
                                weight = null,
                                is_active = true
                            )
                            if (isEditing) viewModel.updatePet(pet) else viewModel.createPet(pet)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = petName.isNotBlank() && !uiState.isLoading && !uiState.uploadingImages,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            Icon(
                                if (isEditing) Icons.Outlined.Save else Icons.Outlined.Add,
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isEditing) "Сохранить изменения" else "Создать питомца",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Отмена")
                    }

                    // Кнопка удаления при редактировании
                    if (isEditing && petId != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Удалить питомца", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Диалог удаления
    if (showDeleteDialog && petId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
            },
            title = { Text("Удалить питомца?", fontWeight = FontWeight.Bold) },
            text = { Text("Это действие необратимо. Все фото будут удалены из хранилища.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePet(petId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

// ===================== PHOTO THUMBNAIL =====================
@Composable
private fun PhotoThumbnail(url: String, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        AsyncImage(model = url, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(Icons.Default.Close, "Удалить", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

// ===================== ADD PHOTO BUTTON =====================
@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Add, null, tint = Primary, modifier = Modifier.size(28.dp))
                Text("Фото", style = MaterialTheme.typography.labelSmall, color = Primary)
            }
        }
    }
}

// ===================== ADD PHOTO PLACEHOLDER =====================
@Composable
private fun AddPhotoPlaceholder(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(2.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AddAPhoto, null, tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Добавить фотографии", fontWeight = FontWeight.Medium, color = Primary)
            Text("Нажмите, чтобы выбрать из галереи", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ===================== SECTION CARD =====================
@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ===================== SECTION HEADER =====================
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ===================== SWITCH ROW =====================
@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onChecked: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (checked) Primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (checked) Primary else TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = Primary.copy(alpha = 0.3f))
        )
    }
}

// ===================== OUTLINED FIELD COLORS =====================
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    focusedLabelColor = Primary,
    cursorColor = Primary
)