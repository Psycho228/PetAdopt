package com.example.petadopt.data.repository

import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.RiskAssessmentRecord

interface QuestionnaireRepository {
    suspend fun saveAnswers(answer: QuestionnaireAnswer)
    suspend fun getAnswers(): QuestionnaireAnswer?
    suspend fun deleteAnswers()
    suspend fun getQuestionnaireByUserId(userId: String): QuestionnaireAnswer?
    
    // Методы для оценки рисков
    suspend fun saveRiskAssessment(record: RiskAssessmentRecord)
    suspend fun getRiskAssessment(): RiskAssessmentRecord?
    suspend fun getRiskAssessmentHistory(): List<RiskAssessmentRecord>
    suspend fun getLatestRiskAssessmentByUserId(userId: String): RiskAssessmentRecord?
}