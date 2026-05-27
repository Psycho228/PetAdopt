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
                color = when (assessment.recommendation.lowercase()) {
                    "reject", "требуется дополнительная проверка", "review_required" -> Color(0xFFFFEBEE)
                    "approve_with_conditions", "одобрить с условиями" -> Color(0xFFFFF3E0)
                    "approve", "рекомендуется одобрить" -> Color(0xFFF1F8E9)
                    else -> Color(0xFFFFEBEE)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (assessment.recommendation.lowercase()) {
                            "reject", "требуется дополнительная проверка", "review_required" -> Color(0xFFD32F2F)
                            "approve_with_conditions", "одобрить с условиями" -> Color(0xFFFF9800)
                            "approve", "рекомендуется одобрить" -> Color(0xFF4CAF50)
                            else -> Color(0xFFD32F2F)
                        },
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = assessment.recommendationText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (assessment.recommendation.lowercase()) {
                        "reject", "требуется дополнительная проверка", "review_required" -> Color(0xFFB71C1C)
                        "approve_with_conditions", "одобрить с условиями" -> Color(0xFFFF6F00)
                        "approve", "рекомендуется одобрить" -> Color(0xFF2E7D32)
                        else -> Color(0xFFB71C1C)
                    },
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
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    assessment.recommendations.forEachIndexed { index, rec ->
                        // Пропускаем строки, которые являются кодами рекомендации (REJECT, APPROVE и т.д.)
                        val translatedRec = when (rec.trim().uppercase()) {
                            "REJECT" -> "Рекомендуется отклонить"
                            "APPROVE" -> "Рекомендуется одобрить"
                            "APPROVE_WITH_CONDITIONS" -> "Одобрить с условиями"
                            "REVIEW_REQUIRED" -> "Требуется дополнительная проверка"
                            else -> rec
                        }
                        
                        // Пропускаем коды рекомендации, так как они уже отображаются выше
                        if (rec.trim().uppercase() !in listOf("REJECT", "APPROVE", "APPROVE_WITH_CONDITIONS", "REVIEW_REQUIRED")) {
                            RecommendationItem(translatedRec)
                            if (index < assessment.recommendations.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                    
                    // Если все рекомендации были кодами, показываем сообщение
                    if (assessment.recommendations.all { it.trim().uppercase() in listOf("REJECT", "APPROVE", "APPROVE_WITH_CONDITIONS", "REVIEW_REQUIRED") }) {
                        Text(
                            text = "Нет дополнительных рекомендаций",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
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
        RiskSeverity.VERY_HIGH -> Color(0xFFB71C1C)
        RiskSeverity.CRITICAL -> Color(0xFF7F0000)
    }
    
    Surface(
        color = when (factor.severity) {
            RiskSeverity.CRITICAL -> Color(0xFFFFEBEE)
            RiskSeverity.VERY_HIGH -> Color(0xFFFFE0E0)
            RiskSeverity.HIGH -> Color(0xFFFFF3E0)
            RiskSeverity.MEDIUM -> Color(0xFFFFF8E1)
            RiskSeverity.LOW -> Color(0xFFF5F5F5)
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, severityColor, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = factor.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = severityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (factor.severity) {
                            RiskSeverity.LOW -> "Низкий"
                            RiskSeverity.MEDIUM -> "Средний"
                            RiskSeverity.HIGH -> "Высокий"
                            RiskSeverity.VERY_HIGH -> "Очень высокий"
                            RiskSeverity.CRITICAL -> "Критический"
                        },
                        color = severityColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = factor.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            factor.suggestion?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = it.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
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
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "📌",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = rec.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
