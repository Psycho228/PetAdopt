package com.example.petadopt.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.MarketplaceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListingFormScreen(
    listingId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var initialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(Pet.TYPE_CAT) }
    var gender by remember { mutableStateOf(Pet.GENDER_MALE) }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var vaccinated by remember { mutableStateOf(false) }
    var vetPassport by remember { mutableStateOf(false) }
    var pedigree by remember { mutableStateOf(false) }
    var chipped by remember { mutableStateOf(false) }
    var delivery by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remainingSlots = MAX_LISTING_PHOTOS - photoUrls.size
        viewModel.uploadListingPhotos(context, uris.take(remainingSlots))
    }

    LaunchedEffect(listingId) {
        viewModel.loadCabinet()
        listingId?.let(viewModel::loadListing)
    }
    LaunchedEffect(state.selectedListing, state.myProfile) {
        val listing = state.selectedListing
        if (!initialized && (listingId == null || listing != null)) {
            listing?.let {
                name = it.name
                type = it.type
                gender = it.gender
                breed = it.breed
                birthDate = it.birthDate.orEmpty()
                price = it.price.toString()
                description = it.description
                photoUrls = (listOf(it.photoUrl) + it.additionalPhotos)
                    .filter(String::isNotBlank)
                    .distinct()
                    .take(MAX_LISTING_PHOTOS)
                vaccinated = it.vaccinated
                vetPassport = it.vetPassport
                pedigree = it.pedigree
                chipped = it.chipped
                delivery = it.deliveryAvailable
            }
            initialized = true
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }
    LaunchedEffect(state.uploadedPhotoUrls) {
        if (state.uploadedPhotoUrls.isNotEmpty()) {
            photoUrls = (photoUrls + state.uploadedPhotoUrls)
                .distinct()
                .take(MAX_LISTING_PHOTOS)
            viewModel.consumeUploadedListingPhotos()
        }
    }

    val valid = name.isNotBlank() &&
        breed.isNotBlank() &&
        price.toDoubleOrNull() != null &&
        description.isNotBlank() &&
        photoUrls.isNotEmpty() &&
        state.myProfile != null

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Хвостики", fontWeight = FontWeight.Bold)
                        Text(
                            if (listingId == null) "Новое объявление" else "Редактирование",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "После сохранения объявление отправится на модерацию.",
                color = TextSecondary
            )
            FormField(name, { name = it }, "Кличка")
            FormField(breed, { breed = it }, "Порода")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == Pet.TYPE_CAT,
                    onClick = { type = Pet.TYPE_CAT },
                    label = { Text("Кошка") }
                )
                FilterChip(
                    selected = type == Pet.TYPE_DOG,
                    onClick = { type = Pet.TYPE_DOG },
                    label = { Text("Собака") }
                )
                FilterChip(
                    selected = type == Pet.TYPE_OTHER,
                    onClick = { type = Pet.TYPE_OTHER },
                    label = { Text("Другое") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = gender == Pet.GENDER_MALE,
                    onClick = { gender = Pet.GENDER_MALE },
                    label = { Text("Мальчик") }
                )
                FilterChip(
                    selected = gender == Pet.GENDER_FEMALE,
                    onClick = { gender = Pet.GENDER_FEMALE },
                    label = { Text("Девочка") }
                )
            }
            BirthDateField(
                value = birthDate,
                onValueChange = { birthDate = it }
            )
            OutlinedTextField(
                value = price,
                onValueChange = { value -> price = value.filter { it.isDigit() || it == '.' } },
                label = { Text("Цена, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            ListingPhotoPicker(
                photoUrls = photoUrls,
                uploading = state.isUploadingPhoto,
                onPickPhoto = { photoPicker.launch("image/*") },
                onRemovePhoto = { index ->
                    photoUrls = photoUrls.filterIndexed { photoIndex, _ -> photoIndex != index }
                },
                onMakeMain = { index ->
                    photoUrls = listOf(photoUrls[index]) + photoUrls.filterIndexed { photoIndex, _ ->
                        photoIndex != index
                    }
                }
            )
            FormField(
                description,
                { description = it },
                "Описание",
                singleLine = false
            )
            ListingSwitch("Привит", vaccinated) { vaccinated = it }
            ListingSwitch("Есть ветеринарный паспорт", vetPassport) { vetPassport = it }
            ListingSwitch("Есть родословная", pedigree) { pedigree = it }
            ListingSwitch("Чипирован", chipped) { chipped = it }
            ListingSwitch("Возможна доставка", delivery) { delivery = it }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            PrimaryButton(
                text = if (state.isSaving) "Отправляем..." else "Отправить на модерацию",
                enabled = valid && !state.isSaving && !state.isUploadingPhoto,
                onClick = {
                    val current = state.selectedListing ?: SaleListing()
                    viewModel.saveListing(
                        current.copy(
                            breederId = state.myProfile!!.id,
                            name = name,
                            type = type,
                            gender = gender,
                            breed = breed,
                            birthDate = birthDate.ifBlank { null },
                            price = price.toDouble(),
                            description = description,
                            photoUrl = photoUrls.first(),
                            additionalPhotos = photoUrls.drop(1),
                            vaccinated = vaccinated,
                            vetPassport = vetPassport,
                            pedigree = pedigree,
                            chipped = chipped,
                            deliveryAvailable = delivery
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val initialMillis = remember(value) { value.toUtcMillisOrNull() }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onValueChange(it.toIsoDate()) }
                        showDatePicker = false
                    }
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Дата рождения",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value.toDisplayDate(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) TextSecondary else MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = "Выбрать дату"
        )
    }
}

@Composable
private fun ListingPhotoPicker(
    photoUrls: List<String>,
    uploading: Boolean,
    onPickPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onMakeMain: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Фотографии",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Первое фото будет главным",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "${photoUrls.size}/$MAX_LISTING_PHOTOS",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }

        if (photoUrls.isEmpty()) {
            OutlinedButton(
                onClick = onPickPhoto,
                enabled = !uploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(if (uploading) "Загружаем фото..." else "Выбрать фото из галереи")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = photoUrls.first(),
                    contentDescription = "Главное фото объявления",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "Главное фото",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(photoUrls) { index, url ->
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !uploading) { onMakeMain(index) }
                            .then(
                                if (index == 0) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Фото ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { onRemovePhoto(index) },
                            enabled = !uploading,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(26.dp)
                                .background(Color.Black.copy(alpha = 0.58f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Удалить фото",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                if (photoUrls.size < MAX_LISTING_PHOTOS) {
                    item {
                        OutlinedButton(
                            onClick = onPickPhoto,
                            enabled = !uploading,
                            modifier = Modifier.size(82.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "Добавить фотографии"
                            )
                        }
                    }
                }
            }

            Text(
                text = if (photoUrls.size < MAX_LISTING_PHOTOS) {
                    "Нажмите на миниатюру, чтобы сделать её главной"
                } else {
                    "Добавлено максимальное количество фотографий"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (uploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

private const val MAX_LISTING_PHOTOS = 6

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun String.toUtcMillisOrNull(): Long? = runCatching {
    LocalDate.parse(this)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

private fun Long.toIsoDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()
    .toString()

private fun String.toDisplayDate(): String = ifBlank { "Выберите дату" }.let { date ->
    runCatching { LocalDate.parse(date).format(displayDateFormatter) }.getOrDefault(date)
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun ListingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
