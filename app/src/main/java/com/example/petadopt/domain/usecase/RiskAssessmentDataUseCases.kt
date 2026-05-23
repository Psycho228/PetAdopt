package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.RiskAssessmentRecord
import com.example.petadopt.data.repository.QuestionnaireRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase для получения последней оценки рисков пользователя
 */
@Singleton
class GetRiskAssessmentUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke(): RiskAssessmentRecord? {
        return repository.getRiskAssessment()
    }
}

/**
 * UseCase для получения истории оценок рисков пользователя
 */
@Singleton
class GetRiskAssessmentHistoryUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke(): List<RiskAssessmentRecord> {
        return repository.getRiskAssessmentHistory()
    }
}
