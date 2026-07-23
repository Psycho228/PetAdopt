package com.example.petadopt.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.components.Screen
import com.example.petadopt.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onAccount: () -> Unit
) {
    Screen {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Хвостики",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Помогаем найти питомца, который подойдет именно вам",
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
