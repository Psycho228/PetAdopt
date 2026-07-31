package com.example.petadopt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.data.model.Pet
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListingFormScreen(
    listingId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var initialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(Pet.TYPE_CAT) }
    var gender by remember { mutableStateOf(Pet.GENDER_MALE) }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var vaccinated by remember { mutableStateOf(false) }
    var vetPassport by remember { mutableStateOf(false) }
    var pedigree by remember { mutableStateOf(false) }
    var chipped by remember { mutableStateOf(false) }
    var delivery by remember { mutableStateOf(false) }

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
                photoUrl = it.photoUrl
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

    val valid = name.isNotBlank() &&
        breed.isNotBlank() &&
        price.toDoubleOrNull() != null &&
        description.isNotBlank() &&
        photoUrl.isNotBlank() &&
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
            FormField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = "Дата рождения (ГГГГ-ММ-ДД)"
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
            FormField(photoUrl, { photoUrl = it }, "Ссылка на главное фото")
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
                enabled = valid && !state.isSaving,
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
                            photoUrl = photoUrl,
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
