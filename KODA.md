# KODA.md — Справочник по проекту PetAdopt

## 📱 Обзор проекта

**PetAdopt** — мобильное приложение для пристройства питомцев из приютов. Реализовано с использованием современной Android-архитектуры (MVVM + Clean Architecture) и Supabase бэкенда.

### Основные функции
- **Свайп-механика** — поиск питомцев по принципу Tinder (лайк/дизлайк)
- **Заявки** — подача заявок на пристройство питомца с отслеживанием статуса
- **Опросник** — детальная анкета потенциального хозяина (7 разделов, 40+ вопросов) с Material 3 UI
- **Профиль** — управление личными данными, просмотр истории заявок и ответов на опросник
- **Совпадения** — список лайкнутых питомцев с возможностью удаления лайков
- **Админ-панель** — управление питомцами (добавление, редактирование) для приютов
- **Кабинет приюта** — статистика, управление питомцами, просмотр заявок
- **Управление заявками** — просмотр списка заявок на питомца, детальный экран с опросником и оценкой рисков, подтверждение/отклонение заявок
- **Чат** — обмен сообщениями между пользователем и приютом в контексте заявки (Android + Web)
- **Веб-панель приюта** — React + Vite + TailwindCSS приложение для управления питомцами и заявками
- **Supabase** — аутентификация, PostgreSQL база данных и Storage
- **S3 (reg.ru Cloud)** — загрузка фотографий питомцев с SigV4 подписью
- **GigaChat** — оценка рисков при подаче заявок

## 🛠 Стек технологий

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 1.9.24 |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt 2.51.1 (kapt) |
| Бэкенд | Supabase (PostgreSQL + Auth + Storage) |
| S3 Хранилище | AWS SDK for Kotlin v1.2.25 + SigV4 подпись |
| GigaChat | Ktor Client 2.3.10 + yandex-cloud SDK |
| Навигация | Navigation Compose 2.8.0 |
| Картинки | Coil 2.6.0 |
| Иконки | Material Icons Extended 1.6.8 |
| Сборка | Gradle 8.2 + AGP 8.2.2 |
| Мин SDK | 24 (Android 7.0) |
| Целевой SDK | 34 (Android 14) |

### Зависимости (libs.versions.toml)
- `aws.sdk.kotlin:s3` — S3 загрузка фото
- `supabase-kt` — Supabase клиент (Postgrest, Gotrue, Storage)
- `ktor-client-*` — HTTP клиент для GigaChat и Supabase
- `hilt-android` — Dependency Injection
- `lifecycle-viewmodel-compose` — ViewModel в Compose
- `kotlinx-serialization-json` — JSON сериализация

## 📁 Структура проекта (Clean Architecture)

