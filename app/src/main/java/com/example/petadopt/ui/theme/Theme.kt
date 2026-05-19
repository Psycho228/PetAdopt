package com.example.petadopt.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun PetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = Card,
            primaryContainer = Primary.copy(alpha = 0.12f),
            background = Background,
            surface = Card,
            surfaceVariant = SurfaceLight,
            secondary = PrimaryVariant,
            error = Like
        ),
        typography = Typography,
        content = content
    )
}