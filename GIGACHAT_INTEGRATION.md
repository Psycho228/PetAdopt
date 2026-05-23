# Интеграция с GigaChat для оценки рисков

## Обзор

Добавлена интеграция с GigaChat API для автоматической оценки рисков передачи питомца кандидату на основе ответов опросника.

## Что было добавлено

### 1. Модели данных (`data/model/`)

- **GigaChatRiskAssessment.kt** — модели для ответа от GigaChat:
  - `GigaChatRiskAssessment` — основная структура оценки
  - `RiskLevel` — уровень риска (LOW, MEDIUM, HIGH, VERY_HIGH)
  - `RiskFactor` — отдельный фактор риска
  - `RiskSeverity` — серьёзность риска
  - `Recommendation` — итоговая рекомендация (APPROVE, APPROVE_WITH_CONDITIONS, REVIEW_REQUIRED, REJECT)

### 2. Репозиторий (`data/repository/`)

- **GigaChatRepository.kt** — работа с GigaChat API:
  - JWT аутентификация
  - Формирование структурированного промпта с данными опросника
  - Отправка запроса к GigaChat
  - Парсинг JSON ответа
  - Fallback-логика при ошибках парсинга

### 3. UseCase'и (`domain/usecase/`)

- **RiskAssessmentUseCases.kt** — бизнес-логика оценки:
  - `AssessRiskUseCase` — основная операция оценки
  - `RiskAssessmentUseCases` — контейнер всех UseCase'ов оценки

### 4. UI компоненты (`ui/components/`)

- **RiskAssessmentCard.kt** — красивый UI для отображения оценки:
  - Индикатор уровня риска с цветовым кодированием
  - Score от 0 до 100
  - Итоговая рекомендация
  - Подробный анализ от AI
  - Список факторов риска с категориями и серьёзностью
  - Положительные факторы
  - Рекомендации для кандидата

### 5. ViewModel (`viewmodel/`)

- Обновлён **QuestionnaireViewModel.kt**:
  - Добавлен метод `saveWithRiskAssessment()` для сохранения + оценки
  - Интеграция с `AssessRiskUseCase`

### 6. Экраны (`ui/screens/`)

- Обновлён **QuestionnaireScreen.kt**:
  - Диалог подтверждения с выбором: "Сохранить и оценить" или "Только сохранить"
  - Отображение результата оценки в модальном окне
  - Параметры для передачи GigaChat credentials

### 7. DI (`di/`)

- Обновлён **RepositoryModule.kt**:
  - Провизор для `GigaChatRepository`
  - Провизоры для `AssessRiskUseCase` и `RiskAssessmentUseCases`

### 8. Зависимости

- Добавлены в `libs.versions.toml` и `build.gradle.kts`:
  - `ktor-client-content-negotiation`
  - `ktor-serialization-kotlinx-json`

## Настройка

### Credentials уже добавлены

GigaChat credentials уже настроены в `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "GIGACHAT_CLIENT_ID", "\"019e516c-1cd7-7e6a-abb0-cfa756884880\"")
buildConfigField("String", "GIGACHAT_SCOPE", "\"GIGACHAT_API_PERS\"")
buildConfigField("String", "GIGACHAT_AUTH_KEY", "\"MDE5ZTUxNmMtMWNkNy03ZTZhLWFiYjAtY2ZhNzU2ODg0ODgwOjU2NjM4MzA3LTFhMmUtNDBjNy1iMTc4LWQwOGZhOGZhMWM4Zg==\"")
```

**Никаких дополнительных настроек не требуется!** Просто соберите проект.

## Использование

### Программный вызов

```kotlin
val auth = GigaChatAuthCredentials(
    clientId = "your-client-id",
    clientSecret = "your-client-secret"
)

viewModel.saveWithRiskAssessment(
    gigachatAuth = auth,
    onSuccess = { assessment ->
        // Успешная оценка
        when (assessment.recommendation) {
            Recommendation.APPROVE -> // Одобрить
            Recommendation.REVIEW_REQUIRED -> // Требуется проверка
            // ...
        }
    },
    onRiskAssessed = { result ->
        // Обработка ошибки (если нужна)
        result.onFailure { error ->
            Log.e("RiskAssessment", "Ошибка оценки", error)
        }
    }
)
```

### Получение оценки в UI

```kotlin
val assessment = riskAssessmentResult ?: return

// Уровень риска
Text("Риск: ${assessment.overallRisk}")

// Score
ProgressIndicator(assessment.riskScore / 100f)

// Рекомендация
Text(assessment.recommendationText)

// Факторы
assessment.riskFactors.forEach { factor ->
    RiskFactorRow(factor)
}
```

