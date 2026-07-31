package com.example.petadopt.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.nio.charset.Charset

/**
 * Ответ от GigaChat с оценкой рисков пристройства питомца
 */
@Serializable
data class GigaChatRiskAssessment(
    val overallRisk: RiskLevel,           // Общий уровень риска
    val riskScore: Int,                   // Баллы от 0 до 100
    val riskFactors: List<RiskFactor>,    // Выявленные факторы риска
    val positiveFactors: List<String>,    // Положительные факторы
    val recommendations: List<String>,    // Рекомендации для пользователя
    val detailedAnalysis: String,         // Развёрнутый анализ
    @JsonNames("recommendation", "recommendation_text", "final_recommendation")
    val recommendation: String            // Итоговая рекомендация (строка от GigaChat)
)

@Serializable
enum class RiskLevel {
    LOW,           // Низкий риск
    MEDIUM,        // Средний риск
    HIGH,          // Высокий риск
    VERY_HIGH      // Очень высокий риск
}

@Serializable
data class RiskFactor(
    val category: String,           // Категория риска (жилищные условия, опыт, ответственность...)
    @JsonNames("severity", "sev", "MED", "MEDIUM", "HIGH", "VERY_HIGH", "CRITICAL", "CRIT")
    val severity: RiskSeverity,     // Серьёзность
    val description: String,        // Описание фактора
    val suggestion: String?         // Предложение по снижению риска
)

@Serializable
enum class RiskSeverity {
    LOW,        // Небольшой риск
    MEDIUM,     // Средний риск
    HIGH,       // Высокий риск
    VERY_HIGH,  // Очень высокий риск
    CRITICAL;   // Критический риск
    
    companion object {
        // Кастомный парсер для поддержки сокращений от GigaChat
        fun fromString(value: String): RiskSeverity {
            return when (value.uppercase()) {
                "LOW", "ЛOW" -> LOW
                "MED", "MEDIUM" -> MEDIUM
                "HIGH", "HI" -> HIGH
                "VERY_HIGH", "VERYHIGH", "VERY" -> VERY_HIGH
                "CRITICAL", "CRIT" -> CRITICAL
                else -> MEDIUM // По умолчанию
            }
        }
    }
}

// Расширения для удобного отображения в UI
val GigaChatRiskAssessment.riskLevelText: String
    get() = when (overallRisk) {
        RiskLevel.LOW -> "Низкий риск"
        RiskLevel.MEDIUM -> "Средний риск"
        RiskLevel.HIGH -> "Высокий риск"
        RiskLevel.VERY_HIGH -> "Очень высокий риск"
    }

val GigaChatRiskAssessment.recommendationText: String
    get() = normalizeRecommendationText(recommendation)

fun normalizeRecommendationText(value: String): String {
    val recommendation = decodeLegacyMojibake(value).trim()
    return when (recommendation.lowercase()) {
        "approve", "можно одобрить", "рекомендуется одобрить", "рекомендуется одобрить заявку" -> "Рекомендуется одобрить"
        "approve_with_conditions", "одобрить с условиями" -> "Одобрить с условиями"
        "review_required", "требуется дополнительная проверка" -> "Требуется дополнительная проверка"
        "reject", "отклонить", "рекомендуется отклонить", "рекомендуется отклонить заявку" -> "Рекомендуется отклонить"
        else -> recommendation
    }
}

private fun decodeLegacyMojibake(value: String): String {
    val decoded = runCatching {
        String(value.toByteArray(Charset.forName("windows-1251")), Charsets.UTF_8)
    }.getOrNull()

    return decoded?.takeIf { '\uFFFD' !in it } ?: value
}

val GigaChatRiskAssessment.riskColor: String
    get() = when (overallRisk) {
        RiskLevel.LOW -> "#4CAF50"      // Зелёный
        RiskLevel.MEDIUM -> "#FF9800"   // Оранжевый
        RiskLevel.HIGH -> "#F44336"     // Красный
        RiskLevel.VERY_HIGH -> "#B71C1C" // Тёмно-красный
    }
