package com.example.petadopt.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petadopt.ui.components.PetCard
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.components.SwipeCard
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.Secondary
import com.example.petadopt.ui.theme.TextPrimary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.ui.theme.WarmSurface
import com.example.petadopt.viewmodel.AccountViewModel
import com.example.petadopt.viewmodel.SwipeViewModel

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
    val remaining = (pets.size - currentIndex).coerceAtLeast(0)
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
            title = { Text("Выйти из профиля?") },
            text = { Text("Текущая подборка сохранится, вы сможете вернуться позже.") },
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
                    Text("Остаться")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(WarmSurface, Background)
                )
            )
    ) {
        TopBar(
            onAccount = onAccount,
            onMatches = onMatches,
            onLogout = { showLogoutDialog = true }
        )

        MatchHeader(remaining = remaining, total = pets.size)

        ProgressCounter(currentIndex = currentIndex, total = pets.size)

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
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
                EmptyPetState(onRefresh = { viewModel.refreshPets() })
            }
        }

        if (currentPet != null) {
            BottomActions(
                onDetails = { onDetails(currentPet.id) },
                onMatches = onMatches
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun TopBar(
    onAccount: () -> Unit,
    onMatches: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Primary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Хвостики",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "подборка с заботой",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onMatches) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Избранные питомцы",
                    tint = Secondary
                )
            }
            IconButton(onClick = onAccount) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Профиль",
                    tint = Primary
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = "Выйти",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MatchHeader(remaining: Int, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Кто может стать вашим другом?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (total > 0) {
                "Еще $remaining анкет питомцев в этой подборке"
            } else {
                "Обновите список, чтобы увидеть доступных питомцев"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ProgressCounter(currentIndex: Int, total: Int) {
    if (total <= 0) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val progress by animateFloatAsState(
            targetValue = ((currentIndex + 1).toFloat() / total).coerceIn(0f, 1f),
            animationSpec = tween(300),
            label = "swipe-progress"
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = Color.White.copy(alpha = 0.72f)
        )

        Text(
            text = "${currentIndex + 1}/$total",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyPetState(onRefresh: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.86f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                tint = Secondary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Подборка закончилась",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Мы покажем новых питомцев, когда приюты обновят анкеты.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Обновить")
            }
        }
    }
}

@Composable
private fun BottomActions(
    onDetails: () -> Unit,
    onMatches: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        PrimaryButton(
            text = "Познакомиться ближе",
            onClick = onDetails
        )

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onMatches,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
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
            Text("Мои симпатии", fontWeight = FontWeight.SemiBold)
        }
    }
}
