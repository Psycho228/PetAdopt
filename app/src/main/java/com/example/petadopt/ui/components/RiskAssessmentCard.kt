package com.example.petadopt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petadopt.data.model.*

/**
 * UI компонент для отображения оценки рисков от GigaChat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentCard(
    assessment: GigaChatRiskAssessment,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val riskColor = getRiskColor(assessment.overallRisk)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с уровнем риска
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Оценка рисков GigaChat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                onDismiss?.let {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Индикатор уровня риска
            RiskBadge(riskLevel = assessment.overallRisk)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Score
            Text(
                text = "Уровень риска: ${assessment.riskScore}/100",
                style = MaterialTheme.typography.titleMedium,
                color = riskColor,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Рекомендация
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = assessment.recommendationText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подробный анализ
            Text(
                text = "Анализ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assessment.detailedAnalysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Факторы риска
            if (assessment.riskFactors.isNotEmpty()) {
                Text(
                    text = "Выявленные риски",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                assessment.riskFactors.forEach { factor ->
                    RiskFactorItem(factor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Положительные факторы
            if (assessment.positiveFactors.isNotEmpty()) {
                Text(
                    text = "Положительные факторы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                assessment.positiveFactors.forEach { factor ->
                    PositiveFactorItem(factor)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Рекомендации
            if (assessment.recommendations.isNotEmpty()) {
                Text(
                    text = "Рекомендации",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                assessment.recommendations.forEach { rec ->
                    RecommendationItem(rec)
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    val (text, color) = when (riskLevel) {
        RiskLevel.LOW -> Pair("Низкий риск", Color(0xFF4CAF50))
        RiskLevel.MEDIUM -> Pair("Средний риск", Color(0xFFFF9800))
        RiskLevel.HIGH -> Pair("Высокий риск", Color(0xFFF44336))
        RiskLevel.VERY_HIGH -> Pair("Очень высокий риск", Color(0xFFB71C1C))
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RiskFactorItem(factor: RiskFactor) {
    val severityColor = when (factor.severity) {
        RiskSeverity.LOW -> Color(0xFFFF9800)
        RiskSeverity.MEDIUM -> Color(0xFFFF5722)
        RiskSeverity.HIGH -> Color(0xFFD32F2F)
    }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = factor.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (factor.severity) {
                        RiskSeverity.LOW -> "Низкий"
                        RiskSeverity.MEDIUM -> "Средний"
                        RiskSeverity.HIGH -> "Высокий"
                    },
                    color = severityColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = factor.description,
                style = MaterialTheme.typography.bodyMedium
            )
            factor.suggestion?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 ${it}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PositiveFactorItem(factor: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "✅",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = factor,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun RecommendationItem(rec: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "📌",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = rec,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Добавляет оценку рисков в конец LazyColumn
 */
fun LazyListScope.riskAssessmentSection(
    assessment: GigaChatRiskAssessment,
    onDismiss: (() -> Unit)? = null
) {
    item {
        Spacer(modifier = Modifier.height(16.dp))
        RiskAssessmentCard(
            assessment = assessment,
            onDismiss = onDismiss
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Получает цвет для уровня риска
 */
fun getRiskColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MEDIUM -> Color(0xFFFF9800)
        RiskLevel.HIGH -> Color(0xFFF44336)
        RiskLevel.VERY_HIGH -> Color(0xFFB71C1C)
    }
}
