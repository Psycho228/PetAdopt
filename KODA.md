# KODA.md — Справочник по проекту PetAdopt

## 📱 Обзор проекта

**PetAdopt** — мобильное приложение для пристройства питомцев из приютов. Реализовано с использованием современной Android-архитектуры (MVVM + Clean Architecture) и Supabase бэкенда.

### Основные функции
- **Свайп-механика** — поиск питомцев по принципу Tinder (лайк/дизлайк)
- **Заявки** — подача заявок на пристройство питомца с отслеживанием статуса
- **Опросник** — детальная анкета потенциального хозяина (6 разделов, 37 вопросов) с Material 3 UI
- **Профиль** — управление личными данными, просмотр истории заявок и ответов на опросник
- **Админ-панель** — управление питомцами (добавление, редактирование) для приютов
- **Supabase** — аутентификация, PostgreSQL база данных и S3 хранилище
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
| S3 Хранилище | AWS SDK for Kotlin + SigV4 подпись |
| GigaChat | Ktor Client + yandex-cloud SDK |
| Навигация | Navigation Compose |
| Картинки | Coil 2.6.0 |
| Иконки | Material Icons Extended |
| Сборка | Gradle 8.13 + AGP 8.2.2 |
| Мин SDK | 24 (Android 7.0) |
| Целевой SDK | 34 (Android 14) |

## 📁 Структура проекта (Clean Architecture)

```
app/src/main/java/com/example/petadopt/
├── data/
│   ├── model/              # Модели данных
│   │   ├── Application.kt
│   │   ├── GigaChatRiskAssessment.kt
│   │   ├── Pet.kt
│   │   ├── QuestionnaireAnswer.kt
│   │   ├── QuestionnaireAnswerExtensions.kt
│   │   ├── RiskAssessmentRecord.kt
│   │   └── User.kt
│   └── repository/         # Репозитории
│       ├── AdminRepository.kt
│       ├── AuthRepository.kt
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
│   │   ├── ApplicationScreen.kt
│   │   ├── ApplicationsScreen.kt
│   │   ├── AuthScreen.kt
│   │   ├── DetailsScreen.kt
│   │   ├── EditProfileScreen.kt
│   │   ├── MatchesScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── QuestionnaireScreen.kt
│   │   └── SwipeScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/              # ViewModels для каждого экрана
│   ├── AccountViewModel.kt
│   ├── AdminViewModel.kt
│   ├── ApplicationsViewModel.kt
│   ├── ApplicationViewModel.kt
│   ├── AuthViewModel.kt
│   ├── NavViewModel.kt
│   ├── QuestionnaireState.kt
│   ├── QuestionnaireViewModel.kt
│   └── SwipeViewModel.kt
└── util/                   # Утилиты и расширения
```

## 🚀 Сборка и запуск

### Команды сборки
```bash
# Сборка debug-версии
./gradlew assembleDebug

# Сборка release-версии
./gradlew assembleRelease

# Запуск тестов
./gradlew testDebugUnitTest

# Очистка и пересборка
./gradlew clean assembleDebug
```

### Предварительные требования
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34
- Настроенный Supabase проект

## 🔧 Supabase конфигурация

### Коллекции (таблицы)
| Таблица | Описание |
|---------|----------|
| `users` | Профили пользователей (id, email, name, role) |
| `pets` | Данные питомцев (имя, возраст, вид, описание, фото) |
| `applications` | Заявки на пристройство (userId, petId, status, timestamp) |
| `questionnaire_answers` | Ответы на опросник (userId + все поля анкеты) |
| `likes` | Лайки пользователей (userId, petId, created_at) |
| `risk_assessments` | Оценки рисков от GigaChat |

### Роли пользователей
- `user` — обычный пользователь (ищет питомца)
- `admin` — администратор приюта (добавляет питомцев)

### Статусы заявок
- `pending` — заявка на рассмотрении
- `approved` — заявка одобрена
- `rejected` — заявка отклонена

## 📝 Опросник (6 разделов, 37 вопросов)

