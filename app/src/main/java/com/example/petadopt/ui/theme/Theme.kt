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
            onPrimaryContainer = PrimaryVariant,
            background = Background,
            onBackground = TextPrimary,
            surface = Card,
            onSurface = TextPrimary,
            surfaceVariant = SurfaceLight,
            onSurfaceVariant = TextSecondary,
            surfaceTint = Primary,
            secondary = Secondary,
            onSecondary = Card,
            secondaryContainer = AccentSoft,
            onSecondaryContainer = TextPrimary,
            outline = TextSecondary.copy(alpha = 0.55f),
            outlineVariant = SurfaceLight,
            error = Dislike,
            onError = Card
        ),
        typography = Typography,
        content = content
    )
}
