package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.RiskAssessmentRecord
import com.example.petadopt.util.SupabaseConfig
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseQuestionnaireRepository @Inject constructor(
) : QuestionnaireRepository {
    private val postgrest: Postgrest = SupabaseConfig.postgrest
    private val auth: Auth = SupabaseConfig.auth
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val TABLE_QUESTIONNAIRE = "questionnaire_answers"
        private const val TABLE_RISK_ASSESSMENT = "risk_assessments"
        private const val TAG = "SupabaseQuestionnaireRepository"
    }

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun saveAnswers(answer: QuestionnaireAnswer) {
        val uid = currentUserId ?: throw Exception("Пользователь не авторизован")
        
        Log.d(TAG, "=== saveAnswers начато ===")
        Log.d(TAG, "user_id: $uid")
        Log.d(TAG, "q1_full_name: ${answer.q1_full_name}")
        Log.d(TAG, "q1_age: ${answer.q1_age}")
        Log.d(TAG, "q1_city: ${answer.q1_city}")
        Log.d(TAG, "q4_understand_requirements: ${answer.q4_understand_requirements}")
        Log.d(TAG, "q4_understand_time: ${answer.q4_understand_time}")
        Log.d(TAG, "q4_understand_attention: ${answer.q4_understand_attention}")
        Log.d(TAG, "q4_understand_training: ${answer.q4_understand_training}")
        Log.d(TAG, "q4_understand_vet_care: ${answer.q4_understand_vet_care}")
        Log.d(TAG, "q4_ready_expenses: ${answer.q4_ready_expenses}")
        Log.d(TAG, "q4_ready_food: ${answer.q4_ready_food}")
        Log.d(TAG, "q4_ready_vet: ${answer.q4_ready_vet}")
        Log.d(TAG, "q4_ready_medication: ${answer.q4_ready_medication}")
        Log.d(TAG, "q4_ready_vaccinations: ${answer.q4_ready_vaccinations}")
        Log.d(TAG, "q4_ready_grooming: ${answer.q4_ready_grooming}")
        Log.d(TAG, "q5_safety_measures: ${answer.q5_safety_measures}")
        Log.d(TAG, "q5_ready_neuter: ${answer.q5_ready_neuter}")
        Log.d(TAG, "q5_ready_recommendations: ${answer.q5_ready_recommendations}")
        Log.d(TAG, "q5_ready_tracker: ${answer.q5_ready_tracker}")
        Log.d(TAG, "q6_responsible_owner_meaning: ${answer.q6_responsible_owner_meaning}")
        Log.d(TAG, "q7_desired_pets: ${answer.q7_desired_pets}")
        
        try {
            val answerData = buildJsonObject {
                put("user_id", uid)
                // Раздел 1
                put("q1_full_name", answer.q1_full_name)
                put("q1_age", answer.q1_age ?: 0)
                put("q1_city", answer.q1_city)
                put("q1_occupation", answer.q1_occupation)
                put("q1_phone", answer.q1_phone)
                put("q1_email", answer.q1_email)
                // Раздел 2
                put("q2_housing_type", answer.q2_housing_type)
                put("q2_pets_allowed", answer.q2_pets_allowed ?: false)
                put("q2_living_with", JsonArray(answer.q2_living_with.map { JsonPrimitive(it) }))
                put("q2_family_consent", answer.q2_family_consent ?: false)
                put("q2_has_children", answer.q2_has_children ?: false)
                put("q2_children_ages", answer.q2_children_ages)
                put("q2_has_other_pets", answer.q2_has_other_pets ?: false)
                put("q2_other_pets_types", JsonArray(answer.q2_other_pets_types.map { JsonPrimitive(it) }))
                put("q2_hours_alone", answer.q2_hours_alone ?: 0)
                put("q2_caregiver", answer.q2_caregiver)
                // Раздел 3
                put("q3_had_pets_before", answer.q3_had_pets_before ?: false)
                put("q3_what_happened", answer.q3_what_happened)
                put("q3_dog_experience", answer.q3_dog_experience ?: false)
                put("q3_cat_experience", answer.q3_cat_experience ?: false)
                put("q3_special_needs_experience", answer.q3_special_needs_experience ?: false)
                put("q3_why_now", answer.q3_why_now)
                // Раздел 4
                put("q4_understand_requirements", answer.q4_understand_requirements)
                put("q4_understand_time", answer.q4_understand_time ?: false)
                put("q4_understand_attention", answer.q4_understand_attention)
                put("q4_understand_training", answer.q4_understand_training)
                put("q4_understand_vet_care", answer.q4_understand_vet_care)
                put("q4_ready_expenses", answer.q4_ready_expenses ?: false)
                put("q4_ready_food", answer.q4_ready_food)
                put("q4_ready_vet", answer.q4_ready_vet)
                put("q4_ready_medication", answer.q4_ready_medication)
                put("q4_ready_vaccinations", answer.q4_ready_vaccinations)
                put("q4_ready_grooming", answer.q4_ready_grooming)
                put("q4_furniture_damage_plan", answer.q4_furniture_damage_plan)
                put("q4_noise_plan", answer.q4_noise_plan)
                put("q4_shy_pet_plan", answer.q4_shy_pet_plan)
                put("q4_long_adaptation_plan", answer.q4_long_adaptation_plan)
                put("q4_ready_education", answer.q4_ready_education)
                put("q4_life_changes_plan", answer.q4_life_changes_plan)
                put("q4_obstacles_next_year", answer.q4_obstacles_next_year)
                // Раздел 5 - используем старый массив q5_safety_measures
                val safetyMeasuresList = answer.q5_safety_measures
                put("q5_safety_measures", JsonArray(safetyMeasuresList.map { JsonPrimitive(it) }))
                put("q5_ready_neuter", answer.q5_ready_neuter)
                put("q5_ready_recommendations", answer.q5_ready_recommendations)
                put("q5_ready_tracker", answer.q5_ready_tracker)
                put("q5_ready_keep_contact", answer.q5_ready_keep_contact)
                // Раздел 6
                put("q6_responsible_owner_meaning", answer.q6_responsible_owner_meaning)
                put("q6_life_with_pet_vision", answer.q6_life_with_pet_vision)
                put("q6_why_good_owner", answer.q6_why_good_owner)
                // Раздел 7
                put("q7_desired_pets", JsonArray(answer.q7_desired_pets.map { JsonPrimitive(it) }))
            }
            
            // УДАЛЯЕМ ВСЕ старые оценки рисков ПЕРЕД сохранением опросника
            val deleteResult = postgrest.from(TABLE_RISK_ASSESSMENT)
                .delete { filter { eq("user_id", uid) } }
            Log.d(TAG, "Deleted risk assessments for user $uid: $deleteResult")
            
            // Проверяем, есть ли уже запись опросника
            val existing = postgrest.from(TABLE_QUESTIONNAIRE)
                .select { filter { eq("user_id", uid) } }
                .decodeSingleOrNull<QuestionnaireAnswer>()
            
            if (existing != null) {
                // Обновляем существующую запись
                val updateData = buildJsonObject {
                    // Раздел 1
                    put("q1_full_name", answer.q1_full_name)
                    put("q1_age", answer.q1_age ?: 0)
                    put("q1_city", answer.q1_city)
                    put("q1_occupation", answer.q1_occupation)
                    put("q1_phone", answer.q1_phone)
                    put("q1_email", answer.q1_email)
                    // Раздел 2
                    put("q2_housing_type", answer.q2_housing_type)
                    put("q2_pets_allowed", answer.q2_pets_allowed ?: false)
                    put("q2_living_with", JsonArray(answer.q2_living_with.map { JsonPrimitive(it) }))
                    put("q2_family_consent", answer.q2_family_consent ?: false)
                    put("q2_has_children", answer.q2_has_children ?: false)
                    put("q2_children_ages", answer.q2_children_ages)
                    put("q2_has_other_pets", answer.q2_has_other_pets ?: false)
                    put("q2_other_pets_types", JsonArray(answer.q2_other_pets_types.map { JsonPrimitive(it) }))
                    put("q2_hours_alone", answer.q2_hours_alone ?: 0)
                    put("q2_caregiver", answer.q2_caregiver)
                    // Раздел 3
                    put("q3_had_pets_before", answer.q3_had_pets_before ?: false)
                    put("q3_what_happened", answer.q3_what_happened)
                    put("q3_dog_experience", answer.q3_dog_experience ?: false)
                    put("q3_cat_experience", answer.q3_cat_experience ?: false)
                    put("q3_special_needs_experience", answer.q3_special_needs_experience ?: false)
                    put("q3_why_now", answer.q3_why_now)
                    // Раздел 4
                    put("q4_understand_requirements", answer.q4_understand_requirements)
                    put("q4_understand_time", answer.q4_understand_time ?: false)
                    put("q4_understand_attention", answer.q4_understand_attention)
                    put("q4_understand_training", answer.q4_understand_training)
                    put("q4_understand_vet_care", answer.q4_understand_vet_care)
                    put("q4_ready_expenses", answer.q4_ready_expenses ?: false)
                    put("q4_ready_food", answer.q4_ready_food)
                    put("q4_ready_vet", answer.q4_ready_vet)
                    put("q4_ready_medication", answer.q4_ready_medication)
                    put("q4_ready_vaccinations", answer.q4_ready_vaccinations)
                    put("q4_ready_grooming", answer.q4_ready_grooming)
                    put("q4_furniture_damage_plan", answer.q4_furniture_damage_plan)
                    put("q4_noise_plan", answer.q4_noise_plan)
                    put("q4_shy_pet_plan", answer.q4_shy_pet_plan)
                    put("q4_long_adaptation_plan", answer.q4_long_adaptation_plan)
                    put("q4_ready_education", answer.q4_ready_education)
                    put("q4_life_changes_plan", answer.q4_life_changes_plan)
                    put("q4_obstacles_next_year", answer.q4_obstacles_next_year)
                    // Раздел 5
                    val safetyMeasuresList = answer.q5_safety_measures
                    put("q5_safety_measures", JsonArray(safetyMeasuresList.map { JsonPrimitive(it) }))
                    put("q5_ready_neuter", answer.q5_ready_neuter)
                    put("q5_ready_recommendations", answer.q5_ready_recommendations)
                    put("q5_ready_tracker", answer.q5_ready_tracker)
                    put("q5_ready_keep_contact", answer.q5_ready_keep_contact)
                    // Раздел 6
                    put("q6_responsible_owner_meaning", answer.q6_responsible_owner_meaning)
                    put("q6_life_with_pet_vision", answer.q6_life_with_pet_vision)
                    put("q6_why_good_owner", answer.q6_why_good_owner)
                    // Раздел 7
                    put("q7_desired_pets", JsonArray(answer.q7_desired_pets.map { JsonPrimitive(it) }))
                }
                
                postgrest.from(TABLE_QUESTIONNAIRE)
                    .update(updateData) { filter { eq("user_id", uid) } }
                Log.d(TAG, "Questionnaire updated for user: $uid")
            } else {
                // Вставляем новую запись
                postgrest.from(TABLE_QUESTIONNAIRE).insert(answerData)
                Log.d(TAG, "Questionnaire inserted for user: $uid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving questionnaire: ${e.message}")
            throw Exception("Ошибка сохранения опросника: ${e.message}")
        }
    }

    override suspend fun getAnswers(): QuestionnaireAnswer? {
        val uid = currentUserId
        Log.d(TAG, "getAnswers: currentUserId = $uid")
        if (uid == null) {
            Log.d(TAG, "getAnswers: user not authenticated")
            return null
        }
        
        return try {
            Log.d(TAG, "getAnswers: querying for user_id = $uid")
            
            // Получаем записи и фильтруем на клиенте
            val response = postgrest.from(TABLE_QUESTIONNAIRE)
                .select()
            
            Log.d(TAG, "Raw response: ${response.data}")
            
            // Декодируем как список и фильтруем по user_id
            val results = response.decodeList<QuestionnaireAnswer>()
            Log.d(TAG, "Decoded ${results.size} records")
            
            val result = results.firstOrNull { it.user_id == uid }
            Log.d(TAG, "Filtered result: ${result?.user_id}, expected: $uid, match: ${result?.user_id == uid}")
            
            Log.d(TAG, "Questionnaire loaded for user: $uid, result = ${result != null}")
            if (result != null) {
                Log.d(TAG, "Questionnaire data: q1_full_name = ${result.q1_full_name}, user_id = ${result.user_id}")
                Log.d(TAG, "Questionnaire data: q1_phone = '${result.q1_phone}', q1_email = '${result.q1_email}'")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting questionnaire: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteAnswers() {
        val uid = currentUserId ?: throw Exception("Пользователь не авторизован")
        
        try {
            postgrest.from(TABLE_QUESTIONNAIRE)
                .delete {
                    filter { eq("user_id", uid) }
                }
            
            Log.d(TAG, "Questionnaire deleted for user: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting questionnaire: ${e.message}")
            throw Exception("Ошибка удаления опросника: ${e.message}")
        }
    }

    override suspend fun saveRiskAssessment(record: RiskAssessmentRecord) {
        val uid = currentUserId ?: throw Exception("Пользователь не авторизован")
        
        Log.d(TAG, "=== Сохранение оценки рисков для userId: $uid ===")
        Log.d(TAG, "riskScore: ${record.riskScore}, overallRisk: ${record.overallRisk}")
        Log.d(TAG, "gigachat_request_id: ${record.gigachat_request_id}")
        
        try {
            val assessmentData = buildJsonObject {
                put("user_id", uid)
                put("questionnaire_answer_id", record.questionnaire_answer_id)
                put("overallRisk", record.overallRisk)
                put("riskScore", record.riskScore)
                put("recommendation", record.recommendation)
                put("detailedAnalysis", record.detailedAnalysis)
                put("riskFactorsJson", record.riskFactorsJson)
                put("positiveFactorsJson", record.positiveFactorsJson)
                put("recommendationsJson", record.recommendationsJson)
                put("gigachat_request_id", record.gigachat_request_id)
            }
            
            Log.d(TAG, "Данные для отправки: $assessmentData")
            Log.d(TAG, "Выполнение INSERT в таблицу $TABLE_RISK_ASSESSMENT...")
            
            val response = postgrest.from(TABLE_RISK_ASSESSMENT).insert(assessmentData)
            Log.d(TAG, "Ответ от Supabase: $response")
            
            Log.d(TAG, "✅ Оценка рисков успешно сохранена в БД!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ОШИБКА при сохранении оценки рисков: ${e.message}", e)
            e.printStackTrace()
            throw Exception("Ошибка сохранения оценки рисков: ${e.message}. Проверьте, что таблица создана и RLS политики настроены.")
        }
    }

    override suspend fun getRiskAssessment(): RiskAssessmentRecord? {
        val uid = currentUserId
        if (uid == null) return null
        
        return try {
            // Получаем ВСЕ записи и сортируем на клиенте (получить последнюю по created_at)
            val response = postgrest.from(TABLE_RISK_ASSESSMENT)
                .select { filter { eq("user_id", uid) } }
                .decodeList<RiskAssessmentRecord>()
            
            val latest = response.maxByOrNull { it.created_at }
            
            Log.d(TAG, "Latest risk assessment loaded for user: $uid, found: ${latest != null}, created_at: ${latest?.created_at}")
            latest
        } catch (e: Exception) {
            Log.e(TAG, "Error getting risk assessment: ${e.message}")
            null
        }
    }

    override suspend fun getRiskAssessmentHistory(): List<RiskAssessmentRecord> {
        val uid = currentUserId
        if (uid == null) return emptyList()
        
        return try {
            val response = postgrest.from(TABLE_RISK_ASSESSMENT)
                .select { 
                    filter { eq("user_id", uid) }
                }
                .decodeList<RiskAssessmentRecord>()
                .sortedByDescending { it.created_at }
            
            Log.d(TAG, "Risk assessment history loaded: ${response.size} records")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error getting risk assessment history: ${e.message}")
            emptyList()
        }
    }
}