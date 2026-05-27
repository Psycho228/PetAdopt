package com.example.petadopt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petadopt.ui.components.PrimaryButton
import com.example.petadopt.ui.theme.Background
import com.example.petadopt.ui.theme.Primary
import com.example.petadopt.ui.theme.TextSecondary

@Composable
fun QuestionnaireCompleteScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка успеха
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Заголовок
        Text(
            text = "Опросник завершён!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )

        Spacer(Modifier.height(16.dp))

        // Описание
        Text(
            text = "Ваши ответы сохранены и отправлены на оценку.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Теперь вы можете искать питомцев, которые подходят именно вам.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // Кнопка продолжения
        PrimaryButton(
            text = "Начать поиск",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}
