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
import com.example.petadopt.data.model.Recommendation
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
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// DTO для ответа OAuth
@Serializable
data class OAuthTokenResponse(
    val access_token: String,
    val expires_at: Long? = null
)

// DTO для запроса к GigaChat
@Serializable
data class GigaChatRequest(
    val model: String = "GigaChat-Pro",
    val messages: List<Message>,
    val temperature: Double = 0.3,
    val max_tokens: Int = 2000
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

/**
 * Репозиторий для взаимодействия с GigaChat API
 * Оценивает риски передачи питомца на основе ответов опросника
 */
@Singleton
class GigaChatRepository @Inject constructor() {
    
    private val client = HttpClient(OkHttp) {
        engine {
            config {
                // Создаём SSL контекст, который доверяет всем сертификатам (для разработки)
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                })
                
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                
                sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                
                hostnameVerifier { _, _ -> true }
            }
        }
        install(ContentNegotiation) {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
        }
    }
    
    // GigaChat API конфигурация
    private val baseUrl = "https://gigachat.devices.sberbank.ru/api/v1"
    private val authUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
    
    companion object {
        private const val TAG = "GigaChatRepository"
    }
    
    /**
     * Получает оценку рисков от GigaChat
     * @param answer Ответы пользователя на опросник
     * @return Оценка рисков с ID запроса
     */
    suspend fun assessRisk(answer: QuestionnaireAnswer): Result<Pair<GigaChatRiskAssessment, String>> {
        return try {
            Log.d(TAG, "=== Начала оценка рисков для пользователя ===")
            
            // 1. Получаем JWT токен
            Log.d(TAG, "1. Получение JWT токена...")
            val jwtToken = getJwtToken()
            Log.d(TAG, "JWT токен получен: ${jwtToken.take(20)}...")
            
            // 2. Формируем промпт для анализа
            Log.d(TAG, "2. Формирование промпта...")
            val prompt = buildRiskAssessmentPrompt(answer)
            Log.d(TAG, "Промпт сформирован, длина: ${prompt.length} символов")
            
            // 3. Отправляем запрос к GigaChat
            Log.d(TAG, "3. Отправка запроса к GigaChat API...")
            val chatResponse = sendChatRequest(jwtToken, prompt)
            Log.d(TAG, "Ответ от GigaChat получен, длина: ${chatResponse.length} символов")
            
            // 4. Парсим ответ в структуру оценки рисков
            Log.d(TAG, "4. Парсинг ответа...")
            val assessment = parseRiskAssessment(chatResponse)
            Log.d(TAG, "Ответ распаршен: riskScore=${assessment.riskScore}, recommendation=${assessment.recommendation}")
            
            // Генерируем ID запроса для трассировки
            val requestId = java.util.UUID.randomUUID().toString()
            Log.d(TAG, "=== Оценка рисков успешна. requestId=$requestId ===")
            
            Result.success(Pair(assessment, requestId))
        } catch (e: Exception) {
            Log.e(TAG, "=== ОШИБКА при оценке рисков: ${e.message} ===", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Получает JWT токен для доступа к GigaChat API
     */
    private suspend fun getJwtToken(): String {
        return try {
            Log.d(TAG, "GigaChat_AUTH_KEY длина: ${BuildConfig.GIGACHAT_AUTH_KEY.length}")
            val authKey = BuildConfig.GIGACHAT_AUTH_KEY
            
            val response = client.post(authUrl) {
                header("RqUID", java.util.UUID.randomUUID().toString())
                header("Authorization", "Basic $authKey")
                header("Content-Type", "application/x-www-form-urlencoded")
                header("Accept", "application/json")
                setBody("scope=GIGACHAT_API_PERS")
            }
            
            Log.d(TAG, "Ответ OAuth: ${response.status}")
            val responseBody = response.body<String>()
            Log.d(TAG, "Тело ответа OAuth: $responseBody")
            
            // Парсим JSON с использованием DTO
            val json = Json { ignoreUnknownKeys = true }
            val token = try {
                val oauthResponse = json.decodeFromString<OAuthTokenResponse>(responseBody)
                oauthResponse.access_token
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка парсинга JSON: ${e.message}")
                null
            }
            
            token ?: throw IllegalStateException("Не удалось получить JWT токен. Ответ: $responseBody")
            
            Log.d(TAG, "JWT токен успешно получен")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка аутентификации в GigaChat: ${e.message}", e)
            throw IllegalStateException("Ошибка аутентификации в GigaChat: ${e.message}", e)
        }
    }
    
    /**
     * Формирует структурированный промпт для анализа рисков
     */
    private fun buildRiskAssessmentPrompt(answer: QuestionnaireAnswer): String {
        return """
        Ты — эксперт по пристройству животных из приютов. Проанализируй ответы кандидата 
        и оцени риски передачи питомца этому человеку.

        **ДАННЫЕ КАНДИДАТА:**
        
        1. Основная информация:
           - Имя: ${answer.q1_full_name}
           - Возраст: ${answer.q1_age} лет
           - Город: ${answer.q1_city}
           - Профессия: ${answer.q1_occupation}
           - Контакт: ${answer.q1_contact_method}

        2. Жилищные условия:
           - Тип жилья: ${answer.q2_housing_type}
           - Животные разрешены: ${answer.q2_pets_allowed}
           - Живёт с: ${answer.q2_living_with.joinToString()}
           - Согласие семьи: ${answer.q2_family_consent}
           - Дети: ${answer.q2_has_children}${if (answer.q2_has_children == true) ", возраст: ${answer.q2_children_ages}" else ""}
           - Другие животные: ${answer.q2_has_other_pets}${if (answer.q2_has_other_pets == true) ", типы: ${answer.q2_other_pets_types.joinToString()}" else ""}
           - Часов в день один: ${answer.q2_hours_alone}
           - Кто ухаживает в отсутствие: ${answer.q2_caregiver}

        3. Опыт с животными:
           - Были ли питомцы раньше: ${answer.q3_had_pets_before}
           - Что с ними сейчас: ${answer.q3_what_happened}
           - Опыт с собаками: ${answer.q3_dog_experience}
           - Опыт с кошками: ${answer.q3_cat_experience}
           - Опыт с особенными животными: ${answer.q3_special_needs_experience}
           - Почему сейчас: ${answer.q3_why_now}

        4. Ответственность и готовность:
           - Понимает требования: ${answer.q4_understand_requirements}
           - Готов ко времени: ${answer.q4_understand_time}
           - Готов к вниманию: ${answer.q4_understand_attention}
           - Готов к обучению: ${answer.q4_understand_training}
           - Готов к ветпомощи: ${answer.q4_understand_vet_care}
           - Потребности: ${answer.understandsNeeds.joinToString()}
           - План при порче мебели: ${answer.q4_furniture_damage_plan}
           - План при шуме: ${answer.q4_noise_plan}
           - План при пугливости: ${answer.q4_shy_pet_plan}
           - План при долгой адаптации: ${answer.q4_long_adaptation_plan}
           - Готов к воспитанию: ${answer.q4_ready_education}
           - Жизненные изменения: ${answer.q4_life_changes_plan}
           - Препятствия: ${answer.q4_obstacles_next_year}

        5. Безопасность:
           - Меры безопасности: ${answer.q5_safety_measures.ifEmpty { listOf("—") }.joinToString()}
           - Готов к стерилизации: ${answer.q5_ready_neuter}
           - Готов к рекомендациям: ${answer.q5_ready_recommendations}
           - Готов к адреснику: ${answer.q5_ready_tracker}
           - Готов поддерживать связь: ${answer.q5_ready_keep_contact}

        6. Эмоциональная часть:
           - Что значит ответственный хозяин: ${answer.q6_responsible_owner_meaning}
           - Видение жизни с питомцем: ${answer.q6_life_with_pet_vision}
           - Почему хороший хозяин: ${answer.q6_why_good_owner}

        **ЗАДАЧА:**
        Оцени риски по шкале от 0 до 100 и верни ответ ТОЛЬКО в формате JSON:

        {
            "overallRisk": "LOW" | "MEDIUM" | "HIGH" | "VERY_HIGH",
            "riskScore": число от 0 до 100,
            "riskFactors": [
                {"category": "категория", "severity": "LOW" | "MEDIUM" | "HIGH", "description": "описание", "suggestion": "предложение"}
            ],
            "positiveFactors": ["положительный фактор 1", "положительный фактор 2"],
            "recommendations": ["рекомендация 1", "рекомендация 2"],
            "detailedAnalysis": "развёрнутый анализ на русском языке",
            "recommendation": "APPROVE" | "APPROVE_WITH_CONDITIONS" | "REVIEW_REQUIRED" | "REJECT"
        }

        Критерии оценки:
        - LOW (0-25): Отличные условия, большой опыт, полная готовность
        - MEDIUM (26-50): Хорошие условия, есть небольшие риски
        - HIGH (51-75): Значительные риски, требуется проверка
        - VERY_HIGH (76-100): Критические риски, не рекомендуется

        **ВАЖНО: Обратить особое внимание на:**
        
        1. **Занятость кандидата:**
           - Если кандидат проводит на работе более 8 часов в день — это ВЫСОКИЙ РИСК
           - Если кандидат работает в ПАО Сбербанк или другой крупной организации с ненормированным днем — риск повышен
           - Оцените, есть ли у кандидата время на уход, прогулки, внимание к питомцу
           - Если в ответах есть противоречия (например, "готов к времени", но работает 10+ часов) — укажите в рисках

        2. **Опыт с животными:**
           - Если кандидат НЕ ИМЕЕТ опыта с животными (q3_had_pets_before = false/null) — это СРЕДНИЙ/ВЫСОКИЙ РИСК
           - Оцените, понимает ли кандидат специфику ухода за конкретным видом животного
           - Если кандидат впервые берёт питомца — требуется дополнительная проверка и обучение
           - Отсутствие опыта с собаками/кошками при желании взять собаку/кошку — риск

        Факторы риска:
        - Нет согласия семьи
        - Нет разрешения на животных в жилье
        - Более 8 часов в день один
        - Нет опыта с животными
        - Не понимает ответственность
        - Нет мер безопасности
        - Непредсказуемые жизненные обстоятельства
        - Высокая занятость на работе (более 8 часов)
        - Работа в организации с ненормированным днем

        Положительные факторы:
        - Опыт с животными
        - Готовность к расходам и ветпомощи
        - Меры безопасности установлены
        - Понимание ответственности
        - Поддержка семьи
        - Гибкий график работы или возможность брать питомца на работу
        - Наличие свободного времени для ухода
        """.trimIndent()
    }
    
    /**
     * Отправляет запрос к GigaChat API
     */
    private suspend fun sendChatRequest(jwtToken: String, prompt: String): String {
        Log.d(TAG, "Отправка запроса к GigaChat API...")
        
        // Пробуем разные модели по очереди
        val modelsToTry = listOf("GigaChat:2.0", "GigaChat", "GigaChat-Plus", "GigaChat-Pro")
        val json = Json { isLenient = true; ignoreUnknownKeys = true }
        
        for (model in modelsToTry) {
            Log.d(TAG, "Попытка с моделью: $model")
            
            val bodyString = json.encodeToString(GigaChatRequest.serializer(), GigaChatRequest(
                model = model,
                messages = listOf(Message(role = "user", content = prompt)),
                temperature = 0.3,
                max_tokens = 2000
            ))
            
            val response = client.post("$baseUrl/chat/completions") {
                header("Authorization", "Bearer $jwtToken")
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("X-Request-ID", UUID.randomUUID().toString())
                setBody(bodyString)
            }
            
            Log.d(TAG, "Статус ответа для $model: ${response.status}")
            
            if (response.status.value == 200) {
                val responseBody = response.body<String>()
                Log.d(TAG, "Успех! Тело ответа: $responseBody")
                
                // Парсим ответ вручную
                val element = json.parseToJsonElement(responseBody)
                val choices = element.jsonObject["choices"]?.jsonArray
                val message = choices?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
                val content = message?.get("content")?.jsonPrimitive?.content
                
                if (content != null) {
                    Log.d(TAG, "Контент успешно извлечён из модели $model")
                    return content
                } else {
                    Log.e(TAG, "Не удалось извлечь content из ответа")
                }
            } else {
                val errorBody = response.body<String>()
                Log.w(TAG, "Модель $model не подошла: ${response.status} - $errorBody")
            }
        }
        
        throw IllegalStateException("Ни одна из моделей не работала. Проверьте доступ к GigaChat API.")
    }
    
    private suspend fun getAvailableModels(jwtToken: String): List<String> {
        return try {
            val response = client.get("$baseUrl/models") {
                header("Authorization", "Bearer $jwtToken")
                header("Accept", "application/json")
            }
            
            if (response.status.value == 200) {
                val responseBody = response.body<String>()
                Log.d(TAG, "Ответ /models: $responseBody")
                
                // Ручной парсинг JSON через JsonPrimitive
                val json = Json { isLenient = true; ignoreUnknownKeys = true }
                val element = json.parseToJsonElement(responseBody)
                val data = element.jsonObject["data"]?.jsonArray
                
                data?.mapNotNull { jsonElement ->
                    jsonElement.jsonObject["id"]?.jsonPrimitive?.content
                } ?: emptyList()
            } else {
                Log.w(TAG, "Не удалось получить список моделей: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении списка моделей", e)
            emptyList()
        }
    }
    
    /**
     * Парсит ответ от GigaChat в структуру оценки рисков
     */
    private fun parseRiskAssessment(response: String): GigaChatRiskAssessment {
        // Извлекаем JSON из ответа (на случай если он обернут в текст)
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        
        if (jsonStart == -1 || jsonEnd == -1) {
            throw IllegalStateException("Не удалось найти JSON в ответе: $response")
        }
        
        val jsonStr = response.substring(jsonStart, jsonEnd + 1)
        
        return try {
            Json.decodeFromString<GigaChatRiskAssessment>(jsonStr)
        } catch (e: Exception) {
            // Если парсинг не удался, создаём базовую оценку
            createFallbackAssessment(response)
        }
    }
    
    /**
     * Создаёт оценку рисков при ошибке парсинга
     */
    private fun createFallbackAssessment(rawResponse: String): GigaChatRiskAssessment {
        return GigaChatRiskAssessment(
            overallRisk = RiskLevel.MEDIUM,
            riskScore = 50,
            riskFactors = emptyList(),
            positiveFactors = listOf("Кандидат прошёл опросник"),
            recommendations = listOf("Провести личную встречу для уточнения деталей"),
            detailedAnalysis = rawResponse.take(500),
            recommendation = Recommendation.REVIEW_REQUIRED
        )
    }
}