```
app/src/main/java/com/example/petadopt/
├── data/
│   ├── model/              # Модели данных
│   │   ├── Application.kt
│   │   ├── ChatMessage.kt
│   │   ├── GigaChatRiskAssessment.kt
│   │   ├── Pet.kt
│   │   ├── QuestionnaireAnswer.kt
│   │   ├── QuestionnaireAnswerExtensions.kt
│   │   ├── RiskAssessmentRecord.kt
│   │   └── User.kt
│   └── repository/         # Репозитории
│       ├── AdminRepository.kt
│       ├── AuthRepository.kt
│       ├── ChatRepository.kt
│       ├── GigaChatRepository.kt
│       ├── PetRepository.kt
│       ├── QuestionnaireRepository.kt
│       ├── S3StorageRepository.kt
│       ├── StorageRepository.kt
│       ├── SupabaseAuthRepository.kt
│       ├── SupabasePetRepository.kt
│       ├── SupabaseQuestionnaireRepository.kt
│       └── SupabaseStorageRepository.kt
├── domain/
│   ├── model/              # Доменные модели
│   └── usecase/            # Бизнес-логика (UseCase'и)
│       ├── AdditionalPetUseCases.kt
│       ├── AdminUseCases.kt
│       ├── AuthUseCases.kt
│       ├── GetLikedPetsUseCase.kt
│       ├── PetUseCases.kt
│       ├── QuestionnaireUseCases.kt
│       ├── RiskAssessmentDataUseCases.kt
│       ├── RiskAssessmentUseCases.kt
│       └── StorageUseCases.kt
├── di/
│   ├── AppModule.kt        # Supabase/Firebase зависимости
│   └── RepositoryModule.kt # Провизоры репозиториев и UseCase'ов
├── navigation/
│   └── NavGraph.kt         # Настройка навигации и routes
├── ui/
│   ├── components/         # Переиспользуемые UI компоненты
│   │   ├── PetCard.kt
│   │   ├── PrimaryButton.kt
│   │   ├── RiskAssessmentCard.kt
│   │   ├── Screen.kt
│   │   └── SwipeCard.kt
│   ├── screens/            # Экраны приложения
│   │   ├── AccountScreen.kt
│   │   ├── AddEditPetScreen.kt
│   │   ├── AdminScreen.kt
│   │   ├── ApplicationDetailWithChatScreen.kt
│   │   ├── ApplicationScreen.kt
│   │   ├── ApplicationsScreen.kt
│   │   ├── AuthScreen.kt
│   │   ├── ChatScreen.kt
│   │   ├── DetailsScreen.kt
│   │   ├── EditProfileScreen.kt
│   │   ├── MatchesScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── PetApplicationDetailScreen.kt
│   │   ├── PetApplicationsScreen.kt
│   │   ├── QuestionnaireCompleteScreen.kt
│   │   ├── QuestionnaireScreen.kt
│   │   ├── ShelterScreen.kt
│   │   └── SwipeScreen.kt
│   ├── state/              # Состояния UI
│   │   └── MatchState.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/              # ViewModels для каждого экрана
│   ├── AccountViewModel.kt
│   ├── AdminViewModel.kt
│   ├── ApplicationDetailViewModel.kt
│   ├── ApplicationsViewModel.kt
│   ├── ApplicationViewModel.kt
│   ├── AuthViewModel.kt
│   ├── ChatViewModel.kt
│   ├── NavViewModel.kt
│   ├── QuestionnaireState.kt
│   ├── QuestionnaireViewModel.kt
│   └── SwipeViewModel.kt
└── util/                   # Утилиты и расширения
    ├── S3Config.kt
    ├── S3SigV4Signer.kt
    └── SupabaseConfig.kt
```

## 🚀 Сборка и запуск

### Предварительные требования
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34
- Настроенный Supabase проект
- **Настроенные секреты** (см. [SECURITY_SETUP.md](SECURITY_SETUP.md))

### Настройка секретов
1. Создайте файл `.env` в корне проекта:
   ```bash
   cp .env.example .env
   ```
2. Заполните `.env` своими ключами:
   ```env
   S3_ACCESS_KEY=your_access_key
   S3_SECRET_KEY=your_secret_key
   S3_BUCKET_NAME=pet-photos
   S3_ENDPOINT_URL=https://s3.regru.cloud
   
   GIGACHAT_CLIENT_ID=your_client_id
   GIGACHAT_SCOPE=GIGACHAT_API_PERS
   GIGACHAT_AUTH_KEY=your_auth_key
   
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your_anon_key
   ```

### Команды сборки (Windows PowerShell)
```powershell
# Сборка debug-версии
.\gradlew assembleDebug

# Сборка release-версии (с ProGuard)
.\gradlew assembleRelease

# Запуск тестов
.\gradlew testDebugUnitTest

# Очистка и пересборка
.\gradlew clean assembleDebug
```

> ⚠️ **Важно:** Перед первым запуском создайте `.env` файл на основе `.env.example`. Без ключей сборка покажет предупреждение, но продолжится.

## 🔧 Supabase конфигурация

