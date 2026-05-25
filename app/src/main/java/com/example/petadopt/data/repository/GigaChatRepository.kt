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
    val model: String = "GigaChat-2-Max",
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
            
            // Проверяем, не пустой ли ключ
            if (authKey.isEmpty()) {
                throw IllegalStateException("GIGACHAT_AUTH_KEY пустой! Проверьте .env файл")
            }
            
            val response = client.post(authUrl) {
                header("RqUID", java.util.UUID.randomUUID().toString())
                header("Authorization", "Basic $authKey")
                header("Content-Type", "application/x-www-form-urlencoded")
                header("Accept", "application/json")
                header("X-Client-App", "PetAdopt")
                setBody("scope=${BuildConfig.GIGACHAT_SCOPE}")
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
        
        1. Основная информация (ПРИОРИТЕТ: средний):
           - Имя: ${answer.q1_full_name}
           - Возраст: ${answer.q1_age} лет ${if ((answer.q1_age ?: 0) < 18 || (answer.q1_age ?: 99) > 90) "[РИСК: возраст вне оптимального диапазона 18-90]" else ""}
           - Город: ${answer.q1_city}
           - Профессия: ${answer.q1_occupation} ${if (answer.q1_occupation?.contains("Сбербанк", ignoreCase = true) == true) "[ПРИОРИТЕТ: проверить график работы]" else ""}
           - Телефон: ${answer.q1_phone}
           - Email: ${answer.q1_email}

        2. Жилищные условия (ПРИОРИТЕТ: ВЫСОКИЙ — критические факторы):
           - Тип жилья: ${answer.q2_housing_type} ${if (answer.q2_housing_type == "Съёмное жильё") "[РИСК: нестабильность]" else ""}
           - Животные разрешены: ${if (answer.q2_pets_allowed == true) "Да" else if (answer.q2_pets_allowed == false) "Нет [КРИТИЧЕСКИЙ РИСК: нет разрешения]" else "Не указано"}
           - Живёт с: ${answer.q2_living_with.joinToString()} ${if (answer.q2_living_with.isEmpty()) "[РИСК: живёт один — нет поддержки]" else ""}
           - Согласие семьи: ${if (answer.q2_family_consent == true) "Да" else if (answer.q2_family_consent == false) "Нет [КРИТИЧЕСКИЙ РИСК: нет согласия семьи]" else "Не указано"}
           - Дети: ${if (answer.q2_has_children == true) "Да, возраст: ${answer.q2_children_ages}" else if (answer.q2_has_children == false) "Нет" else "Не указано"}
           - Другие животные: ${if (answer.q2_has_other_pets == true) "Да, типы: ${answer.q2_other_pets_types.joinToString()}" else if (answer.q2_has_other_pets == false) "Нет" else "Не указано"}
           - Часов в день питомец один: ${answer.q2_hours_alone}${getHoursAloneRiskHint(answer.q2_hours_alone)}
           - Кто ухаживает в отсутствие: ${answer.q2_caregiver} ${if (answer.q2_caregiver.isNullOrBlank() || answer.q2_caregiver.length < 3) "[РИСК: не указан уход]" else ""}

        3. Опыт с животными (ПРИОРИТЕТ: ВЫСОКИЙ):
           - Были ли питомцы раньше: ${if (answer.q3_had_pets_before == true) "Да" else if (answer.q3_had_pets_before == false) "Нет [РИСК: нет опыта]" else "Не указано"}
           - Что с ними сейчас: ${answer.q3_what_happened} ${if (answer.q3_what_happened.isNotEmpty() && (answer.q3_what_happened.contains("выбросил", ignoreCase = true) || answer.q3_what_happened.contains("отдал", ignoreCase = true))) "[КРИТИЧЕСКИЙ РИСК: негативная история]" else ""}
           - Опыт с собаками: ${if (answer.q3_dog_experience == true) "Да" else if (answer.q3_dog_experience == false) "Нет" else "Не указано"}
           - Опыт с кошками: ${if (answer.q3_cat_experience == true) "Да" else if (answer.q3_cat_experience == false) "Нет" else "Не указано"}
           - Опыт с особенными животными: ${if (answer.q3_special_needs_experience == true) "Да" else if (answer.q3_special_needs_experience == false) "Нет" else "Не указано"}
           - Почему сейчас: ${answer.q3_why_now}

        4. Ответственность и готовность (ПРИОРИТЕТ: ВЫСОКИЙ):
           - Понимает требования: ${if (answer.q4_understand_requirements) "Да" else "Нет [КРИТИЧЕСКИЙ РИСК: не понимает ответственность]"}
           - Готов ко времени: ${if (answer.q4_understand_time) "Да" else "Нет"}
           - Готов к вниманию: ${if (answer.q4_understand_attention) "Да" else "Нет"}
           - Готов к обучению: ${if (answer.q4_understand_training) "Да" else "Нет"}
           - Готов к ветпомощи: ${if (answer.q4_understand_vet_care) "Да" else "Нет"}
           - Потребности: ${answer.understandsNeeds.joinToString()} ${if (answer.understandsNeeds.isEmpty()) "[РИСК: не понимает потребностей]" else ""}
           - План при порче мебели: ${answer.q4_furniture_damage_plan} ${if (answer.q4_furniture_damage_plan.isNullOrBlank() || answer.q4_furniture_damage_plan.length < 5) "[РИСК: нет плана]" else ""}
           - План при шуме: ${answer.q4_noise_plan} ${if (answer.q4_noise_plan.isNullOrBlank() || answer.q4_noise_plan.length < 5) "[РИСК: нет плана]" else ""}
           - План при пугливости: ${answer.q4_shy_pet_plan} ${if (answer.q4_shy_pet_plan.isNullOrBlank() || answer.q4_shy_pet_plan.length < 5) "[РИСК: нет плана]" else ""}
           - План при долгой адаптации: ${answer.q4_long_adaptation_plan} ${if (answer.q4_long_adaptation_plan.isNullOrBlank() || answer.q4_long_adaptation_plan.length < 5) "[РИСК: нет плана]" else ""}
           - Готов к воспитанию: ${if (answer.q4_ready_education) "Да" else "Нет [РИСК: не готов к воспитанию]"}
           - Жизненные изменения: ${answer.q4_life_changes_plan}
           - Препятствия: ${answer.q4_obstacles_next_year}

        5. Безопасность (ПРИОРИТЕТ: ВЫСОКИЙ):
           - Меры безопасности: ${answer.q5_safety_measures.ifEmpty { listOf("—") }.joinToString()} ${if (answer.q5_safety_measures.isEmpty()) "[РИСК: нет мер безопасности]" else ""}
           - Готов к стерилизации: ${if (answer.q5_ready_neuter) "Да" else "Нет [РИСК: не готов к стерилизации]"}
           - Готов к рекомендациям: ${if (answer.q5_ready_recommendations) "Да" else "Нет [РИСК: не готов следовать рекомендациям]"}
           - Готов к адреснику: ${if (answer.q5_ready_tracker) "Да" else "Нет"}
           - Готов поддерживать связь: ${if (answer.q5_ready_keep_contact) "Да" else "Нет [РИСК: не готов к обратной связи]"}

        6. Эмоциональная часть (ПРИОРИТЕТ: средний — качественные ответы):
           - Что значит ответственный хозяин: ${answer.q6_responsible_owner_meaning}
           - Видение жизни с питомцем: ${answer.q6_life_with_pet_vision}
           - Почему хороший хозяин: ${answer.q6_why_good_owner}

        7. Желаемые виды животных (ПРИОРИТЕТ: низкий — для рекомендации):
           - Какие питомцы желаемы: ${answer.q7_desired_pets.joinToString { it.ifEmpty { "не указано" } }}

        **ПРАВИЛА ОЦЕНКИ РИСКОВ:**
        
        1. **Время питомца в одиночестве (КРИТИЧЕСКИ ВАЖНО):**
           - 0-4 часов в день — ОТЛИЧНО (снижает риск на 15-20 баллов)
           - 5-6 часов в день — ХОРОШО (снижает риск на 5-10 баллов)
           - 7-8 часов в день — ДОПУСТИМО (без изменения)
           - 9-10 часов в день — ПОВЫШЕННЫЙ РИСК (+10-15 баллов к риску)
           - 11+ часов в день — ВЫСОКИЙ РИСК (+20-30 баллов к риску, питомец будет страдать)
           - Чем МЕНЬШЕ времени питомец проводит один, тем ЛУЧШЕ для его психики и здоровья

        2. **Приоритеты обработки полей (от высокого к низкому):**
           
           ПРИОРИТЕТ ВЫСОКИЙ (критические факторы):
           - q2_pets_allowed: без разрешения на животных — автоматический REJECT
           - q2_family_consent: без согласия семьи — высокий риск
           - q2_hours_alone: >8 часов — высокий риск (см. правило выше)
           - q4_understand_requirements: непонимание ответственности — высокий риск
           - q5_safety_measures: отсутствие мер безопасности — средний/высокий риск
           - q5_ready_neuter: отказ от стерилизации — высокий риск
           - q3_had_pets_before: отсутствие опыта — средний риск
           - q3_what_happened: негативная история (выбросил/отдал) — критический риск
           
           ПРИОРИТЕТ СРЕДНИЙ:
           - q2_housing_type: съёмное жильё — небольшой риск нестабильности
           - q2_caregiver: отсутствие плана ухода — средний риск
           - q4_*_plan: отсутствие планов для сложных ситуаций — средний риск
           - q4_ready_education: неготовность к воспитанию — средний риск
           - q5_ready_keep_contact: неготовность к обратной связи — средний риск
           - q1_age: возраст <18 или >90 — небольшой риск
           
           ПРИОРИТЕТ НИЗКИЙ:
           - q6_*: эмоциональные ответы — для qualitative оценки
           - q7_desired_pets: для рекомендации подходящих питомцев

        3. **Занятость кандидата:**
           - Если кандидат указывает профессию в крупной организации (Сбербанк и др.) — проверить на ненормированный день
           - Если кандидат живёт один (q2_living_with пустой) — нет поддержки в уходе

        4. **Опыт с животными:**
           - Отсутствие опыта (q3_had_pets_before = "Нет") — средний риск, но не критичный
           - Негативная история (выбросил, отдал, умерли по вине) — КРИТИЧЕСКИЙ РИСК
           - Отсутствие опыта с конкретным видом (собака/кошка) при желании взять его — средний риск

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
        - LOW (0-25): Отличные условия, большой опыт, полная готовность, питомец не будет долго один
        - MEDIUM (26-50): Хорошие условия, есть небольшие риски (например, 7-8 часов один)
        - HIGH (51-75): Значительные риски (9-10 часов один, нет опыта, съёмное жильё)
        - VERY_HIGH (76-100): Критические риски (11+ часов один, нет разрешения, негативная история)

        **Факторы риска (с учётом времени в одиночестве):**
        - Нет согласия семьи
        - Нет разрешения на животных в жилье
        - 9+ часов в день один (чем больше, тем хуже)
        - Нет опыта с животными
        - Не понимает ответственность
        - Нет мер безопасности
        - Непредсказуемые жизненные обстоятельства
        - Высокая занятость на работе (9+ часов)
        - Негативная история с предыдущими питомцами
        
        **Положительные факторы:**
        - Опыт с животными (особенно положительный)
        - Готовность к расходам и ветпомощи
        - Меры безопасности установлены
        - Понимание ответственности
        - Поддержка семьи
        - 0-6 часов в день один (идеально для питомца)
        - Гибкий график работы или возможность брать питомца на работу
        - Наличие плана на сложные ситуации
        """.trimIndent()
    }
    
    /**
     * Генерирует подсказку о риске на основе часов в одиночестве
     */
    private fun getHoursAloneRiskHint(hours: Int?): String {
        if (hours == null) return "[РИСК: не указано]"
        return when {
            hours <= 4 -> "[ОТЛИЧНО: минимальное время в одиночестве]"
            hours <= 6 -> "[ХОРОШО: приемлемое время]"
            hours <= 8 -> "[ДОПУСТИМО: стандартное рабочее время]"
            hours <= 10 -> "[ПОВЫШЕННЫЙ РИСК: питомец будет проводить много времени один]"
            else -> "[ВЫСОКИЙ РИСК: 11+ часов в день — питомец будет страдать]"
        }
    }
    
    /**
     * Отправляет запрос к GigaChat API
     */
    private suspend fun sendChatRequest(jwtToken: String, prompt: String): String {
        Log.d(TAG, "Отправка запроса к GigaChat API...")
        
        // Пробуем модели в приоритетном порядке: сначала Max/Pro, потом остальные
        val modelsToTry = listOf("GigaChat-2-Max", "GigaChat-2-Pro", "GigaChat-2", "GigaChat-Pro")
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