### UI-реализация
- **Пошаговая навигация** — 6 секций с прогресс-баром
- **Анимации** — slide, fade, scale при переходе между шагами
- **Типы вопросов**:
  - `Text` — однострочные и многострочные поля ввода
  - `Dropdown` — выпадающий список (ExposedDropdownMenuBox)
  - `CheckboxGroup` — группа чекбоксов для множественного выбора
  - `YesNo` — кнопки Да/Нет с визуальной индикацией

### Раздел 1: Основная информация (5 вопросов)
- Как вас зовут? (Text)
- Сколько вам лет? (Text)
- В каком городе вы живёте? (Dropdown)
- Чем вы занимаетесь? (Text)
- Как с вами лучше связаться? (Text)

### Раздел 2: Жилищные условия (10 вопросов)
- Где вы живёте? (Dropdown: Квартира/Частный дом/Съёмное жильё/Другое)
- Разрешены ли животные в вашем жилье? (YesNo)
- С кем вы живёте? (Dropdown: Один/Семья/Друзья/Другое)
- Все ли члены семьи согласны? (YesNo)
- Есть ли у вас дети? (YesNo)
- Если да — какого возраста? (Text)
- Есть ли у вас другие животные? (YesNo)
- Если да — то какие? (Text)
- Сколько часов в день питомец будет один? (Text)
- Кто будет ухаживать в ваше отсутствие? (Text)

### Раздел 3: Опыт с животными (6 вопросов)
- Были ли у вас раньше питомцы? (YesNo)
- Что с ними сейчас? (Text)
- Опыт ухода за собаками? (YesNo)
- Опыт ухода за кошками? (YesNo)
- Опыт с животными с особенностями? (YesNo)
- Почему решили взять питомца именно сейчас? (Text)

### Раздел 4: Ответственность и готовность (9 вопросов)
- Понимаете ли вы, что потребуется? (CheckboxGroup: Время/Внимание/Обучение/Ветпомощь)
- Готовы ли к расходам на? (CheckboxGroup: Корм/Ветеринар/Лекарства/Прививки/Груминг)
- Что будете делать, если питомец испортит мебель? (Text)
- Что будете делать, если будет шуметь? (Text)
- Что будете делать, если окажется пугливым? (Text)
- Что будете делать при долгой адаптации? (Text)
- Готовы ли к воспитанию и адаптации? (YesNo)
- Что будете делать при изменении обстоятельств? (Text)
- Есть ли препятствия в ближайший год? (Text)

### Раздел 5: Безопасность (3 вопроса)
- Установлены ли? (CheckboxGroup: Сетки/Безопасные балконы/Ограждения)
- Готовы ли? (CheckboxGroup: Стерилизовать/Соблюдать рекомендации/Адресник)
- Готовы ли поддерживать связь после пристройства? (YesNo)

### Раздел 6: Эмоциональная часть (3 вопроса)
- Что для вас значит "ответственный хозяин"? (Text)
- Как представляете жизнь с питомцем? (Text)
- Почему именно вы станете хорошим хозяином? (Text)

## 🎨 Дизайн-система

### Цветовая палитра
```kotlin
val Primary = Color(0xFF6200EE)      // Фиолетовый (основной)
val Background = Color(0xFFFFFFFF)   // Белый (фон)
val TextSecondary = Color(0xFF757575) // Серый (вторичный текст)
```

### UI компоненты
| Компонент | Описание |
|-----------|----------|
| `PrimaryButton` | Кнопка на всю ширину, фиолетовый фон, белый текст |
| `Screen` | Контейнер экрана с отступами 16dp и белым фоном |
| `SwipeCard` | Карточка с жестом свайпа (порог 320px) |
| `PetCard` | Карточка питомца с изображением и тегами |
| `RiskAssessmentCard` | Карточка оценки рисков от GigaChat |
| `QuestionCard` | Карточка вопроса с иконкой и скруглёнными углами (16dp) |
| `ExposedDropdownMenu` | Выпадающий список Material 3 |
| `CheckboxGroup` | Группа чекбоксов для множественного выбора |

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