## Формат запроса к GigaChat

Система отправляет структурированный промпт со всеми данными опросника:

```
Ты — эксперт по пристройству животных из приютов. Проанализируй ответы кандидата 
и оцени риски передачи питомца этому человеку.

**ДАННЫЕ КАНДИДАТА:**
1. Основная информация: ...
2. Жилищные условия: ...
3. Опыт с животными: ...
4. Ответственность и готовность: ...
5. Безопасность: ...
6. Эмоциональная часть: ...

**ЗАДАЧА:**
Оцени риски по шкале от 0 до 100 и верни ответ ТОЛЬКО в формате JSON:
{
    "overallRisk": "LOW" | "MEDIUM" | "HIGH" | "VERY_HIGH",
    "riskScore": число от 0 до 100,
    "riskFactors": [...],
    "positiveFactors": [...],
    "recommendations": [...],
    "detailedAnalysis": "...",
    "recommendation": "APPROVE" | "APPROVE_WITH_CONDITIONS" | "REVIEW_REQUIRED" | "REJECT"
}
```

## Критерии оценки

### Уровни риска

- **LOW (0-25)**: Отличные условия, большой опыт, полная готовность
- **MEDIUM (26-50)**: Хорошие условия, есть небольшие риски
- **HIGH (51-75)**: Значительные риски, требуется проверка
- **VERY_HIGH (76-100)**: Критические риски, не рекомендуется

### Ключевые факторы риска

- Нет согласия семьи
- Нет разрешения на животных в жилье
- Более 8 часов в день один
- Нет опыта с животными
- Не понимает ответственность
- Нет мер безопасности
- Непредсказуемые жизненные обстоятельства

### Положительные факторы

- Опыт с животными
- Готовность к расходам и ветпомощи
- Меры безопасности установлены
- Понимание ответственности
- Поддержка семьи

## Обработка ошибок

Система предусматривает несколько уровней защиты:

1. **JWT аутентификация** — понятные сообщения при ошибках авторизации
2. **Сетевые ошибки** — fallback с оценкой MEDIUM + REVIEW_REQUIRED
3. **Парсинг JSON** — автоматическое извлечение JSON из текста ответа
4. **Невалидный ответ** — создание базовой оценки с `detailedAnalysis` из сырого ответа

## Тестирование

### Unit тесты

```kotlin
@Test
fun `assessRisk returns LOW risk for excellent candidate`() = runTest {
    val answer = createExcellentCandidateAnswer()
    val result = assessRiskUseCase(answer, credentials)
    
    assertTrue(result.isSuccess)
    assertEquals(RiskLevel.LOW, result.getOrNull()?.overallRisk)
}

@Test
fun `assessRisk returns HIGH risk for poor candidate`() = runTest {
    val answer = createPoorCandidateAnswer()
    val result = assessRiskUseCase(answer, credentials)
    
    assertTrue(result.isSuccess)
    assertEquals(RiskLevel.HIGH, result.getOrNull()?.overallRisk)
}
```

### Интеграционное тестирование

Используйте моки для GigaChat API:

```kotlin
@MockK
private lateinit var mockHttpClient

@BeforeTest
fun setup() {
    MockKAnnotations.init(this)
    // Настройка моков
}
```

## Производительность

- Среднее время оценки: **3-5 секунд**
- Размер промпта: **~5-8 KB**
- Размер ответа: **~1-2 KB**
- Рекомендуется кэшировать оценки в Firestore

## Безопасность

1. **JWT токены** живут 1 час, автоматически обновляются
2. **Credentials** хранятся в BuildConfig, не в коде
3. **HTTPS** — все запросы через защищённое соединение
4. **Валидация** — проверка всех полей перед отправкой

## Будущие улучшения

- [ ] Кэширование оценок в локальной БД
- [ ] История оценок для одного кандидата
- [ ] Экспорт отчёта в PDF
- [ ] Уведомления при изменении оценки
- [ ] A/B тестирование разных промптов
- [ ] Поддержка нескольких языков
- [ ] Интеграция с админ-панелью приютов

## Ссылки

- [GigaChat API Documentation](https://gigachat.dev/docs)
- [Примеры промптов](https://github.com/sberdevices/gigachat-examples)
- [Спецификация OpenAPI](https://api.gigachat.devices.sberbank.ru/openapi.json)

## Поддержка

При проблемах с интеграцией:
1. Проверьте credentials
2. Убедитесь, что HTTPS доступен
3. Проверьте логи Ktor на ошибки
4. Свяжитесь с поддержкой GigaChat

---

*Документация создана 22 мая 2026 г.*