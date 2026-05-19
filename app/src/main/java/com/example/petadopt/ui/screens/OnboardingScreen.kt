package com.example.petadopt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petadopt.ui.components.Screen
import com.example.petadopt.ui.theme.TextSecondary
import com.example.petadopt.ui.components.PrimaryButton
@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onAccount: () -> Unit
) {
    Screen {

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Найди питомца 🐾",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Мы подбираем животных, которые подходят именно тебе",
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Начать",
            onClick = onStart
        )

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onAccount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Профиль")
        }
    }
}