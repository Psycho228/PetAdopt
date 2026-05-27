package com.example.petadopt.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
    
    var isEditing by remember { mutableStateOf(petId != null) }
    
    // Список выбранных изображений (новые, ещё не загруженные)
    var selectedImages by remember { mutableStateOf(listOf<android.net.Uri>()) }
    
    // Получаем все изображения из ViewModel (существующие + новые загруженные)
    val allImageUrls = remember(uiState.existingImageUrls, uiState.uploadedImages) {
        uiState.existingImageUrls + uiState.uploadedImages
    }
    
    // Инициализация значений по умолчанию
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
    
    // Теги питомца (максимум 3)
    var petTraitsInput by remember { mutableStateOf("") }
    var petTraits by remember { mutableStateOf(emptyList<String>()) }
    
    // Загрузка данных питомца при редактировании
    LaunchedEffect(petId) {
        if (petId != null) {
            viewModel.loadPetById(petId)
        }
    }
    
    // Применение загруженных данных к полям (только при первой загрузке)
    var petLoaded by remember { mutableStateOf(false) }
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
        petLocation = "" // Не хранится в модели Pet
        petShelterName = "" // Не хранится в модели Pet
        petShelterContact = "" // Не хранится в модели Pet
        petEnergyLevel = "medium" // Не хранится в модели Pet
        isVaccinated = pet.has_vaccination
        isSterilized = pet.is_neutered
        isHouseTrained = false // Не хранится в модели Pet
        goodWithKids = true // Не хранится в модели Pet
        goodWithPets = true // Не хранится в модели Pet
        petTraits = pet.petTraits
        petTraitsInput = pet.petTraits.joinToString(", ")
    }

    // Launcher для выбора изображений
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = selectedImages + uris
            viewModel.uploadImages(context, uris)
        }
    }

    // Обработка успешной загрузки изображений
    LaunchedEffect(uiState.uploadedImages) {
        if (uiState.uploadedImages.isNotEmpty()) {
            // Очищаем выбранные изображения после загрузки
            selectedImages = emptyList()
            delay(100)
        }
    }

    // Обработка успешного сохранения
    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            delay(500)
            viewModel.clearCurrentPet()
            navController.popBackStack()
        }
    }
    
    // Очистка данных при выходе со экрана
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentPet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать питомца" else "Новый питомец") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Основная информация
            SectionTitle("Основная информация")
            Spacer(Modifier.height(12.dp))

            TextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Имя питомца") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Spacer(Modifier.height(12.dp))

            TextField(
                value = petAge,
                onValueChange = { 
                    petAge = it
                    petAgeYears = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0
                },
                label = { Text("Возраст (например, 3 года)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, null) }
            )

            Spacer(Modifier.height(16.dp))

            // Тип питомца
            Text("Тип питомца", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("cat" to "Кошка", "dog" to "Собака", "bird" to "Птица", "other" to "Другое").forEach { (type, label) ->
                    FilterChip(
                        selected = petType == type,
                        onClick = { petType = type },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Пол
            Text("Пол", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Pet.GENDER_MALE to "Мальчик", Pet.GENDER_FEMALE to "Девочка").forEach { (gender, label) ->
                    FilterChip(
                        selected = petGender == gender,
                        onClick = { petGender = gender },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Размер
            Text("Размер", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Pet.SIZE_SMALL to "Маленький", Pet.SIZE_MEDIUM to "Средний", Pet.SIZE_LARGE to "Большой").forEach { (size, label) ->
                    FilterChip(
                        selected = petSize == size,
                        onClick = { petSize = size },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            TextField(
                value = petBreed,
                onValueChange = { petBreed = it },
                label = { Text("Порода") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TextField(
                value = petColor,
                onValueChange = { petColor = it },
                label = { Text("Окрас") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Фотографии
            SectionTitle("Фотографии")
            Spacer(Modifier.height(12.dp))

            // Сетка изображений
            if (allImageUrls.isNotEmpty() || selectedImages.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Существующие фото с сервера
                    items(uiState.existingImageUrls) { url ->
                        val index = uiState.existingImageUrls.indexOf(url)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Фото питомца",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Кнопка удаления
                            IconButton(
                                onClick = { 
                                    viewModel.removeImage(index)
                                    viewModel.deleteImageFromServer(url)
                                },
                                modifier = Modifier
                                    .align(androidx.compose.ui.Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Удалить",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    // Новые загруженные фото
                    items(uiState.uploadedImages) { url ->
                        val index = uiState.existingImageUrls.size + uiState.uploadedImages.indexOf(url)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Фото питомца",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Кнопка удаления
                            IconButton(
                                onClick = { 
                                    viewModel.removeImage(index)
                                    viewModel.deleteImageFromServer(url)
                                },
                                modifier = Modifier
                                    .align(androidx.compose.ui.Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Удалить",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    // Новые выбранные фото (ещё не загружены)
                    items(selectedImages) { uri ->
                        val index = uiState.existingImageUrls.size + uiState.uploadedImages.size + selectedImages.indexOf(uri)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Фото питомца",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Кнопка удаления
                            IconButton(
                                onClick = { 
                                    viewModel.removeImage(index)
                                    val indexToRemove = selectedImages.indexOf(uri)
                                    selectedImages = selectedImages.filterIndexed { i, _ -> i != indexToRemove }
                                },
                                modifier = Modifier
                                    .align(androidx.compose.ui.Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Удалить",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    // Кнопка добавления
                    item {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Добавить фото", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (uiState.uploadingImages) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Загрузка...", color = TextSecondary)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Характеристики
            SectionTitle("Характеристики")
            Spacer(Modifier.height(12.dp))

            CheckboxRow("Приучен к лотку/поводку", isHouseTrained) { isHouseTrained = it }
            CheckboxRow("Ладит с детьми", goodWithKids) { goodWithKids = it }
            CheckboxRow("Ладит с животными", goodWithPets) { goodWithPets = it }

            Spacer(Modifier.height(16.dp))

            Text("Уровень активности", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("low" to "Низкий", "medium" to "Средний", "high" to "Высокий").forEach { (level, label) ->
                    FilterChip(
                        selected = petEnergyLevel == level,
                        onClick = { petEnergyLevel = level },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Медицинская информация
            SectionTitle("Медицинская информация")
            Spacer(Modifier.height(12.dp))

            CheckboxRow("Привит", isVaccinated) { isVaccinated = it }
            CheckboxRow("Стерилизован/кастрирован", isSterilized) { isSterilized = it }

            Spacer(Modifier.height(24.dp))

            // Теги питомца
            SectionTitle("Теги")
            Spacer(Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = petTraitsInput,
                    onValueChange = { input ->
                        petTraitsInput = input
                        // Разбиваем по запятым и фильтруем пустые
                        petTraits = input
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .take(3) // Максимум 3 тега
                    },
                    label = { Text("Теги (через запятую, максимум 3)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        val count = petTraits.count()
                        Text(
                            text = "$count / 3 тегов",
                            color = if (count > 3) MaterialTheme.colorScheme.error else TextSecondary
                        )
                    },
                    enabled = petTraits.count() < 3 || petTraitsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.count() == petTraits.count()
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Отображение выбранных тегов
                if (petTraits.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        petTraits.forEach { trait ->
                            FilterChip(
                                selected = true,
                                onClick = {
                                    // Удаление тега по клику
                                    petTraitsInput = petTraitsInput.removePrefix("$trait,").removePrefix("$trait ").removeSuffix(", $trait").removeSuffix(", $trait").removeSuffix(",")
                                    petTraits = petTraitsInput
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                        .take(3)
                                },
                                label = { Text(trait) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary.copy(alpha = 0.2f),
                                    selectedLabelColor = Primary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Описание
            SectionTitle("Описание")
            Spacer(Modifier.height(12.dp))

            TextField(
                value = petDescription,
                onValueChange = { petDescription = it },
                label = { Text("Описание питомца") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6
            )

            Spacer(Modifier.height(24.dp))

            // Информация о приюте
            SectionTitle("Информация о приюте")
            Spacer(Modifier.height(12.dp))

            TextField(
                value = petShelterName,
                onValueChange = { petShelterName = it },
                label = { Text("Название приюта") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TextField(
                value = petShelterContact,
                onValueChange = { petShelterContact = it },
                label = { Text("Контакт приюта") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TextField(
                value = petLocation,
                onValueChange = { petLocation = it },
                label = { Text("Город/район") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        // Основное изображение - первое из всех доступных
                        val mainImageUrl = allImageUrls.firstOrNull() ?: ""
                        
                        // Дополнительные фото - все остальные
                        val additionalPhotos = allImageUrls.drop(1).filter { it.isNotBlank() }
                        
                        // shelter_id: при редактировании берём из загруженного питомца, при создании - пустой
                        val currentShelterId = if (isEditing && uiState.currentPet != null) {
                            uiState.currentPet!!.shelter_id
                        } else {
                            ""
                        }
                        
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
                        android.util.Log.d("AddEditPet", "Saving pet: id=${pet.id}, name=${pet.name}")
                        android.util.Log.d("AddEditPet", "  shelter_id=$currentShelterId, isEditing=$isEditing")
                        android.util.Log.d("AddEditPet", "  traits=${pet.traits}, count=${pet.traits?.size ?: 0}")
                        android.util.Log.d("AddEditPet", "  additional_photos=${pet.additional_photos}, count=${pet.additional_photos?.size ?: 0}")
                        android.util.Log.d("AddEditPet", "  photo_url=${pet.photo_url}")

                        if (isEditing) {
                            viewModel.updatePet(pet)
                        } else {
                            viewModel.createPet(pet)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = petName.isNotBlank() && !uiState.isLoading && !uiState.uploadingImages
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(if (isEditing) "Сохранить" else "Создать")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        "Ошибка: ${uiState.error}",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(color = Primary, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}