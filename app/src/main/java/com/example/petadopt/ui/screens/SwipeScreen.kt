package com.example.petadopt.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.ui.components.PetCard
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.components.SwipeCard
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.SwipeViewModel
import com.example.petadopt.viewmodel.AccountViewModel

@Composable
fun SwipeScreen(
    onDetails: (String) -> Unit,
    onMatches: () -> Unit,
    onAccount: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SwipeViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    val pets by viewModel.pets.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val currentPet = pets.getOrNull(currentIndex)
    
    // Объединяем основное фото и дополнительные, фильтруем пустые строки
    val allImages = remember(currentPet?.imageUrl, currentPet?.images) {
        listOfNotNull(currentPet?.imageUrl?.takeIf { it.isNotBlank() }) + 
        currentPet?.images?.filter { it.isNotBlank() }.orEmpty()
    }
    val displayImage = allImages.firstOrNull() ?: "https://via.placeholder.com/300"

    LaunchedEffect(Unit) {
        viewModel.loadPets()
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
                    onLogout()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            onAccount = onAccount,
            onMatches = onMatches,
            onLogout = { showLogoutDialog = true }
        )

        Spacer(Modifier.height(4.dp))

        ProgressCounter(currentIndex = currentIndex, total = pets.size)

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (currentPet != null) {
                SwipeCard(
                    onSwipedLeft = { viewModel.dislikePet() },
                    onSwipedRight = { viewModel.likePet(currentPet) }
                ) { offsetX ->
                    PetCard(
                        name = currentPet.name,
                        age = currentPet.age.toString(),
                        description = currentPet.description,
                        imageUrl = displayImage,
                        traits = currentPet.petTraits,
                        offsetX = offsetX
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Питомцы закончились",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Вам понравились все питомцы из этой подборки",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { viewModel.refreshPets() },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Обновить список", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (currentPet != null) {
            BottomActions(
                onDetails = { petId -> onDetails(petId) },
                onMatches = onMatches,
                currentPetId = currentPet.id
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TopBar(
    onAccount: () -> Unit,
    onMatches: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Хвостики",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onMatches) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Совпадения",
                        tint = Color(0xFF888888)
                    )
                }
                IconButton(onClick = onAccount) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Профиль",
                        tint = Color(0xFF444444)
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Выйти",
                        tint = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCounter(currentIndex: Int, total: Int) {
    if (total > 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val progress by animateFloatAsState(
                targetValue = if (total > 0) currentIndex.toFloat() / total else 0f,
                animationSpec = tween(300)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Primary,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Осталось $total",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomActions(
    onDetails: (String) -> Unit,
    onMatches: () -> Unit,
    currentPetId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        PrimaryButton(
            text = "Подробнее о питомце",
            onClick = { onDetails(currentPetId) }
        )

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onMatches,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Мои совпадения", fontWeight = FontWeight.SemiBold)
        }
    }
}