### Таблицы
| Таблица | Описание | Колонки |
|---------|----------|---------|
| `users` | Профили пользователей | `id`, `email`, `name`, `role`, `phone`, `created_at` |
| `pets` | Данные питомцев | `id`, `shelter_id`, `name`, `age`, `type`, `gender`, `size`, `breed`, `color`, `weight`, `description`, `photo_url`, `additional_photos`, `traits`, `is_neutered`, `has_vaccination`, `is_active`, `created_at`, `updated_at` |
| `applications` | Заявки на пристройство | `id`, `user_id`, `user_name`, `user_email`, `pet_id`, `pet_name`, `message`, `contact_time`, `contact_days`, `status`, `created_at`, `updated_at` |
| `questionnaire_answers` | Ответы на опросник | `id`, `user_id`, все поля опросника (q1_*, q2_*, ...), `created_at`, `updated_at` |
| `likes` | Лайки пользователей | `id`, `user_id`, `pet_id`, `created_at` |
| `risk_assessments` | Оценки рисков от GigaChat | `id`, `user_id`, `application_id`, `assessment_data`, `created_at` |
| `chat_messages` | Сообщения чата | `id`, `application_id`, `sender_id`, `sender_role`, `message`, `is_read`, `status`, `created_at` |

### Роли пользователей
- `user` — обычный пользователь (ищет питомца)
- `admin` — администратор приюта (управляет всеми питомцами)
- `shelter` — сотрудник приюта (управляет своими питомцами)

### Статусы заявок
- `pending` — заявка на рассмотрении
- `processing` — заявка в работе (автоматически при открытии списка)
- `approved` — заявка одобрена
- `rejected` — заявка отклонена

### RLS политики
- `pets`: публичное чтение, редактирование только для `shelter`/`admin`
- `applications`: чтение только владельцу/приюту, запись для всех авторизованных
- `questionnaire_answers`: чтение/запись только владельцу
- `likes`: чтение/запись только владельцу
- `risk_assessments`: чтение только для приюта, запись для системы
- `chat_messages`: чтение/запись только участникам заявки (пользователь или приют)

## 📝 Опросник (7 разделов, 40+ вопросов)

### UI-реализация
- **Пошаговая навигация** — 7 секций с прогресс-баром
- **Анимации** — slide, fade, scale при переходе между шагами
- **Валидация полей**:
  - **Обязательность** — все поля провераются перед переходом на следующий шаг
  - **Числовые поля** — фильтрация ввода, только цифры (возраст: 18-120, часы: 0-24)
  - **Зависимые поля** — если выбрано "Да" (дети/животные), показывается дополнительное поле
  - **Визуальные ошибки** — подсветка полей, диалог с перечнем ошибок
- **Типы вопросов**:
  - `Text` — однострочные и многострочные поля ввода с клавиатурой под тип данных
  - `Dropdown` — выпадающий список (ExposedDropdownMenuBox)
  - `CheckboxGroup` — группа чекбоксов для множественного выбора
  - `YesNo` — кнопки Да/Нет с визуальной индикацией

### Раздел 1: Основная информация (6 вопросов)
- Как вас зовут? (Text, **обязательно**)
- Сколько вам лет? (Text, **обязательно**, только числа 18-120)
- В каком городе вы живёте? (Dropdown, **обязательно**)
- Чем вы занимаетесь? (Text, **обязательно**)
- Как с вами лучше связаться? (Text: телефон, **обязательно**)
- Ваш email? (Text: email, **обязательно**)

### Раздел 2: Жилищные условия (11 вопросов)
- Где вы живёте? (Dropdown: Квартира/Частный дом/Съёмное жильё/Другое, **обязательно**)
- Разрешены ли животные в вашем жилье? (YesNo, **обязательно**)
- С кем вы живёте? (CheckboxGroup: Один/Семья/Друзья/Другое, **обязательно**)
- Все ли члены семьи согласны? (YesNo, **обязательно**)
- Есть ли у вас дети? (YesNo, **обязательно**)
- Если да — какого возраста? (Text, **обязательно если есть дети**)
- Есть ли у вас другие животные? (YesNo, **обязательно**)
- Если да — то какие? (Text, **обязательно если есть другие животные**)
- Сколько часов в день питомец будет один? (Text, **обязательно**, только числа 0-24)
- Кто будет ухаживать в ваше отсутствие? (Text, **обязательно**)

