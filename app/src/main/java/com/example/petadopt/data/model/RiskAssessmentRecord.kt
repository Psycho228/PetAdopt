package com.example.petadopt.data.model

import kotlinx.serialization.Serializable

/**
 * Оценка рисков, сохранённая в Supabase
 */
@Serializable
data class RiskAssessmentRecord(
    val id: String = "",
    val user_id: String = "",
    val questionnaire_answer_id: String = "",
    
    // Результаты оценки
    val overallRisk: String = "",           // LOW, MEDIUM, HIGH, VERY_HIGH
    val riskScore: Int = 0,                 // 0-100
    val recommendation: String = "",        // APPROVE, APPROVE_WITH_CONDITIONS, REVIEW_REQUIRED, REJECT
    
    // Данные для отображения
    val detailedAnalysis: String = "",
    val riskFactorsJson: String = "",       // JSON массив факторов риска
    val positiveFactorsJson: String = "",   // JSON массив положительных факторов
    val recommendationsJson: String = "",   // JSON массив рекомендаций
    
    // Метаданные
    val created_at: String = "",
    val gigachat_request_id: String = ""    // Для трассировки
)

// Расширения для удобной работы
val RiskAssessmentRecord.overallRiskLevel: RiskLevel
    get() = RiskLevel.valueOf(overallRisk)

val RiskAssessmentRecord.recommendationType: Recommendation
    get() = Recommendation.valueOf(recommendation)
