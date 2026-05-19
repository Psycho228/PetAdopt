package com.example.petadopt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petadopt.ui.theme.Dislike
import com.example.petadopt.ui.theme.Like
import kotlin.math.abs

@Composable
fun PetCard(
    name: String,
    age: String,
    description: String,
    imageUrl: String,
    offsetX: Float = 0f
) {
    val cardShape = RoundedCornerShape(24.dp)

    // Прозрачность оверлея лайк/дизлайк — нарастает по мере свайпа
    val swipeProgress = (abs(offsetX) / 320f).coerceIn(0f, 1f)
    val isSwipingRight = offsetX > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .shadow(
                elevation = 16.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(cardShape)
            .background(Color.White)
    ) {
        // Фото питомца
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Градиент снизу для читаемости текста
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Оверлей LIKE (правый свайп)
        if (isSwipingRight && swipeProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Like.copy(alpha = swipeProgress * 0.25f))
            )
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
            ) {
                LikeDislikeLabel(text = "НРАВИТСЯ", color = Like, alpha = swipeProgress)
            }
        }

        // Оверлей DISLIKE (левый свайп)
        if (!isSwipingRight && swipeProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dislike.copy(alpha = swipeProgress * 0.25f))
            )
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopEnd)
            ) {
                LikeDislikeLabel(text = "ПРОПУСТИТЬ", color = Dislike, alpha = swipeProgress)
            }
        }

        // Информация о питомце внизу
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$age лет",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            // Тег-чипсы (пример)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetTag(text = "Дружелюбный")
                PetTag(text = "Активный")
            }
        }
    }
}

@Composable
private fun LikeDislikeLabel(text: String, color: Color, alpha: Float) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = alpha * 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun PetTag(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
