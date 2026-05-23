package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.GigaChatRiskAssessment
import com.example.petadopt.data.repository.GigaChatRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase для оценки рисков пристройства питомца с помощью GigaChat
 */
@Singleton
class AssessRiskUseCase @Inject constructor(
    private val repository: GigaChatRepository
) {
    /**
     * Оценивает риски на основе ответов опросника
     * @param answer Ответы пользователя
     * @return Результат с оценкой рисков и ID запроса
     */
    suspend operator fun invoke(answer: QuestionnaireAnswer): Result<Pair<GigaChatRiskAssessment, String>> {
        return repository.assessRisk(answer)
    }
}

/**
 * UseCase для получения всех бизнес-логик, связанных с оценкой рисков
 */
@Singleton
class RiskAssessmentUseCases @Inject constructor(
    val assessRisk: AssessRiskUseCase
)
