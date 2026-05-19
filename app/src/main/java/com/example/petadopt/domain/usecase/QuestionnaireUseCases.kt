package com.example.petadopt.domain.usecase

import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.repository.QuestionnaireRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke(answer: QuestionnaireAnswer) {
        repository.saveAnswers(answer)
    }
}

@Singleton
class GetQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke(): QuestionnaireAnswer? {
        return repository.getAnswers()
    }
}

@Singleton
class DeleteQuestionnaireUseCase @Inject constructor(
    private val repository: QuestionnaireRepository
) {
    suspend operator fun invoke() {
        repository.deleteAnswers()
    }
}