### Раздел 3: Опыт с животными (6 вопросов)
- Были ли у вас раньше питомцы? (YesNo, **обязательно**)
- Что с ними сейчас? (Text, **обязательно если были**)
- Опыт ухода за собаками? (YesNo, **обязательно**)
- Опыт ухода за кошками? (YesNo, **обязательно**)
- Опыт с животными с особенностями? (YesNo, **обязательно**)
- Почему решили взять питомца именно сейчас? (Text, **обязательно**)

### Раздел 4: Ответственность и готовность (10 вопросов)
- Понимаете ли вы, что потребуется? (CheckboxGroup: Время/Внимание/Обучение/Ветпомощь, **минимум 1**)
- Готовы ли к расходам на? (CheckboxGroup: Корм/Ветеринар/Лекарства/Прививки/Груминг, **минимум 1**)
- Что будете делать, если питомец испортит мебель? (Text, **обязательно**)
- Что будете делать, если будет шуметь? (Text, **обязательно**)
- Что будете делать, если окажется пугливым? (Text, **обязательно**)
- Что будете делать при долгой адаптации? (Text, **обязательно**)
- Готовы ли к воспитанию и адаптации? (YesNo, **обязательно**)
- Что будете делать при изменении обстоятельств? (Text, **обязательно**)
- Есть ли препятствия в ближайший год? (Text, **обязательно**)

### Раздел 5: Безопасность (4 вопроса)
- Установлены ли меры безопасности? (CheckboxGroup: Сетки/Безопасные балконы/Ограждения, **минимум 1**)
- Готовы ли к стерилизации? (YesNo, **обязательно**)
- Готовы ли следовать рекомендациям? (YesNo, **обязательно**)
- Готовы ли установить адресник? (YesNo, **обязательно**)
- Готовы ли поддерживать связь после пристройства? (YesNo, **обязательно**)

### Раздел 6: Эмоциональная часть (3 вопроса)
- Что для вас значит "ответственный хозяин"? (Text, **обязательно**)
- Как представляете жизнь с питомцем? (Text, **обязательно**)
- Почему именно вы станете хорошим хозяином? (Text, **обязательно**)

### Раздел 7: Желаемые виды животных (1 вопрос)
- Какие питомцы вас интересуют? (CheckboxGroup: Собаки/Кошки/Птицы/Грызуны/Другое, **минимум 1**)

## 🎨 Дизайн-система

### Цветовая палитра
```kotlin
val Primary = Color(0xFF6C63FF)         // Фиолетовый (основной)
val PrimaryVariant = Color(0xFF5A52E0)  // Тёмно-фиолетовый
val Background = Color(0xFFF8F9FE)      // Светло-серый фон
val Card = Color.White                  // Карточки
val TextPrimary = Color(0xFF1C1C1C)     // Основной текст
val TextSecondary = Color(0xFF6E6E6E)   // Вторичный текст
val Like = Color(0xFF4CAF50)            // Зелёный (лайк)
val Dislike = Color(0xFFF44336)         // Красный (дизлайк)
val SurfaceLight = Color(0xFFF0F0F5)    // Светлая поверхность
```

