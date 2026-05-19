package com.example.petadopt.data.repository

import com.example.petadopt.data.model.QuestionnaireAnswer

interface QuestionnaireRepository {
    suspend fun saveAnswers(answer: QuestionnaireAnswer)
    suspend fun getAnswers(): QuestionnaireAnswer?
    suspend fun deleteAnswers()
}