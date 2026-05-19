package com.example.petadopt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// Порог пикселей для засчитывания свайпа
private const val SWIPE_THRESHOLD = 320f

// Максимальный угол поворота карточки при свайпе (градусы)
private const val MAX_ROTATION = 18f

@Composable
fun SwipeCard(
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    content: @Composable (offsetX: Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val currentOnSwipedLeft by rememberUpdatedState(onSwipedLeft)
    val currentOnSwipedRight by rememberUpdatedState(onSwipedRight)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = (offsetX.value / SWIPE_THRESHOLD) * MAX_ROTATION
                val scale = 1f - (abs(offsetX.value) / 4000f).coerceAtMost(0.05f)
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > SWIPE_THRESHOLD -> {
                                    offsetX.animateTo(
                                        targetValue = 1200f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                    currentOnSwipedRight()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                offsetX.value < -SWIPE_THRESHOLD -> {
                                    offsetX.animateTo(
                                        targetValue = -1200f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                    currentOnSwipedLeft()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                                else -> {
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { _, dragAmount ->
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y * 0.3f)
                    }
                }
            }
    ) {
        content(offsetX.value)
    }
}