### UI компоненты
| Компонент | Описание |
|-----------|----------|
| `PrimaryButton` | Кнопка на всю ширину, фиолетовый фон, белый текст |
| `Screen` | Контейнер экрана с отступами и фоном |
| `SwipeCard` | Карточка с жестом свайпа (порог 320px) |
| `PetCard` | Карточка питомца с изображением и тегами |
| `RiskAssessmentCard` | Карточка оценки рисков от GigaChat |
| `QuestionCard` | Карточка вопроса с иконкой и скруглёнными углами (16dp) |
| `StatTile` | Карточка статистики с иконкой, значением и цветовым акцентом |
| `StatusFilterRow` | Горизонтальная полоса FilterChip для фильтрации по статусам |
| `DetailedRiskAssessment` | Детальный блок оценки рисков с градиентной шапкой, прогресс-баром и секциями |
| `ExposedDropdownMenu` | Выпадающий список Material 3 |
| `CheckboxGroup` | Группа чекбоксов для множественного выбора |
| `StatusBanner` | Банер статуса заявки с иконкой и цветовым фоном |
| `FactorSection` | Секция факторов риска (положительных/отрицательных) |
| `QuestionnaireExpandable` | Сворачиваемый раздел опросника с анимированным раскрытием |

### Анимации опросника
- Переходы между шагами: `slideInHorizontally` + `fadeIn` / `slideOutHorizontally` + `fadeOut`
- Появление вопросов: `slideInVertically` с spring-анимацией
- Прогресс-бар: `AnimatedVisibility` с `scaleIn` + `fadeIn`

## 🔧 Архитектурные паттерны

### Репозитории
```kotlin
@Singleton
class PetRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getPets(): List<Pet> { ... }
    suspend fun submitApplication(application: Application) { ... }
}
```
- Внедрение через `@Inject constructor`
- Все методы `suspend` с использованием Supabase Kotlin client
- Обработка ошибок через `try-catch` с выбрасыванием исключений

### UseCase'и
```kotlin
@Singleton
class GetPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(): List<Pet> {
        return repository.getPets()
    }
}
```
- Оператор `invoke` для прямого вызова
- Один UseCase = одна операция
- Внедрение репозиториев через конструктор

### ViewModel'и
```kotlin
@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val likePetUseCase: LikePetUseCase
) : ViewModel() {
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()
}
```
- `@HiltViewModel` для внедрения зависимостей
- `StateFlow` для состояния UI
- `viewModelScope.launch` для асинхронных операций
- Обработка ошибок через `try-catch`

### Навигация (routes)
```
auth                  — экран авторизации
loading               — экран загрузки (проверка сессии)
onboarding            — онбординг
questionnaire         — опросник (7 разделов)
swipe                 — свайп-экран
details/{petId}       — детали питомца
application/{petId}/{petName} — подача заявки
matches               — список лайкнутых питомцев
account               — личный кабинет
edit_profile          — редактирование профиля
applications          — мои заявки
shelter               — кабинет приюта
admin/addPet          — добавление питомца
admin/editPet/{petId} — редактирование питомца
admin/applications/{petId}/{petName} — заявки на питомца
admin/application/detail/{applicationId}/... — детальная заявка
application_chat/{applicationId} — детали заявки + чат (пользователь)
chat/{applicationId} — экран чата (только чат)
```

## 📌 Правила разработки

### Именование
- **Классы**: `PascalCase` (`UserRepository`, `GetPetsUseCase`)
- **Функции/переменные**: `camelCase` (`loadUserData`, `_currentIndex`)
- **Константы**: `UPPER_SNAKE_CASE` (`COLLECTION_PETS`)
- **StateFlow**: `_variable` (приватный), `variable` (публичный)

### Код-стили
1. Все отступы кратны 4 (4dp, 8dp, 12dp, 16dp...)
2. Используйте `alias(libs.*)` для версий из `libs.versions.toml`
3. Обработка ошибок: `try-catch` в репозиториях, отображение в UI через Snackbar/AlertDialog
4. Асинхронность: только `viewModelScope.launch` в ViewModel'ях
5. DI: только через `@Inject` и `@Provides`, избегайте ручного создания зависимостей
6. Иконки: использовать `Icons.Default.*` или `Icons.AutoMirrored.*` из Material Icons Extended

### Тестирование
```powershell
# Unit тесты
.\gradlew test

# Интеграционные тесты
.\gradlew connectedAndroidTest
```
- Unit тесты: `src/test/`
- Интеграционные тесты: `src/androidTest/`

## ☁️ S3 хранилище (reg.ru Cloud)

