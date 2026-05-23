package com.example.petadopt.data.model

import kotlinx.serialization.Serializable

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
    val recommendation: Recommendation    // Итоговая рекомендация
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
    val severity: RiskSeverity,     // Серьёзность
    val description: String,        // Описание фактора
    val suggestion: String?         // Предложение по снижению риска
)

@Serializable
enum class RiskSeverity {
    LOW,        // Небольшой риск
    MEDIUM,     // Средний риск
    HIGH        // Высокий риск
}

@Serializable
enum class Recommendation {
    APPROVE,            // Можно одобрить
    APPROVE_WITH_CONDITIONS,  // Одобрить с условиями
    REVIEW_REQUIRED,    // Требуется дополнительная проверка
    REJECT              // Отклонить
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
    get() = when (recommendation) {
        Recommendation.APPROVE -> "Рекомендуется одобрить"
        Recommendation.APPROVE_WITH_CONDITIONS -> "Одобрить с условиями"
        Recommendation.REVIEW_REQUIRED -> "Требуется дополнительная проверка"
        Recommendation.REJECT -> "Рекомендуется отклонить"
    }

val GigaChatRiskAssessment.riskColor: String
    get() = when (overallRisk) {
        RiskLevel.LOW -> "#4CAF50"      // Зелёный
        RiskLevel.MEDIUM -> "#FF9800"   // Оранжевый
        RiskLevel.HIGH -> "#F44336"     // Красный
        RiskLevel.VERY_HIGH -> "#B71C1C" // Тёмно-красный
    }
