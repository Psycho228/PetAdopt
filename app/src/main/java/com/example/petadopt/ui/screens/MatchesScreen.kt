package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petadopt.data.model.Pet
import com.example.petadopt.ui.theme.Like
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.viewmodel.SwipeViewModel

@Composable
fun MatchesScreen(
    onPetClick: (String) -> Unit,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    viewModel: SwipeViewModel = hiltViewModel()
) {
    val likedPets by viewModel.likedPets.collectAsState()

    // Перезагружаем данные при входе на экран
    LaunchedEffect(Unit) {
        viewModel.loadLikedPets()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = "Мои совпадения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAccount) {
                Text("Профиль")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (likedPets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Like.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Пока нет совпадений",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Свайпайте вправо, чтобы добавить питомца",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Text(
                text = "${likedPets.size} ${declination(likedPets.size)}",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(likedPets, key = { it.id }) { pet ->
                    MatchCard(
                        pet = pet,
                        onClick = {
                            viewModel.selectPet(pet)
                            onPetClick(pet.id)
                        },
                        onRemove = {
                            viewModel.removeLike(pet)
                        }
                    )
                }
            }
        }
    }
}

private fun declination(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "питомец"
    count % 10 in 2..4 && (count % 100 !in 12..14) -> "питомца"
    else -> "питомцев"
}

@Composable
private fun MatchCard(pet: Pet, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pet.imageUrl.ifEmpty { "https://via.placeholder.com/300" })
                    .crossfade(true)
                    .build(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                Primary.copy(alpha = 0.1f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${pet.age} лет",
                            fontSize = 12.sp,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = pet.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
            }

            IconButton(
                onClick = { onRemove() },
                modifier = Modifier.padding(end = 8.dp).size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = TextSecondary
                )
            }
        }
    }
}