Приложение использует **S3 совместимое хранилище** для загрузки фотографий питомцев:

- **Провайдер**: reg.ru Cloud Object Storage
- **Бакет**: `pet-photos`
- **Доступ**: Path-style URL (`https://s3.regru.cloud/pet-photos/{key}`)
- **Авторизация**: AWS SigV4 подпись запросов (через `S3SigV4Signer.kt`)

### Настройка S3
1. Создайте бакет `pet-photos` в reg.ru Cloud
2. Настройте публичный доступ на чтение (`s3:GetObject`)
3. Создайте Access Key с правами `s3:PutObject`, `s3:DeleteObject`
4. Добавьте ключи в `.env` файл:
   ```env
   S3_ACCESS_KEY=your_access_key
   S3_SECRET_KEY=your_secret_key
   S3_BUCKET_NAME=pet-photos
   S3_ENDPOINT_URL=https://s3.regru.cloud
   ```

### Известные проблемы и решения
- **Ошибка 403 Forbidden** — проверьте права Access Key и политику бакета
- **Ошибка RequestTimeTooSkewed** — время на устройстве должно быть синхронизировано (используется UTC)
- **Дублирование фото на UI** — `selectedImages` очищается после загрузки

## 🤖 GigaChat интеграция

Для оценки рисков при подаче заявок используется **GigaChat** от Сбера:

- **Интеграция**: Ktor Client 2.3.10 + yandex-cloud SDK
- **Функция**: Анализ ответов на опросник и формирование оценки рисков
- **Результат**: `RiskAssessmentRecord` с рекомендациями для приюта

### Настройка GigaChat
Ключи загружаются из `.env` файла:
```env
GIGACHAT_CLIENT_ID=your_client_id
GIGACHAT_SCOPE=GIGACHAT_API_PERS
GIGACHAT_AUTH_KEY=your_auth_key
```

> ⚠️ **Важно:** Ключи загружаются из `.env`. Для production-версии перенесите работу с GigaChat на backend-сервер.

## 🔒 Безопасность

### Настройка секретов
Все API-ключи теперь загружаются из файла `.env` (не коммитится в Git):
```bash
cp .env.example .env
# Заполните .env своими ключами
```

### S3 и GigaChat
- Ключи больше не вшиты в код
- Загружаются из `.env` через Gradle (BuildConfigField)
- Для production рекомендуется backend-прокси

### ProGuard/R8
- Настроен для release-сборок (`isMinifyEnabled = true`)
- Правила в `proguard-rules.pro`:
  - Hilt (DI)
  - Supabase (Postgrest, Gotrue, Storage)
  - Ktor (HTTP клиент)
  - AWS SDK (S3)
  - kotlinx.serialization
  - Jetpack Compose

### Дополнительные материалы
- [SECURITY.md](SECURITY.md) — общие принципы безопасности
- [SECURITY_SETUP.md](SECURITY_SETUP.md) — пошаговая инструкция

## 🚧 Roadmap

### Реализовано
- [x] S3 загрузка фото с SigV4 подписью (reg.ru Cloud)
- [x] Редактирование питомцев с существующими фото
- [x] Удаление фото при редактировании
- [x] Исправление сохранения фото и тегов при редактировании (shelter_id)
- [x] GigaChat оценка рисков
- [x] Админ-панель для приютов
- [x] Кабинет приюта (ShelterScreen) со статистикой, управлением и автообновлением
- [x] Экран совпадений (MatchesScreen) со списком лайкнутых питомцев
- [x] Экран завершения опросника (QuestionnaireCompleteScreen)
- [x] Улучшенный опросник с Material 3 UI
  - Выпадающие списки (ExposedDropdownMenuBox)
  - Чекбоксы для множественного выбора
  - Анимации переходов и прогресс-бар
  - Пошаговая навигация по разделам (7 секций, 40+ вопросов)
