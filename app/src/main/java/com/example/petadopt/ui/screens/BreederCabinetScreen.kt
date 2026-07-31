package com.example.petadopt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.data.model.BreederProfile
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.AccountViewModel
import com.example.petadopt.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreederCabinetScreen(
    onLogout: () -> Unit,
    onAddListing: () -> Unit,
    onEditListing: (String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var editingProfile by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadCabinet() }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            editingProfile = false
            viewModel.consumeSaved()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Хвостики", fontWeight = FontWeight.Bold)
                        Text(
                            "Кабинет заводчика",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        if (state.isLoading && state.myProfile == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (state.myProfile == null || editingProfile) {
                        BreederProfileForm(
                            profile = state.myProfile,
                            saving = state.isSaving,
                            onSave = viewModel::saveProfile
                        )
                    } else {
                        BreederProfileSummary(
                            profile = state.myProfile!!,
                            onEdit = { editingProfile = true }
                        )
                    }
                }

                state.error?.let { message ->
                    item {
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (state.myProfile != null && !editingProfile) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Мои объявления",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalButton(onClick = onAddListing) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.padding(3.dp))
                                Text("Добавить")
                            }
                        }
                    }
                    if (state.myListings.isEmpty()) {
                        item {
                            Text(
                                "Объявлений пока нет.",
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = TextSecondary
                            )
                        }
                    } else {
                        items(state.myListings, key = { it.id }) { listing ->
                            CabinetListingCard(
                                listing = listing,
                                onEdit = { onEditListing(listing.id) },
                                onStatus = { status ->
                                    viewModel.updateListingStatus(listing.id, status)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Выйти из аккаунта?", fontWeight = FontWeight.Bold) },
            text = { Text("Вы будете перенаправлены на экран авторизации.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        accountViewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
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

@Composable
private fun BreederProfileSummary(profile: BreederProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        profile.kennelName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(profile.city, color = TextSecondary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                }
            }
            Text(verificationLabel(profile.verificationStatus), color = statusColor(profile.verificationStatus))
            profile.moderationNote?.takeIf { it.isNotBlank() }?.let {
                Text("Комментарий: $it", color = MaterialTheme.colorScheme.error)
            }
            if (profile.description.isNotBlank()) Text(profile.description)
        }
    }
}

@Composable
private fun BreederProfileForm(
    profile: BreederProfile?,
    saving: Boolean,
    onSave: (BreederProfile) -> Unit
) {
    var kennelName by remember(profile?.id) { mutableStateOf(profile?.kennelName.orEmpty()) }
    var city by remember(profile?.id) { mutableStateOf(profile?.city.orEmpty()) }
    var phone by remember(profile?.id) { mutableStateOf(profile?.phone.orEmpty()) }
    var website by remember(profile?.id) { mutableStateOf(profile?.website.orEmpty()) }
    var breeds by remember(profile?.id) { mutableStateOf(profile?.breeds?.joinToString(", ").orEmpty()) }
    var description by remember(profile?.id) { mutableStateOf(profile?.description.orEmpty()) }
    val valid = kennelName.isNotBlank() && city.isNotBlank() && phone.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = Primary)
            Text(
                if (profile == null) "Заполните профиль питомника" else "Профиль заводчика",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Профиль будет опубликован после проверки.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            MarketplaceField(kennelName, { kennelName = it }, "Название питомника")
            MarketplaceField(city, { city = it }, "Город")
            MarketplaceField(phone, { phone = it }, "Телефон")
            MarketplaceField(website, { website = it }, "Сайт (необязательно)")
            MarketplaceField(breeds, { breeds = it }, "Породы через запятую")
            MarketplaceField(
                description,
                { description = it },
                "О питомнике",
                singleLine = false
            )
            PrimaryButton(
                text = if (saving) "Сохраняем..." else "Отправить на проверку",
                onClick = {
                    onSave(
                        (profile ?: BreederProfile()).copy(
                            kennelName = kennelName,
                            city = city,
                            phone = phone,
                            website = website.ifBlank { null },
                            breeds = breeds.split(",").map(String::trim).filter(String::isNotBlank),
                            description = description
                        )
                    )
                },
                enabled = valid && !saving
            )
        }
    }
}

@Composable
private fun MarketplaceField(
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
        minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun CabinetListingCard(
    listing: SaleListing,
    onEdit: () -> Unit,
    onStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        listing.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(listing.breed, color = TextSecondary)
                }
                Text(statusLabel(listing.status), color = statusColor(listing.status))
            }
            listing.moderationNote?.takeIf { it.isNotBlank() }?.let {
                Text("Комментарий: $it", color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Редактировать") }
                if (listing.status == SaleListing.STATUS_AVAILABLE) {
                    TextButton(onClick = { onStatus(SaleListing.STATUS_RESERVED) }) {
                        Text("Резерв")
                    }
                    TextButton(onClick = { onStatus(SaleListing.STATUS_SOLD) }) {
                        Text("Продано")
                    }
                }
                if (listing.status != SaleListing.STATUS_ARCHIVED) {
                    TextButton(onClick = { onStatus(SaleListing.STATUS_ARCHIVED) }) {
                        Text("Архив")
                    }
                }
            }
        }
    }
}

private fun verificationLabel(status: String) = when (status) {
    BreederProfile.STATUS_VERIFIED -> "Профиль проверен"
    BreederProfile.STATUS_REJECTED -> "Нужны исправления"
    else -> "Профиль на проверке"
}

private fun statusLabel(status: String) = when (status) {
    SaleListing.STATUS_AVAILABLE -> "Опубликовано"
    SaleListing.STATUS_RESERVED -> "Резерв"
    SaleListing.STATUS_SOLD -> "Продано"
    SaleListing.STATUS_REJECTED -> "Нужны исправления"
    SaleListing.STATUS_ARCHIVED -> "Архив"
    SaleListing.STATUS_DRAFT -> "Черновик"
    else -> "На модерации"
}

@Composable
private fun statusColor(status: String) = when (status) {
    "verified", "available" -> Primary
    "rejected" -> MaterialTheme.colorScheme.error
    else -> TextSecondary
}
