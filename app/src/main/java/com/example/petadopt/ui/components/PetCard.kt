package com.example.petadopt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petadopt.ui.theme.Dislike
import com.example.petadopt.ui.theme.Like
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.Secondary
import kotlin.math.abs

@Composable
fun PetCard(
    name: String,
    age: String,
    description: String,
    imageUrl: String,
    traits: List<String> = emptyList(),
    offsetX: Float = 0f
) {
    val cardShape = RoundedCornerShape(28.dp)
    val swipeProgress = (abs(offsetX) / 320f).coerceIn(0f, 1f)
    val isSwipingRight = offsetX > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .shadow(
                elevation = 18.dp,
                shape = cardShape,
                ambientColor = Primary.copy(alpha = 0.14f),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(cardShape)
            .background(Color.White)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.42f),
                            Color.Black.copy(alpha = 0.86f)
                        )
                    )
                )
        )

        AdoptionBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
        )

        if (swipeProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSwipingRight) {
                            Like.copy(alpha = swipeProgress * 0.24f)
                        } else {
                            Dislike.copy(alpha = swipeProgress * 0.24f)
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .align(if (isSwipingRight) Alignment.TopStart else Alignment.TopEnd)
            ) {
                SwipeLabel(
                    text = if (isSwipingRight) "Нравится" else "Пропустить",
                    color = if (isSwipingRight) Like else Dislike,
                    alpha = swipeProgress
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Secondary.copy(alpha = 0.94f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "$age лет",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            if (traits.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    traits.take(3).forEach { trait ->
                        PetTag(text = trait)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Ищет дом",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SwipeLabel(text: String, color: Color, alpha: Float) {
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = alpha * 0.92f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun PetTag(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