- [x] Навигация с аутентификацией через Supabase Auth
- [x] Роли пользователей (user/admin/shelter) через `role` колонку в `users`
- [x] RLS политики для редактирования питомцев (приюты + админы)
- [x] Управление заявками:
  - Список заявок на питомца (PetApplicationsScreen) со статистикой и фильтр-чипами
  - Быстрые кнопки «Принять/Отклонить» в карточках заявок
  - Детальный экран заявки (PetApplicationDetailScreen) с полной информацией
  - Детальная оценка рисков GigaChat (градиентная шапка, прогресс-бар, секции факторов)
  - Кнопки «Подтвердить/Отклонить» с диалогами подтверждения в BottomBar
  - Автоматический перевод pending → processing при открытии списка
- [x] Добавлены колонки в `pets`: `size`, `breed`, `color`, `weight`, `traits`
- [x] Добавлен раздел 7 опросника: «Желаемые виды животных»
- [x] Автообновление ShelterScreen при возврате с экрана добавления/редактирования (LifecycleEventObserver)
- [x] Современный UI заявок: аватары с инициалами, цветовые акценты статусов, `animateItem`
- [x] **Чат** между пользователем и приютом в контексте заявки:
  - Таблица `chat_messages` с RLS политиками и автоматической установкой роли (триггер)
  - `ChatScreen` — Material 3 UI с адаптивными пузырьками, аватарами, форматированием времени
  - `ChatViewModel` + `ChatRepository` — загрузка и отправка сообщений
  - `ApplicationDetailWithChatScreen` — комбинированный экран заявки + чат для пользователя
  - Интеграция чата в `ApplicationsScreen` (иконка чата в карточке заявки)
  - Интеграция чата в `PetApplicationDetailScreen` (кнопка "Чат" в BottomBar для приюта)
  - Колонки `contact_days` в `applications` и `status` в `chat_messages`
- [x] **Веб-панель приюта** (`web-panel/`) — React + TypeScript + Vite + TailwindCSS:
  - Дашборд со статистикой и графиком заявок
  - Управление питомцами (CRUD, фильтры, поиск)
  - Просмотр заявок с детальной анкетой и оценкой рисков
  - Компонент чата (`Chat.tsx`) с real-time обновлениями через Supabase Realtime

### В планах
- [ ] Push-уведомления (Firebase Cloud Messaging)
- [ ] Real-time подписка в Android (Supabase Realtime для чата)
- [ ] Фильтрация питомцев (вид, возраст, пол)
- [ ] Firebase Analytics
- [ ] Офлайн-режим (Room DB + WorkManager)
- [ ] Тёмная тема (Material 3 Dynamic Color)
- [ ] Многоязычность (ru/en)

## 🐛 Известные проблемы

1. **Loading-экран** — нет таймаута, может зависнуть при проблемах с Supabase
2. **Placeholder изображений** — используется заглушка при пустом `photo_url`
3. **Нет CI/CD** — отсутствует GitHub Actions для автоматических тестов
4. **Тесты** — только boilerplate код, нет покрытых тестов бизнес-логики
5. **Устаревшие иконки** — некоторые иконки помечены как deprecated, нужно использовать `AutoMirrored` версии

> ✅ **Исправлено:**
> - ProGuard теперь настроен для release-сборок
> - S3/GigaChat ключи загружаются из `.env` файла (не вшиты в код)
> - Сохранение фото и тегов при редактировании питомца (shelter_id)
> - RLS политики для редактирования питомцев (поддержка ролей user/admin)
> - Загрузка оценки рисков для правильного пользователя (заявителя)
> - Добавлен статус `processing` в заявки
> - Добавлен раздел 7 опросника

## 📞 Контакты

Проект создан в учебных целях. Для вопросов обращайтесь через GitHub Issues.

---

*Файл актуализирован 29 мая 2026 г.*
*Обновлено: добавлен чат между пользователем и приютом, веб-панель приюта (React + Vite), новые модели и экраны (ChatMessage, ChatScreen, ChatViewModel, ChatRepository, ApplicationDetailWithChatScreen), обновлены структуры БД и навигация*
