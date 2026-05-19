package com.example.petadopt.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.*
import com.example.petadopt.viewmodel.SwipeViewModel
import kotlin.math.abs

// Функция для правильного склонения слова "год/года/лет"
fun getAgeWord(age: Int): String {
    val lastDigit = age % 10
    val lastTwoDigits = age % 100
    
    return when {
        lastTwoDigits in 11..19 -> "лет"
        lastDigit == 1 -> "год"
        lastDigit in 2..4 -> "года"
        else -> "лет"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    viewModel: SwipeViewModel = hiltViewModel()
) {
    val pet by viewModel.selectedPet.collectAsState()

    if (pet == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Питомец не выбран",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
        return
    }

    val p = pet!!
    val scrollState = rememberScrollState()
    
    // Состояние для пагинации изображений
    val allImages = remember(p.imageUrl, p.images) {
        listOfNotNull(p.imageUrl.takeIf { it.isNotBlank() }) + p.images.filter { it.isNotBlank() }
    }
    val pagerState = rememberPagerState(pageCount = { allImages.size })
    
    // Анимация для кнопки назад
    val buttonScale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(200)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        // Фото с пагинацией
        Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
            if (allImages.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(allImages[page])
                            .crossfade(true)
                            .build(),
                        contentDescription = "${p.name} - фото ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(p.imageUrl.ifEmpty { "https://via.placeholder.com/300" })
                        .crossfade(true)
                        .build(),
                    contentDescription = p.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Градиент для лучшей читаемости текста
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f)
                            ),
                            startY = 0f,
                            endY = 360f
                        )
                    )
            )

            // Индикатор страниц
            if (allImages.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(allImages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }

            // Верхняя панель с кнопками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка назад
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.scale(buttonScale.value)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Кнопка избранного
                Surface(
                    onClick = { /* TODO: Добавить в избранное */ },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.scale(buttonScale.value)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Бейдж с возрастом
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${p.ageYearsInt} ${getAgeWord(p.ageYearsInt)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Заголовок с именем
            Text(
                text = p.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 32.sp
            )

            Spacer(Modifier.height(8.dp))

            // Вид питомца (заглушка, можно добавить в модель)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Питомец из приюта",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Разделитель с заголовком
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(Primary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "О питомце",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Описание
            Text(
                text = p.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontSize = 16.sp,
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(32.dp))

            // Характеристики из модели Pet
            Text(
                text = "Характеристики",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            // Сетка характеристик 2 колонки
            Column(modifier = Modifier.fillMaxWidth()) {
                // row 1: возраст + тип
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        icon = Icons.Default.DateRange,
                        label = "Возраст",
                        value = p.age.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Info,
                        label = "Тип",
                        value = p.getTypeDisplay(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // row 2: пол + размер
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        icon = Icons.Default.Person,
                        label = "Пол",
                        value = p.getGenderDisplay(),
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Home,
                        label = "Размер",
                        value = p.getSizeDisplay(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // row 3: активность + порода
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        icon = Icons.Default.Star,
                        label = "Активность",
                        value = p.getEnergyLevelDisplay(),
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Info,
                        label = "Порода",
                        value = p.breed.ifEmpty { "Не указана" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // row 4: прививки + стерилизован
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        icon = Icons.Default.CheckCircle,
                        label = "Прививки",
                        value = if (p.isVaccinated) "Да" else "Нет",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Favorite,
                        label = "Стерилизован",
                        value = if (p.isSterilized) "Да" else "Нет",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // row 5: приучен + с детьми
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        icon = Icons.Default.Check,
                        label = "Приучен к лотку",
                        value = if (p.isHouseTrained) "Да" else "Нет",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Person,
                        label = "Ладит с детьми",
                        value = if (p.goodWithKids) "Да" else "Нет",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Кнопки действий
            PrimaryButton(
                text = "Подать заявку",
                onClick = {
                    navController.navigate("application/${p.id}/${Uri.encode(p.name)}")
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAccount,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Профиль",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F7))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(22.dp)
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
