package com.example.petadopt.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petadopt.data.model.SaleListing
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.MarketplaceViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListingDetailScreen(
    listingId: String,
    onBack: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(listingId) { viewModel.loadListing(listingId) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Хвостики", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Primary) }

            state.selectedListing == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(state.error ?: "Объявление не найдено") }

            else -> {
                val listing = state.selectedListing!!
                val breeder = state.selectedBreeder
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = listing.photoUrl,
                        contentDescription = listing.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    listing.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(listing.breed, color = TextSecondary)
                            }
                            Text(
                                "${NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(listing.price)} ₽",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        DetailFacts(listing)

                        Text("О питомце", fontWeight = FontWeight.Bold)
                        Text(listing.description, style = MaterialTheme.typography.bodyLarge)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                    Spacer(Modifier.padding(4.dp))
                                    Text(
                                        breeder?.kennelName ?: "Проверенный заводчик",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                breeder?.city?.let {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                        Text(it, color = TextSecondary)
                                    }
                                }
                                breeder?.description?.takeIf { it.isNotBlank() }?.let {
                                    Text(it)
                                }
                            }
                        }

                        PrimaryButton(
                            text = "Позвонить заводчику",
                            onClick = {
                                breeder?.phone?.let { phone ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))
                                    )
                                }
                            },
                            enabled = !breeder?.phone.isNullOrBlank()
                        )
                        Text(
                            "Хвостики не принимает оплату и не гарантирует сделку. Проверьте документы и здоровье питомца лично.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailFacts(listing: SaleListing) {
    val facts = buildList {
        add(if (listing.gender == "male") "Мальчик" else "Девочка")
        if (listing.vaccinated) add("Привит")
        if (listing.vetPassport) add("Ветпаспорт")
        if (listing.pedigree) add("Родословная")
        if (listing.chipped) add("Чипирован")
        if (listing.deliveryAvailable) add("Возможна доставка")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        facts.take(3).forEach { fact ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Primary.copy(alpha = 0.12f)
            ) {
                Text(
                    fact,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
