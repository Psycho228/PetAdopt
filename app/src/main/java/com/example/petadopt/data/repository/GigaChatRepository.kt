package com.example.petadopt.data.repository

import android.util.Log
import com.example.petadopt.BuildConfig
import com.example.petadopt.data.model.QuestionnaireAnswer
import com.example.petadopt.data.model.understandsNeeds
import com.example.petadopt.data.model.readyForExpenses
import com.example.petadopt.data.model.safetyMeasures
import com.example.petadopt.data.model.willingTo
import com.example.petadopt.data.model.GigaChatRiskAssessment
import com.example.petadopt.data.model.RiskLevel
import com.example.petadopt.domain.usecase.AssessRiskUseCase
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// DTO РґР»СЏ РѕС‚РІРµС‚Р° OAuth
@Serializable
data class OAuthTokenResponse(
    val access_token: String,
    val expires_at: Long? = null
)

// DTO РґР»СЏ Р·Р°РїСЂРѕСЃР° Рє GigaChat
@Serializable
data class GigaChatRequest(
    val model: String = "GigaChat-2",
    val messages: List<Message>,
    val temperature: Double = 0.3,
    val max_tokens: Int = 4096
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

/**
 * Р РµРїРѕР·РёС‚РѕСЂРёР№ РґР»СЏ РІР·Р°РёРјРѕРґРµР№СЃС‚РІРёСЏ СЃ GigaChat API
 * РћС†РµРЅРёРІР°РµС‚ СЂРёСЃРєРё РїРµСЂРµРґР°С‡Рё РїРёС‚РѕРјС†Р° РЅР° РѕСЃРЅРѕРІРµ РѕС‚РІРµС‚РѕРІ РѕРїСЂРѕСЃРЅРёРєР°
 */
@Singleton
class GigaChatRepository @Inject constructor() {
    
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
        }
    }
    
    // GigaChat API РєРѕРЅС„РёРіСѓСЂР°С†РёСЏ
    private val baseUrl = "https://api.giga.chat/v1"
    private val authUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
    
    companion object {
        private const val TAG = "GigaChatRepository"
    }
    
    /**
     * РџРѕР»СѓС‡Р°РµС‚ РѕС†РµРЅРєСѓ СЂРёСЃРєРѕРІ РѕС‚ GigaChat
     * @param answer РћС‚РІРµС‚С‹ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ РЅР° РѕРїСЂРѕСЃРЅРёРє
     * @return РћС†РµРЅРєР° СЂРёСЃРєРѕРІ СЃ ID Р·Р°РїСЂРѕСЃР°
     */
    suspend fun assessRisk(answer: QuestionnaireAnswer): Result<Pair<GigaChatRiskAssessment, String>> {
        return try {
            Log.d(TAG, "=== РќР°С‡Р°Р»Р° РѕС†РµРЅРєР° СЂРёСЃРєРѕРІ РґР»СЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ ===")
            
            // 1. РџРѕР»СѓС‡Р°РµРј JWT С‚РѕРєРµРЅ
            Log.d(TAG, "1. РџРѕР»СѓС‡РµРЅРёРµ JWT С‚РѕРєРµРЅР°...")
            val jwtToken = getJwtToken()
            Log.d(TAG, "JWT С‚РѕРєРµРЅ РїРѕР»СѓС‡РµРЅ: ${jwtToken.take(20)}...")
            
            // 2. Р¤РѕСЂРјРёСЂСѓРµРј РїСЂРѕРјРїС‚ РґР»СЏ Р°РЅР°Р»РёР·Р°
            Log.d(TAG, "2. Р¤РѕСЂРјРёСЂРѕРІР°РЅРёРµ РїСЂРѕРјРїС‚Р°...")
            val prompt = buildRiskAssessmentPrompt(answer)
            Log.d(TAG, "РџСЂРѕРјРїС‚ СЃС„РѕСЂРјРёСЂРѕРІР°РЅ, РґР»РёРЅР°: ${prompt.length} СЃРёРјРІРѕР»РѕРІ")
            
            // 3. РћС‚РїСЂР°РІР»СЏРµРј Р·Р°РїСЂРѕСЃ Рє GigaChat
            Log.d(TAG, "3. РћС‚РїСЂР°РІРєР° Р·Р°РїСЂРѕСЃР° Рє GigaChat API...")
            val chatResponse = sendChatRequest(jwtToken, prompt)
            Log.d(TAG, "РћС‚РІРµС‚ РѕС‚ GigaChat РїРѕР»СѓС‡РµРЅ, РґР»РёРЅР°: ${chatResponse.length} СЃРёРјРІРѕР»РѕРІ")
            
            // 4. РџР°СЂСЃРёРј РѕС‚РІРµС‚ РІ СЃС‚СЂСѓРєС‚СѓСЂСѓ РѕС†РµРЅРєРё СЂРёСЃРєРѕРІ
            Log.d(TAG, "4. РџР°СЂСЃРёРЅРі РѕС‚РІРµС‚Р°...")
            val assessment = parseRiskAssessment(chatResponse)
            Log.d(TAG, "РћС‚РІРµС‚ СЂР°СЃРїР°СЂС€РµРЅ: riskScore=${assessment.riskScore}, recommendation=${assessment.recommendation}")
            
            // Р“РµРЅРµСЂРёСЂСѓРµРј ID Р·Р°РїСЂРѕСЃР° РґР»СЏ С‚СЂР°СЃСЃРёСЂРѕРІРєРё
            val requestId = java.util.UUID.randomUUID().toString()
            Log.d(TAG, "=== РћС†РµРЅРєР° СЂРёСЃРєРѕРІ СѓСЃРїРµС€РЅР°. requestId=$requestId ===")
            
            Result.success(Pair(assessment, requestId))
        } catch (e: Exception) {
            Log.e(TAG, "=== РћРЁРР‘РљРђ РїСЂРё РѕС†РµРЅРєРµ СЂРёСЃРєРѕРІ: ${e.message} ===", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * РџРѕР»СѓС‡Р°РµС‚ JWT С‚РѕРєРµРЅ РґР»СЏ РґРѕСЃС‚СѓРїР° Рє GigaChat API
     */
    private suspend fun getJwtToken(): String {
        return try {
            Log.d(TAG, "GigaChat_AUTH_KEY РґР»РёРЅР°: ${BuildConfig.GIGACHAT_AUTH_KEY.length}")
            val authKey = BuildConfig.GIGACHAT_AUTH_KEY
            
            // РџСЂРѕРІРµСЂСЏРµРј, РЅРµ РїСѓСЃС‚РѕР№ Р»Рё РєР»СЋС‡
            if (authKey.isEmpty()) {
                throw IllegalStateException("GIGACHAT_AUTH_KEY не задан. Проверьте файл .env")
            }
            
            val response = client.post(authUrl) {
                header("RqUID", java.util.UUID.randomUUID().toString())
                header("Authorization", "Basic $authKey")
                header("Content-Type", "application/x-www-form-urlencoded")
                header("Accept", "application/json")
                header("X-Client-App", "Hvostiki")
                setBody("scope=${BuildConfig.GIGACHAT_SCOPE}")
            }
            
            Log.d(TAG, "РћС‚РІРµС‚ OAuth: ${response.status}")
            val responseBody = response.body<String>()
            Log.d(TAG, "РўРµР»Рѕ РѕС‚РІРµС‚Р° OAuth: $responseBody")
            
            // РџР°СЂСЃРёРј JSON СЃ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёРµРј DTO
            val json = Json { ignoreUnknownKeys = true }
            val token = try {
                val oauthResponse = json.decodeFromString<OAuthTokenResponse>(responseBody)
                oauthResponse.access_token
            } catch (e: Exception) {
                Log.e(TAG, "РћС€РёР±РєР° РїР°СЂСЃРёРЅРіР° JSON: ${e.message}")
                null
            }
            
            token ?: throw IllegalStateException("Не удалось получить токен GigaChat. Ответ: $responseBody")
            
            Log.d(TAG, "JWT С‚РѕРєРµРЅ СѓСЃРїРµС€РЅРѕ РїРѕР»СѓС‡РµРЅ")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка аутентификации GigaChat: ${e.message}", e)
            throw IllegalStateException("Не удалось подключиться к GigaChat: ${e.message}", e)
        }
    }
    
    /**
     * Р¤РѕСЂРјРёСЂСѓРµС‚ СЃС‚СЂСѓРєС‚СѓСЂРёСЂРѕРІР°РЅРЅС‹Р№ РїСЂРѕРјРїС‚ РґР»СЏ Р°РЅР°Р»РёР·Р° СЂРёСЃРєРѕРІ
     */
    private fun buildRiskAssessmentPrompt(answer: QuestionnaireAnswer): String {
        return """
        РўС‹ вЂ” СЌРєСЃРїРµСЂС‚ РїРѕ РїСЂРёСЃС‚СЂРѕР№СЃС‚РІСѓ Р¶РёРІРѕС‚РЅС‹С… РёР· РїСЂРёСЋС‚РѕРІ. РџСЂРѕР°РЅР°Р»РёР·РёСЂСѓР№ РѕС‚РІРµС‚С‹ РєР°РЅРґРёРґР°С‚Р° 
        Рё РѕС†РµРЅРё СЂРёСЃРєРё РїРµСЂРµРґР°С‡Рё РїРёС‚РѕРјС†Р° СЌС‚РѕРјСѓ С‡РµР»РѕРІРµРєСѓ.

        **Р”РђРќРќР«Р• РљРђРќР”РР”РђРўРђ:**
        
        1. РћСЃРЅРѕРІРЅР°СЏ РёРЅС„РѕСЂРјР°С†РёСЏ (РџР РРћР РРўР•Рў: СЃСЂРµРґРЅРёР№):
           - РРјСЏ: ${answer.q1_full_name}
           - Р’РѕР·СЂР°СЃС‚: ${answer.q1_age} Р»РµС‚ ${if ((answer.q1_age ?: 0) < 18 || (answer.q1_age ?: 99) > 90) "[Р РРЎРљ: РІРѕР·СЂР°СЃС‚ РІРЅРµ РѕРїС‚РёРјР°Р»СЊРЅРѕРіРѕ РґРёР°РїР°Р·РѕРЅР° 18-90]" else ""}
           - Р“РѕСЂРѕРґ: ${answer.q1_city}
           - РџСЂРѕС„РµСЃСЃРёСЏ: ${answer.q1_occupation} ${if (answer.q1_occupation?.contains("РЎР±РµСЂР±Р°РЅРє", ignoreCase = true) == true) "[РџР РРћР РРўР•Рў: РїСЂРѕРІРµСЂРёС‚СЊ РіСЂР°С„РёРє СЂР°Р±РѕС‚С‹]" else ""}
           - РўРµР»РµС„РѕРЅ: ${answer.q1_phone}
           - Email: ${answer.q1_email}

        2. Р–РёР»РёС‰РЅС‹Рµ СѓСЃР»РѕРІРёСЏ (РџР РРћР РРўР•Рў: Р’Р«РЎРћРљРР™ вЂ” РєСЂРёС‚РёС‡РµСЃРєРёРµ С„Р°РєС‚РѕСЂС‹):
           - РўРёРї Р¶РёР»СЊСЏ: ${answer.q2_housing_type} ${if (answer.q2_housing_type == "РЎСЉС‘РјРЅРѕРµ Р¶РёР»СЊС‘") "[Р РРЎРљ: РЅРµСЃС‚Р°Р±РёР»СЊРЅРѕСЃС‚СЊ]" else ""}
           - Р–РёРІРѕС‚РЅС‹Рµ СЂР°Р·СЂРµС€РµРЅС‹: ${if (answer.q2_pets_allowed == true) "Р”Р°" else if (answer.q2_pets_allowed == false) "РќРµС‚ [РљР РРўРР§Р•РЎРљРР™ Р РРЎРљ: РЅРµС‚ СЂР°Р·СЂРµС€РµРЅРёСЏ]" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - Р–РёРІС‘С‚ СЃ: ${answer.q2_living_with.joinToString()} ${if (answer.q2_living_with.isEmpty()) "[Р РРЎРљ: Р¶РёРІС‘С‚ РѕРґРёРЅ вЂ” РЅРµС‚ РїРѕРґРґРµСЂР¶РєРё]" else ""}
           - РЎРѕРіР»Р°СЃРёРµ СЃРµРјСЊРё: ${if (answer.q2_family_consent == true) "Р”Р°" else if (answer.q2_family_consent == false) "РќРµС‚ [РљР РРўРР§Р•РЎРљРР™ Р РРЎРљ: РЅРµС‚ СЃРѕРіР»Р°СЃРёСЏ СЃРµРјСЊРё]" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - Р”РµС‚Рё: ${if (answer.q2_has_children == true) "Р”Р°, РІРѕР·СЂР°СЃС‚: ${answer.q2_children_ages}" else if (answer.q2_has_children == false) "РќРµС‚" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - Р”СЂСѓРіРёРµ Р¶РёРІРѕС‚РЅС‹Рµ: ${if (answer.q2_has_other_pets == true) "Р”Р°, С‚РёРїС‹: ${answer.q2_other_pets_types.joinToString()}" else if (answer.q2_has_other_pets == false) "РќРµС‚" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - Р§Р°СЃРѕРІ РІ РґРµРЅСЊ РїРёС‚РѕРјРµС† РѕРґРёРЅ: ${answer.q2_hours_alone}${getHoursAloneRiskHint(answer.q2_hours_alone)}
           - РљС‚Рѕ СѓС…Р°Р¶РёРІР°РµС‚ РІ РѕС‚СЃСѓС‚СЃС‚РІРёРµ: ${answer.q2_caregiver} ${if (answer.q2_caregiver.isNullOrBlank() || answer.q2_caregiver.length < 3) "[Р РРЎРљ: РЅРµ СѓРєР°Р·Р°РЅ СѓС…РѕРґ]" else ""}

        3. РћРїС‹С‚ СЃ Р¶РёРІРѕС‚РЅС‹РјРё (РџР РРћР РРўР•Рў: Р’Р«РЎРћРљРР™):
           - Р‘С‹Р»Рё Р»Рё РїРёС‚РѕРјС†С‹ СЂР°РЅСЊС€Рµ: ${if (answer.q3_had_pets_before == true) "Р”Р°" else if (answer.q3_had_pets_before == false) "РќРµС‚ [Р РРЎРљ: РЅРµС‚ РѕРїС‹С‚Р°]" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - Р§С‚Рѕ СЃ РЅРёРјРё СЃРµР№С‡Р°СЃ: ${answer.q3_what_happened} ${if (answer.q3_what_happened.isNotEmpty() && (answer.q3_what_happened.contains("РІС‹Р±СЂРѕСЃРёР»", ignoreCase = true) || answer.q3_what_happened.contains("РѕС‚РґР°Р»", ignoreCase = true))) "[РљР РРўРР§Р•РЎРљРР™ Р РРЎРљ: РЅРµРіР°С‚РёРІРЅР°СЏ РёСЃС‚РѕСЂРёСЏ]" else ""}
           - РћРїС‹С‚ СЃ СЃРѕР±Р°РєР°РјРё: ${if (answer.q3_dog_experience == true) "Р”Р°" else if (answer.q3_dog_experience == false) "РќРµС‚" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - РћРїС‹С‚ СЃ РєРѕС€РєР°РјРё: ${if (answer.q3_cat_experience == true) "Р”Р°" else if (answer.q3_cat_experience == false) "РќРµС‚" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - РћРїС‹С‚ СЃ РѕСЃРѕР±РµРЅРЅС‹РјРё Р¶РёРІРѕС‚РЅС‹РјРё: ${if (answer.q3_special_needs_experience == true) "Р”Р°" else if (answer.q3_special_needs_experience == false) "РќРµС‚" else "РќРµ СѓРєР°Р·Р°РЅРѕ"}
           - РџРѕС‡РµРјСѓ СЃРµР№С‡Р°СЃ: ${answer.q3_why_now}

        4. РћС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚СЊ Рё РіРѕС‚РѕРІРЅРѕСЃС‚СЊ (РџР РРћР РРўР•Рў: Р’Р«РЎРћРљРР™):
           - РџРѕРЅРёРјР°РµС‚ С‚СЂРµР±РѕРІР°РЅРёСЏ: ${if (answer.q4_understand_requirements) "Р”Р°" else "РќРµС‚ [РљР РРўРР§Р•РЎРљРР™ Р РРЎРљ: РЅРµ РїРѕРЅРёРјР°РµС‚ РѕС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚СЊ]"}
           - Р“РѕС‚РѕРІ РєРѕ РІСЂРµРјРµРЅРё: ${if (answer.q4_understand_time) "Р”Р°" else "РќРµС‚"}
           - Р“РѕС‚РѕРІ Рє РІРЅРёРјР°РЅРёСЋ: ${if (answer.q4_understand_attention) "Р”Р°" else "РќРµС‚"}
           - Р“РѕС‚РѕРІ Рє РѕР±СѓС‡РµРЅРёСЋ: ${if (answer.q4_understand_training) "Р”Р°" else "РќРµС‚"}
           - Р“РѕС‚РѕРІ Рє РІРµС‚РїРѕРјРѕС‰Рё: ${if (answer.q4_understand_vet_care) "Р”Р°" else "РќРµС‚"}
           - РџРѕС‚СЂРµР±РЅРѕСЃС‚Рё: ${answer.understandsNeeds.joinToString()} ${if (answer.understandsNeeds.isEmpty()) "[Р РРЎРљ: РЅРµ РїРѕРЅРёРјР°РµС‚ РїРѕС‚СЂРµР±РЅРѕСЃС‚РµР№]" else ""}
           - РџР»Р°РЅ РїСЂРё РїРѕСЂС‡Рµ РјРµР±РµР»Рё: ${answer.q4_furniture_damage_plan} ${if (answer.q4_furniture_damage_plan.isNullOrBlank() || answer.q4_furniture_damage_plan.length < 5) "[Р РРЎРљ: РЅРµС‚ РїР»Р°РЅР°]" else ""}
           - РџР»Р°РЅ РїСЂРё С€СѓРјРµ: ${answer.q4_noise_plan} ${if (answer.q4_noise_plan.isNullOrBlank() || answer.q4_noise_plan.length < 5) "[Р РРЎРљ: РЅРµС‚ РїР»Р°РЅР°]" else ""}
           - РџР»Р°РЅ РїСЂРё РїСѓРіР»РёРІРѕСЃС‚Рё: ${answer.q4_shy_pet_plan} ${if (answer.q4_shy_pet_plan.isNullOrBlank() || answer.q4_shy_pet_plan.length < 5) "[Р РРЎРљ: РЅРµС‚ РїР»Р°РЅР°]" else ""}
           - РџР»Р°РЅ РїСЂРё РґРѕР»РіРѕР№ Р°РґР°РїС‚Р°С†РёРё: ${answer.q4_long_adaptation_plan} ${if (answer.q4_long_adaptation_plan.isNullOrBlank() || answer.q4_long_adaptation_plan.length < 5) "[Р РРЎРљ: РЅРµС‚ РїР»Р°РЅР°]" else ""}
           - Р“РѕС‚РѕРІ Рє РІРѕСЃРїРёС‚Р°РЅРёСЋ: ${if (answer.q4_ready_education) "Р”Р°" else "РќРµС‚ [Р РРЎРљ: РЅРµ РіРѕС‚РѕРІ Рє РІРѕСЃРїРёС‚Р°РЅРёСЋ]"}
           - Р–РёР·РЅРµРЅРЅС‹Рµ РёР·РјРµРЅРµРЅРёСЏ: ${answer.q4_life_changes_plan}
           - РџСЂРµРїСЏС‚СЃС‚РІРёСЏ: ${answer.q4_obstacles_next_year}

        5. Р‘РµР·РѕРїР°СЃРЅРѕСЃС‚СЊ (РџР РРћР РРўР•Рў: Р’Р«РЎРћРљРР™):
           - РњРµСЂС‹ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё: ${answer.q5_safety_measures.ifEmpty { listOf("вЂ”") }.joinToString()} ${if (answer.q5_safety_measures.isEmpty()) "[Р РРЎРљ: РЅРµС‚ РјРµСЂ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё]" else ""}
           - Р“РѕС‚РѕРІ Рє СЃС‚РµСЂРёР»РёР·Р°С†РёРё: ${if (answer.q5_ready_neuter) "Р”Р°" else "РќРµС‚ [Р РРЎРљ: РЅРµ РіРѕС‚РѕРІ Рє СЃС‚РµСЂРёР»РёР·Р°С†РёРё]"}
           - Р“РѕС‚РѕРІ Рє СЂРµРєРѕРјРµРЅРґР°С†РёСЏРј: ${if (answer.q5_ready_recommendations) "Р”Р°" else "РќРµС‚ [Р РРЎРљ: РЅРµ РіРѕС‚РѕРІ СЃР»РµРґРѕРІР°С‚СЊ СЂРµРєРѕРјРµРЅРґР°С†РёСЏРј]"}
           - Р“РѕС‚РѕРІ Рє Р°РґСЂРµСЃРЅРёРєСѓ: ${if (answer.q5_ready_tracker) "Р”Р°" else "РќРµС‚"}
           - Р“РѕС‚РѕРІ РїРѕРґРґРµСЂР¶РёРІР°С‚СЊ СЃРІСЏР·СЊ: ${if (answer.q5_ready_keep_contact) "Р”Р°" else "РќРµС‚ [Р РРЎРљ: РЅРµ РіРѕС‚РѕРІ Рє РѕР±СЂР°С‚РЅРѕР№ СЃРІСЏР·Рё]"}

        6. Р­РјРѕС†РёРѕРЅР°Р»СЊРЅР°СЏ С‡Р°СЃС‚СЊ (РџР РРћР РРўР•Рў: СЃСЂРµРґРЅРёР№ вЂ” РєР°С‡РµСЃС‚РІРµРЅРЅС‹Рµ РѕС‚РІРµС‚С‹):
           - Р§С‚Рѕ Р·РЅР°С‡РёС‚ РѕС‚РІРµС‚СЃС‚РІРµРЅРЅС‹Р№ С…РѕР·СЏРёРЅ: ${answer.q6_responsible_owner_meaning}
           - Р’РёРґРµРЅРёРµ Р¶РёР·РЅРё СЃ РїРёС‚РѕРјС†РµРј: ${answer.q6_life_with_pet_vision}
           - РџРѕС‡РµРјСѓ С…РѕСЂРѕС€РёР№ С…РѕР·СЏРёРЅ: ${answer.q6_why_good_owner}

        7. Р–РµР»Р°РµРјС‹Рµ РІРёРґС‹ Р¶РёРІРѕС‚РЅС‹С… (РџР РРћР РРўР•Рў: РЅРёР·РєРёР№ вЂ” РґР»СЏ СЂРµРєРѕРјРµРЅРґР°С†РёРё):
           - РљР°РєРёРµ РїРёС‚РѕРјС†С‹ Р¶РµР»Р°РµРјС‹: ${answer.q7_desired_pets.joinToString { it.ifEmpty { "РЅРµ СѓРєР°Р·Р°РЅРѕ" } }}

        **РџР РђР’РР›Рђ РћР¦Р•РќРљР Р РРЎРљРћР’:**
        
        1. **Р’СЂРµРјСЏ РїРёС‚РѕРјС†Р° РІ РѕРґРёРЅРѕС‡РµСЃС‚РІРµ (РљР РРўРР§Р•РЎРљР Р’РђР–РќРћ):**
           - 0-4 С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” РћРўР›РР§РќРћ (СЃРЅРёР¶Р°РµС‚ СЂРёСЃРє РЅР° 15-20 Р±Р°Р»Р»РѕРІ)
           - 5-6 С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” РҐРћР РћРЁРћ (СЃРЅРёР¶Р°РµС‚ СЂРёСЃРє РЅР° 5-10 Р±Р°Р»Р»РѕРІ)
           - 7-8 С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” Р”РћРџРЈРЎРўРРњРћ (Р±РµР· РёР·РјРµРЅРµРЅРёСЏ)
           - 9-10 С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” РџРћР’Р«РЁР•РќРќР«Р™ Р РРЎРљ (+10-15 Р±Р°Р»Р»РѕРІ Рє СЂРёСЃРєСѓ)
           - 11+ С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” Р’Р«РЎРћРљРР™ Р РРЎРљ (+20-30 Р±Р°Р»Р»РѕРІ Рє СЂРёСЃРєСѓ, РїРёС‚РѕРјРµС† Р±СѓРґРµС‚ СЃС‚СЂР°РґР°С‚СЊ)
           - Р§РµРј РњР•РќР¬РЁР• РІСЂРµРјРµРЅРё РїРёС‚РѕРјРµС† РїСЂРѕРІРѕРґРёС‚ РѕРґРёРЅ, С‚РµРј Р›РЈР§РЁР• РґР»СЏ РµРіРѕ РїСЃРёС…РёРєРё Рё Р·РґРѕСЂРѕРІСЊСЏ

        2. **РџСЂРёРѕСЂРёС‚РµС‚С‹ РѕР±СЂР°Р±РѕС‚РєРё РїРѕР»РµР№ (РѕС‚ РІС‹СЃРѕРєРѕРіРѕ Рє РЅРёР·РєРѕРјСѓ):**
           
           РџР РРћР РРўР•Рў Р’Р«РЎРћРљРР™ (РєСЂРёС‚РёС‡РµСЃРєРёРµ С„Р°РєС‚РѕСЂС‹):
           - q2_pets_allowed: Р±РµР· СЂР°Р·СЂРµС€РµРЅРёСЏ РЅР° Р¶РёРІРѕС‚РЅС‹С… вЂ” Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєРёР№ REJECT
           - q2_family_consent: Р±РµР· СЃРѕРіР»Р°СЃРёСЏ СЃРµРјСЊРё вЂ” РІС‹СЃРѕРєРёР№ СЂРёСЃРє
           - q2_hours_alone: >8 С‡Р°СЃРѕРІ вЂ” РІС‹СЃРѕРєРёР№ СЂРёСЃРє (СЃРј. РїСЂР°РІРёР»Рѕ РІС‹С€Рµ)
           - q4_understand_requirements: РЅРµРїРѕРЅРёРјР°РЅРёРµ РѕС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚Рё вЂ” РІС‹СЃРѕРєРёР№ СЂРёСЃРє
           - q5_safety_measures: РѕС‚СЃСѓС‚СЃС‚РІРёРµ РјРµСЂ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё вЂ” СЃСЂРµРґРЅРёР№/РІС‹СЃРѕРєРёР№ СЂРёСЃРє
           - q5_ready_neuter: РѕС‚РєР°Р· РѕС‚ СЃС‚РµСЂРёР»РёР·Р°С†РёРё вЂ” РІС‹СЃРѕРєРёР№ СЂРёСЃРє
           - q3_had_pets_before: РѕС‚СЃСѓС‚СЃС‚РІРёРµ РѕРїС‹С‚Р° вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє
           - q3_what_happened: РЅРµРіР°С‚РёРІРЅР°СЏ РёСЃС‚РѕСЂРёСЏ (РІС‹Р±СЂРѕСЃРёР»/РѕС‚РґР°Р») вЂ” РєСЂРёС‚РёС‡РµСЃРєРёР№ СЂРёСЃРє
           
           РџР РРћР РРўР•Рў РЎР Р•Р”РќРР™:
           - q2_housing_type: СЃСЉС‘РјРЅРѕРµ Р¶РёР»СЊС‘ вЂ” РЅРµР±РѕР»СЊС€РѕР№ СЂРёСЃРє РЅРµСЃС‚Р°Р±РёР»СЊРЅРѕСЃС‚Рё
           - q2_caregiver: РѕС‚СЃСѓС‚СЃС‚РІРёРµ РїР»Р°РЅР° СѓС…РѕРґР° вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє
           - q4_*_plan: РѕС‚СЃСѓС‚СЃС‚РІРёРµ РїР»Р°РЅРѕРІ РґР»СЏ СЃР»РѕР¶РЅС‹С… СЃРёС‚СѓР°С†РёР№ вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє
           - q4_ready_education: РЅРµРіРѕС‚РѕРІРЅРѕСЃС‚СЊ Рє РІРѕСЃРїРёС‚Р°РЅРёСЋ вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє
           - q5_ready_keep_contact: РЅРµРіРѕС‚РѕРІРЅРѕСЃС‚СЊ Рє РѕР±СЂР°С‚РЅРѕР№ СЃРІСЏР·Рё вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє
           - q1_age: РІРѕР·СЂР°СЃС‚ <18 РёР»Рё >90 вЂ” РЅРµР±РѕР»СЊС€РѕР№ СЂРёСЃРє
           
           РџР РРћР РРўР•Рў РќРР—РљРР™:
           - q6_*: СЌРјРѕС†РёРѕРЅР°Р»СЊРЅС‹Рµ РѕС‚РІРµС‚С‹ вЂ” РґР»СЏ qualitative РѕС†РµРЅРєРё
           - q7_desired_pets: РґР»СЏ СЂРµРєРѕРјРµРЅРґР°С†РёРё РїРѕРґС…РѕРґСЏС‰РёС… РїРёС‚РѕРјС†РµРІ

        3. **Р—Р°РЅСЏС‚РѕСЃС‚СЊ РєР°РЅРґРёРґР°С‚Р°:**
           - Р•СЃР»Рё РєР°РЅРґРёРґР°С‚ СѓРєР°Р·С‹РІР°РµС‚ РїСЂРѕС„РµСЃСЃРёСЋ РІ РєСЂСѓРїРЅРѕР№ РѕСЂРіР°РЅРёР·Р°С†РёРё (РЎР±РµСЂР±Р°РЅРє Рё РґСЂ.) вЂ” РїСЂРѕРІРµСЂРёС‚СЊ РЅР° РЅРµРЅРѕСЂРјРёСЂРѕРІР°РЅРЅС‹Р№ РґРµРЅСЊ
           - Р•СЃР»Рё РєР°РЅРґРёРґР°С‚ Р¶РёРІС‘С‚ РѕРґРёРЅ (q2_living_with РїСѓСЃС‚РѕР№) вЂ” РЅРµС‚ РїРѕРґРґРµСЂР¶РєРё РІ СѓС…РѕРґРµ

        4. **РћРїС‹С‚ СЃ Р¶РёРІРѕС‚РЅС‹РјРё:**
           - РћС‚СЃСѓС‚СЃС‚РІРёРµ РѕРїС‹С‚Р° (q3_had_pets_before = "РќРµС‚") вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє, РЅРѕ РЅРµ РєСЂРёС‚РёС‡РЅС‹Р№
           - РќРµРіР°С‚РёРІРЅР°СЏ РёСЃС‚РѕСЂРёСЏ (РІС‹Р±СЂРѕСЃРёР», РѕС‚РґР°Р», СѓРјРµСЂР»Рё РїРѕ РІРёРЅРµ) вЂ” РљР РРўРР§Р•РЎРљРР™ Р РРЎРљ
           - РћС‚СЃСѓС‚СЃС‚РІРёРµ РѕРїС‹С‚Р° СЃ РєРѕРЅРєСЂРµС‚РЅС‹Рј РІРёРґРѕРј (СЃРѕР±Р°РєР°/РєРѕС€РєР°) РїСЂРё Р¶РµР»Р°РЅРёРё РІР·СЏС‚СЊ РµРіРѕ вЂ” СЃСЂРµРґРЅРёР№ СЂРёСЃРє

        **Р—РђР”РђР§Рђ:**
        РћС†РµРЅРё СЂРёСЃРєРё РїРѕ С€РєР°Р»Рµ РѕС‚ 0 РґРѕ 100 Рё РІРµСЂРЅРё РѕС‚РІРµС‚ РўРћР›Р¬РљРћ РІ С„РѕСЂРјР°С‚Рµ JSON:

        {
            "overallRisk": "LOW" | "MEDIUM" | "HIGH" | "VERY_HIGH",
            "riskScore": С‡РёСЃР»Рѕ РѕС‚ 0 РґРѕ 100,
            "riskFactors": [
                {"category": "РєР°С‚РµРіРѕСЂРёСЏ", "severity": "LOW" | "MEDIUM" | "HIGH", "description": "РѕРїРёСЃР°РЅРёРµ", "suggestion": "РїСЂРµРґР»РѕР¶РµРЅРёРµ"}
            ],
            "positiveFactors": ["РїРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Р№ С„Р°РєС‚РѕСЂ 1", "РїРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Р№ С„Р°РєС‚РѕСЂ 2"],
            "recommendations": ["СЂРµРєРѕРјРµРЅРґР°С†РёСЏ 1", "СЂРµРєРѕРјРµРЅРґР°С†РёСЏ 2"],
            "detailedAnalysis": "СЂР°Р·РІС‘СЂРЅСѓС‚С‹Р№ Р°РЅР°Р»РёР· РЅР° СЂСѓСЃСЃРєРѕРј СЏР·С‹РєРµ",
            "recommendation": "APPROVE" | "APPROVE_WITH_CONDITIONS" | "REVIEW_REQUIRED" | "REJECT"
        }

        РљСЂРёС‚РµСЂРёРё РѕС†РµРЅРєРё:
        - LOW (0-25): РћС‚Р»РёС‡РЅС‹Рµ СѓСЃР»РѕРІРёСЏ, Р±РѕР»СЊС€РѕР№ РѕРїС‹С‚, РїРѕР»РЅР°СЏ РіРѕС‚РѕРІРЅРѕСЃС‚СЊ, РїРёС‚РѕРјРµС† РЅРµ Р±СѓРґРµС‚ РґРѕР»РіРѕ РѕРґРёРЅ
        - MEDIUM (26-50): РҐРѕСЂРѕС€РёРµ СѓСЃР»РѕРІРёСЏ, РµСЃС‚СЊ РЅРµР±РѕР»СЊС€РёРµ СЂРёСЃРєРё (РЅР°РїСЂРёРјРµСЂ, 7-8 С‡Р°СЃРѕРІ РѕРґРёРЅ)
        - HIGH (51-75): Р—РЅР°С‡РёС‚РµР»СЊРЅС‹Рµ СЂРёСЃРєРё (9-10 С‡Р°СЃРѕРІ РѕРґРёРЅ, РЅРµС‚ РѕРїС‹С‚Р°, СЃСЉС‘РјРЅРѕРµ Р¶РёР»СЊС‘)
        - VERY_HIGH (76-100): РљСЂРёС‚РёС‡РµСЃРєРёРµ СЂРёСЃРєРё (11+ С‡Р°СЃРѕРІ РѕРґРёРЅ, РЅРµС‚ СЂР°Р·СЂРµС€РµРЅРёСЏ, РЅРµРіР°С‚РёРІРЅР°СЏ РёСЃС‚РѕСЂРёСЏ)

        **Р¤Р°РєС‚РѕСЂС‹ СЂРёСЃРєР° (СЃ СѓС‡С‘С‚РѕРј РІСЂРµРјРµРЅРё РІ РѕРґРёРЅРѕС‡РµСЃС‚РІРµ):**
        - РќРµС‚ СЃРѕРіР»Р°СЃРёСЏ СЃРµРјСЊРё
        - РќРµС‚ СЂР°Р·СЂРµС€РµРЅРёСЏ РЅР° Р¶РёРІРѕС‚РЅС‹С… РІ Р¶РёР»СЊРµ
        - 9+ С‡Р°СЃРѕРІ РІ РґРµРЅСЊ РѕРґРёРЅ (С‡РµРј Р±РѕР»СЊС€Рµ, С‚РµРј С…СѓР¶Рµ)
        - РќРµС‚ РѕРїС‹С‚Р° СЃ Р¶РёРІРѕС‚РЅС‹РјРё
        - РќРµ РїРѕРЅРёРјР°РµС‚ РѕС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚СЊ
        - РќРµС‚ РјРµСЂ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё
        - РќРµРїСЂРµРґСЃРєР°Р·СѓРµРјС‹Рµ Р¶РёР·РЅРµРЅРЅС‹Рµ РѕР±СЃС‚РѕСЏС‚РµР»СЊСЃС‚РІР°
        - Р’С‹СЃРѕРєР°СЏ Р·Р°РЅСЏС‚РѕСЃС‚СЊ РЅР° СЂР°Р±РѕС‚Рµ (9+ С‡Р°СЃРѕРІ)
        - РќРµРіР°С‚РёРІРЅР°СЏ РёСЃС‚РѕСЂРёСЏ СЃ РїСЂРµРґС‹РґСѓС‰РёРјРё РїРёС‚РѕРјС†Р°РјРё
        
        **РџРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Рµ С„Р°РєС‚РѕСЂС‹:**
        - РћРїС‹С‚ СЃ Р¶РёРІРѕС‚РЅС‹РјРё (РѕСЃРѕР±РµРЅРЅРѕ РїРѕР»РѕР¶РёС‚РµР»СЊРЅС‹Р№)
        - Р“РѕС‚РѕРІРЅРѕСЃС‚СЊ Рє СЂР°СЃС…РѕРґР°Рј Рё РІРµС‚РїРѕРјРѕС‰Рё
        - РњРµСЂС‹ Р±РµР·РѕРїР°СЃРЅРѕСЃС‚Рё СѓСЃС‚Р°РЅРѕРІР»РµРЅС‹
        - РџРѕРЅРёРјР°РЅРёРµ РѕС‚РІРµС‚СЃС‚РІРµРЅРЅРѕСЃС‚Рё
        - РџРѕРґРґРµСЂР¶РєР° СЃРµРјСЊРё
        - 0-6 С‡Р°СЃРѕРІ РІ РґРµРЅСЊ РѕРґРёРЅ (РёРґРµР°Р»СЊРЅРѕ РґР»СЏ РїРёС‚РѕРјС†Р°)
        - Р“РёР±РєРёР№ РіСЂР°С„РёРє СЂР°Р±РѕС‚С‹ РёР»Рё РІРѕР·РјРѕР¶РЅРѕСЃС‚СЊ Р±СЂР°С‚СЊ РїРёС‚РѕРјС†Р° РЅР° СЂР°Р±РѕС‚Сѓ
        - РќР°Р»РёС‡РёРµ РїР»Р°РЅР° РЅР° СЃР»РѕР¶РЅС‹Рµ СЃРёС‚СѓР°С†РёРё
        """.trimIndent()
    }
    
    /**
     * Р“РµРЅРµСЂРёСЂСѓРµС‚ РїРѕРґСЃРєР°Р·РєСѓ Рѕ СЂРёСЃРєРµ РЅР° РѕСЃРЅРѕРІРµ С‡Р°СЃРѕРІ РІ РѕРґРёРЅРѕС‡РµСЃС‚РІРµ
     */
    private fun getHoursAloneRiskHint(hours: Int?): String {
        if (hours == null) return "[Р РРЎРљ: РЅРµ СѓРєР°Р·Р°РЅРѕ]"
        return when {
            hours <= 4 -> "[РћРўР›РР§РќРћ: РјРёРЅРёРјР°Р»СЊРЅРѕРµ РІСЂРµРјСЏ РІ РѕРґРёРЅРѕС‡РµСЃС‚РІРµ]"
            hours <= 6 -> "[РҐРћР РћРЁРћ: РїСЂРёРµРјР»РµРјРѕРµ РІСЂРµРјСЏ]"
            hours <= 8 -> "[Р”РћРџРЈРЎРўРРњРћ: СЃС‚Р°РЅРґР°СЂС‚РЅРѕРµ СЂР°Р±РѕС‡РµРµ РІСЂРµРјСЏ]"
            hours <= 10 -> "[РџРћР’Р«РЁР•РќРќР«Р™ Р РРЎРљ: РїРёС‚РѕРјРµС† Р±СѓРґРµС‚ РїСЂРѕРІРѕРґРёС‚СЊ РјРЅРѕРіРѕ РІСЂРµРјРµРЅРё РѕРґРёРЅ]"
            else -> "[Р’Р«РЎРћРљРР™ Р РРЎРљ: 11+ С‡Р°СЃРѕРІ РІ РґРµРЅСЊ вЂ” РїРёС‚РѕРјРµС† Р±СѓРґРµС‚ СЃС‚СЂР°РґР°С‚СЊ]"
        }
    }
    
    /**
     * РћС‚РїСЂР°РІР»СЏРµС‚ Р·Р°РїСЂРѕСЃ Рє GigaChat API
     */
    private suspend fun sendChatRequest(jwtToken: String, prompt: String): String {
        Log.d(TAG, "РћС‚РїСЂР°РІРєР° Р·Р°РїСЂРѕСЃР° Рє GigaChat API...")
        
        // РџС‹С‚Р°РµРјСЃСЏ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ РјРѕРґРµР»Рё РїРѕ РїСЂРёРѕСЂРёС‚РµС‚Сѓ (РѕС‚ Р»СѓС‡С€РµР№ Рє Р±Р°Р·РѕРІРѕР№)
        val modelsToTry = listOf("GigaChat-2", "GigaChat", "GigaChat", "GigaChat")
        val json = Json { isLenient = true; ignoreUnknownKeys = true }
        
        for (model in modelsToTry) {
            Log.d(TAG, "РџРѕРїС‹С‚РєР° СЃ РјРѕРґРµР»СЊСЋ: $model")
            
            val bodyString = json.encodeToString(GigaChatRequest.serializer(), GigaChatRequest(
                model = model,
                messages = listOf(Message(role = "user", content = prompt)),
                temperature = 0.3,
                max_tokens = 4096
            ))
            
            val response = client.post("$baseUrl/chat/completions") {
                header("Authorization", "Bearer $jwtToken")
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("X-Request-ID", UUID.randomUUID().toString())
                setBody(bodyString)
            }
            
            Log.d(TAG, "РЎС‚Р°С‚СѓСЃ РѕС‚РІРµС‚Р° РґР»СЏ $model: ${response.status}")
            
            if (response.status.value == 200) {
                val responseBody = response.body<String>()
                Log.d(TAG, "РЈСЃРїРµС…! РўРµР»Рѕ РѕС‚РІРµС‚Р°, РґР»РёРЅР°: ${responseBody.length} СЃРёРјРІРѕР»РѕРІ")
                
                // РџР°СЂСЃРёРј РѕС‚РІРµС‚ РІСЂСѓС‡РЅСѓСЋ
                val element = json.parseToJsonElement(responseBody)
                val choices = element.jsonObject["choices"]?.jsonArray
                val message = choices?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
                val content = message?.get("content")?.jsonPrimitive?.content
                
                if (content != null) {
                    Log.d(TAG, "РљРѕРЅС‚РµРЅС‚ СѓСЃРїРµС€РЅРѕ РёР·РІР»РµС‡С‘РЅ РёР· РјРѕРґРµР»Рё $model, РґР»РёРЅР°: ${content.length} СЃРёРјРІРѕР»РѕРІ")
                    Log.d(TAG, "РџРµСЂРІС‹Рµ 300 СЃРёРјРІРѕР»РѕРІ РєРѕРЅС‚РµРЅС‚Р°: ${content.take(300)}")
                    return content
                } else {
                    Log.e(TAG, "РќРµ СѓРґР°Р»РѕСЃСЊ РёР·РІР»РµС‡СЊ content РёР· РѕС‚РІРµС‚Р°")
                    Log.e(TAG, "РџРѕР»РЅС‹Р№ РѕС‚РІРµС‚: $responseBody")
                }
            } else {
                val errorBody = response.body<String>()
                Log.w(TAG, "РњРѕРґРµР»СЊ $model РЅРµ РїРѕРґРѕС€Р»Р°: ${response.status} - $errorBody")
            }
        }
        
        throw IllegalStateException("РќРё РѕРґРЅР° РёР· РјРѕРґРµР»РµР№ РЅРµ СЂР°Р±РѕС‚Р°Р»Р°. РџСЂРѕРІРµСЂСЊС‚Рµ РґРѕСЃС‚СѓРї Рє GigaChat API.")
    }
    
    private suspend fun getAvailableModels(jwtToken: String): List<String> {
        return try {
            val response = client.get("$baseUrl/models") {
                header("Authorization", "Bearer $jwtToken")
                header("Accept", "application/json")
            }
            
            if (response.status.value == 200) {
                val responseBody = response.body<String>()
                Log.d(TAG, "РћС‚РІРµС‚ /models: $responseBody")
                
                // Р СѓС‡РЅРѕР№ РїР°СЂСЃРёРЅРі JSON С‡РµСЂРµР· JsonPrimitive
                val json = Json { isLenient = true; ignoreUnknownKeys = true }
                val element = json.parseToJsonElement(responseBody)
                val data = element.jsonObject["data"]?.jsonArray
                
                data?.mapNotNull { jsonElement ->
                    jsonElement.jsonObject["id"]?.jsonPrimitive?.content
                } ?: emptyList()
            } else {
                Log.w(TAG, "РќРµ СѓРґР°Р»РѕСЃСЊ РїРѕР»СѓС‡РёС‚СЊ СЃРїРёСЃРѕРє РјРѕРґРµР»РµР№: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "РћС€РёР±РєР° РїСЂРё РїРѕР»СѓС‡РµРЅРёРё СЃРїРёСЃРєР° РјРѕРґРµР»РµР№", e)
            emptyList()
        }
    }
    
    /**
     * РџР°СЂСЃРёС‚ РѕС‚РІРµС‚ РѕС‚ GigaChat РІ СЃС‚СЂСѓРєС‚СѓСЂСѓ РѕС†РµРЅРєРё СЂРёСЃРєРѕРІ
     */
    private fun parseRiskAssessment(response: String): GigaChatRiskAssessment {
        Log.d(TAG, "=== РџР°СЂСЃРёРЅРі РѕС‚РІРµС‚Р° GigaChat ===")
        Log.d(TAG, "Р”Р»РёРЅР° РѕС‚РІРµС‚Р°: ${response.length} СЃРёРјРІРѕР»РѕРІ")
        Log.d(TAG, "РџРµСЂРІС‹Рµ 500 СЃРёРјРІРѕР»РѕРІ: ${response.take(500)}")
        
        // РР·РІР»РµРєР°РµРј JSON РёР· РѕС‚РІРµС‚Р° (РЅР° СЃР»СѓС‡Р°Р№ РµСЃР»Рё РѕРЅ РѕР±РµСЂРЅСѓС‚ РІ С‚РµРєСЃС‚)
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        
        if (jsonStart == -1 || jsonEnd == -1) {
            Log.e(TAG, "РќРµ СѓРґР°Р»РѕСЃСЊ РЅР°Р№С‚Рё JSON РІ РѕС‚РІРµС‚Рµ. РќР°С‡Р°Р»Рѕ: $jsonStart, РљРѕРЅРµС†: $jsonEnd")
            Log.e(TAG, "РџРѕР»РЅС‹Р№ РѕС‚РІРµС‚: $response")
            throw IllegalStateException("РќРµ СѓРґР°Р»РѕСЃСЊ РЅР°Р№С‚Рё JSON РІ РѕС‚РІРµС‚Рµ: $response")
        }
        
        Log.d(TAG, "РќР°Р№РґРµРЅ JSON СЃ РїРѕР·РёС†РёРё $jsonStart РїРѕ $jsonEnd")
        var jsonStr = response.substring(jsonStart, jsonEnd + 1)
        Log.d(TAG, "Р”Р»РёРЅР° РёР·РІР»РµС‡РµРЅРЅРѕРіРѕ JSON: ${jsonStr.length} СЃРёРјРІРѕР»РѕРІ")
        
        // РќРѕСЂРјР°Р»РёР·СѓРµРј РЅРµСЃС‚Р°РЅРґР°СЂС‚РЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ severity РѕС‚ GigaChat
        jsonStr = normalizeSeverityValues(jsonStr)
        // РќРѕСЂРјР°Р»РёР·СѓРµРј РЅРµСЃС‚Р°РЅРґР°СЂС‚РЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ recommendation РІ РјР°СЃСЃРёРІРµ recommendations
        jsonStr = normalizeRecommendationValues(jsonStr)
        
        return try {
            val assessment = Json.decodeFromString<GigaChatRiskAssessment>(jsonStr)
            Log.d(TAG, "вњ… РЈСЃРїРµС€РЅС‹Р№ РїР°СЂСЃРёРЅРі:")
            Log.d(TAG, "  overallRisk: ${assessment.overallRisk}")
            Log.d(TAG, "  riskScore: ${assessment.riskScore}")
            Log.d(TAG, "  riskFactors: ${assessment.riskFactors.size} С„Р°РєС‚РѕСЂРѕРІ")
            Log.d(TAG, "  positiveFactors: ${assessment.positiveFactors.size} С„Р°РєС‚РѕСЂРѕРІ")
            Log.d(TAG, "  recommendations: ${assessment.recommendations.size} СЂРµРєРѕРјРµРЅРґР°С†РёР№")
            Log.d(TAG, "  detailedAnalysis: ${assessment.detailedAnalysis.length} СЃРёРјРІРѕР»РѕРІ")
            Log.d(TAG, "  recommendation: ${assessment.recommendation}")
            assessment
        } catch (e: Exception) {
            Log.e(TAG, "вќЊ РћС€РёР±РєР° РїР°СЂСЃРёРЅРіР° JSON: ${e.message}")
            Log.e(TAG, "JSON СЃС‚СЂРѕРєР°: $jsonStr")
            e.printStackTrace()
            // Р•СЃР»Рё РїР°СЂСЃРёРЅРі РЅРµ СѓРґР°Р»СЃСЏ, СЃРѕР·РґР°С‘Рј Р±Р°Р·РѕРІСѓСЋ РѕС†РµРЅРєСѓ
            createFallbackAssessment(response)
        }
    }
    
    /**
     * РќРѕСЂРјР°Р»РёР·СѓРµС‚ РЅРµСЃС‚Р°РЅРґР°СЂС‚РЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ severity РѕС‚ GigaChat
     * GigaChat РјРѕР¶РµС‚ РІРѕР·РІСЂР°С‰Р°С‚СЊ: MED, HI, VERY, CRIT Рё С‚.Рґ.
     */
    private fun normalizeSeverityValues(json: String): String {
        val severityMap = mapOf(
            "\"MED\"" to "\"MEDIUM\"",
            "\"HI\"" to "\"HIGH\"",
            "\"VERY\"" to "\"VERY_HIGH\"",
            "\"CRIT\"" to "\"CRITICAL\""
        )
        var result = json
        for ((shorthand, full) in severityMap) {
            result = result.replace(shorthand, full)
        }
        return result
    }
    
    /**
     * РќРѕСЂРјР°Р»РёР·СѓРµС‚ РЅРµСЃС‚Р°РЅРґР°СЂС‚РЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ recommendation РІ РјР°СЃСЃРёРІРµ recommendations
     * GigaChat РјРѕР¶РµС‚ РІРѕР·РІСЂР°С‰Р°С‚СЊ "REJECT" РєР°Рє СЌР»РµРјРµРЅС‚ РјР°СЃСЃРёРІР° РІРјРµСЃС‚Рѕ С‚РµРєСЃС‚РѕРІРѕР№ СЂРµРєРѕРјРµРЅРґР°С†РёРё
     */
    private fun normalizeRecommendationValues(json: String): String {
        val recommendationCodes = listOf("REJECT", "APPROVE", "APPROVE_WITH_CONDITIONS", "REVIEW_REQUIRED")
        val recommendationDescriptions = mapOf(
            "REJECT" to "Р РµРєРѕРјРµРЅРґСѓРµС‚СЃСЏ РѕС‚РєР»РѕРЅРёС‚СЊ Р·Р°СЏРІРєСѓ",
            "APPROVE" to "Р РµРєРѕРјРµРЅРґСѓРµС‚СЃСЏ РѕРґРѕР±СЂРёС‚СЊ Р·Р°СЏРІРєСѓ",
            "APPROVE_WITH_CONDITIONS" to "РћРґРѕР±СЂРёС‚СЊ СЃ СѓСЃР»РѕРІРёСЏРјРё",
            "REVIEW_REQUIRED" to "РўСЂРµР±СѓРµС‚СЃСЏ РґРѕРїРѕР»РЅРёС‚РµР»СЊРЅР°СЏ РїСЂРѕРІРµСЂРєР°"
        )
        
        var result = json
        for (code in recommendationCodes) {
            // Р—Р°РјРµРЅСЏРµРј РєРѕРґ СЂРµРєРѕРјРµРЅРґР°С†РёРё РІ РјР°СЃСЃРёРІРµ recommendations РЅР° С‚РµРєСЃС‚РѕРІРѕРµ РѕРїРёСЃР°РЅРёРµ
            // РС‰РµРј С€Р°Р±Р»РѕРЅ: "REJECT" (РІРЅСѓС‚СЂРё РјР°СЃСЃРёРІР° recommendations)
            val description = recommendationDescriptions[code] ?: code
            result = result.replace("\"$code\"", "\"$description\"")
        }
        return result
    }
    
    /**
     * РЎРѕР·РґР°С‘С‚ РѕС†РµРЅРєСѓ СЂРёСЃРєРѕРІ РїСЂРё РѕС€РёР±РєРµ РїР°СЂСЃРёРЅРіР°
     */
    private fun createFallbackAssessment(rawResponse: String): GigaChatRiskAssessment {
        return GigaChatRiskAssessment(
            overallRisk = RiskLevel.MEDIUM,
            riskScore = 50,
            riskFactors = emptyList(),
            positiveFactors = listOf("РљР°РЅРґРёРґР°С‚ РїСЂРѕС€С‘Р» РѕРїСЂРѕСЃРЅРёРє"),
            recommendations = listOf("РџСЂРѕРІРµСЃС‚Рё Р»РёС‡РЅСѓСЋ РІСЃС‚СЂРµС‡Сѓ РґР»СЏ СѓС‚РѕС‡РЅРµРЅРёСЏ РґРµС‚Р°Р»РµР№"),
            detailedAnalysis = rawResponse.take(500),
            recommendation = "РўСЂРµР±СѓРµС‚СЃСЏ РґРѕРїРѕР»РЅРёС‚РµР»СЊРЅР°СЏ РїСЂРѕРІРµСЂРєР°"
        )
    }
}