### Навигация
```kotlin
// Routes
composable("auth") { AuthScreen(...) }
composable("loading") { ... }
composable("onboarding") { OnboardingScreen(...) }
composable("questionnaire") { QuestionnaireScreen(onFinish = {...}) }
composable("swipe") { SwipeScreen(...) }
composable("details/{petId}") { DetailsScreen(...) }
composable("application/{petId}/{petName}") { ApplicationScreen(...) }
composable("matches") { MatchesScreen(...) }
composable("account") { AccountScreen(...) }
composable("edit_profile") { EditProfileScreen(...) }
composable("applications") { ApplicationsScreen(...) }
composable("admin") { AdminScreen(...) }
composable("admin/addPet") { AddEditPetScreen(...) }
composable("admin/editPet/{petId}") { AddEditPetScreen(...) }

// Переход с аргументами
navController.navigate("details/$petId")
// Получение в экране:
val petId = backStackEntry.arguments?.getString("petId")
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
```bash
# Unit тесты
./gradlew test

# Интеграционные тесты
./gradlew connectedAndroidTest
```
- Unit тесты: `src/test/`
- Интеграционные тесты: `src/androidTest/`

## ☁️ S3 хранилище (reg.ru Cloud)

Приложение использует **S3 совместимое хранилище** для загрузки фотографий питомцев:

- **Провайдер**: reg.ru Cloud Object Storage
- **Бакет**: `pet-photos`
- **Доступ**: Path-style URL (`https://s3.regru.cloud/pet-photos/{key}`)
- **Авторизация**: AWS SigV4 подпись запросов

### Настройка S3
1. Создайте бакет `pet-photos` в reg.ru Cloud
2. Настройте публичный доступ на чтение (`s3:GetObject`)
3. Создайте Access Key с правами `s3:PutObject`, `s3:DeleteObject`
4. Ключи доступны через `BuildConfig` (временное решение для проверки)

## 🤖 GigaChat интеграция

Для оценки рисков при подаче заявок используется **GigaChat** от Сбера:

- **Интеграция**: Ktor Client + yandex-cloud SDK
- **Функция**: Анализ ответов на опросник и формирование оценки рисков
- **Результат**: `RiskAssessmentRecord` с рекомендациями для приюта

### Настройка GigaChat
Ключи доступны через `BuildConfig`:
```kotlin
buildConfigField("String", "GIGACHAT_CLIENT_ID", "\"...\"")
buildConfigField("String", "GIGACHAT_SCOPE", "\"GIGACHAT_API_PERS\"")
buildConfigField("String", "GIGACHAT_AUTH_KEY", "\"...\"")
```

## 🚧 Roadmap

### Реализовано
- [x] S3 загрузка фото с SigV4 подписью
- [x] Редактирование питомцев с существующими фото
- [x] Удаление фото при редактировании
- [x] GigaChat оценка рисков
- [x] Админ-панель для приютов
- [x] Улучшенный опросник с Material 3 UI
  - Выпадающие списки (ExposedDropdownMenuBox)
  - Чекбоксы для множественного выбора
  - Анимации переходов и прогресс-бар
  - Пошаговая навигация по разделам

### В планах
- [ ] Push-уведомления (Firebase Cloud Messaging)
- [ ] Чат между приютом и пользователем
- [ ] Фильтрация питомцев (вид, возраст, пол)
- [ ] Firebase Analytics
- [ ] Офлайн-режим (Room DB + WorkManager)
- [ ] Тёмная тема (Material 3 Dynamic Color)
- [ ] Многоязычность (ru/en)

## 🐛 Известные проблемы

1. **Loading-экран** — нет таймаута, может зависнуть при проблемах с Supabase
2. **Placeholder изображений** — используется заглушка при пустом `imageUrl`
3. **ProGuard** — правила пустые, R8 не настроен для release-сборки
4. **Нет CI/CD** — отсутствует GitHub Actions для автоматических тестов
5. **S3 ключи** — временно вшиты в `BuildConfig`, нужно перенести в безопасное хранилище
6. **Устаревшие иконки** — некоторые иконки помечены как deprecated (ArrowBack, ArrowForward), нужно использовать AutoMirrored версии

## 📞 Контакты

Проект создан в учебных целях. Для вопросов обращайтесь через GitHub Issues.

---

*Файл актуализирован 23 мая 2026 г.*