# KODA.md — Справочник по проекту PetAdopt

## 📱 Обзор проекта

**PetAdopt** — мобильное приложение для пристройства питомцев из приютов. Реализовано с использованием современной Android-архитектуры (MVVM + Clean Architecture) и Firebase бэкенда.

### Основные функции
- **Свайп-механика** — поиск питомцев по принципу Tinder (лайк/дизлайк)
- **Заявки** — подача заявок на пристройство питомца с отслеживанием статуса
- **Опросник** — детальная анкета потенциального хозяина (6 разделов, 37 вопросов)
- **Профиль** — управление личными данными, просмотр истории заявок и ответов на опросник
- **Firebase** — аутентификация и хранение данных в Firestore

## 🛠 Стек технологий

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 1.9.24 |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt 2.51.1 |
| Бэкенд | Firebase Auth + Firestore |
| Навигация | Navigation Compose |
| Картинки | Coil 2.6.0 |
| Сборка | Gradle 8.13 + AGP 8.13.2 |
| Мин SDK | 24 (Android 7.0) |
| Целевой SDK | 34 (Android 14) |

## 📁 Структура проекта (Clean Architecture)

```
app/src/main/java/com/example/petadopt/
├── data/
│   ├── model/              # Модели данных (User, Pet, Application, QuestionnaireAnswer)
│   └── repository/         # Репозитории (PetRepository, AuthRepository, QuestionnaireRepository)
├── domain/
│   ├── usecase/            # Бизнес-логика (UseCase'и)
│   │   ├── AuthUseCases.kt      (Login, Register, Logout, GetUser...)
│   │   ├── PetUseCases.kt       (GetPets, SubmitApplication, LikePet...)
│   │   └── QuestionnaireUseCases.kt (SaveQuestionnaire, GetQuestionnaire...)
├── di/
│   ├── AppModule.kt        # Firebase зависимости
│   └── RepositoryModule.kt # Провизоры репозиториев и UseCase'ов
├── navigation/
│   └── NavGraph.kt         # Настройка навигации и routes
├── ui/
│   ├── components/         # Переиспользуемые UI компоненты
│   │   ├── PrimaryButton.kt
│   │   ├── Screen.kt
│   │   ├── SwipeCard.kt
│   │   └── PetCard.kt
│   ├── screens/            # Экраны приложения
│   │   ├── AuthScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── QuestionnaireScreen.kt
│   │   ├── SwipeScreen.kt
│   │   ├── DetailsScreen.kt
│   │   ├── ApplicationScreen.kt
│   │   ├── ApplicationsScreen.kt
│   │   └── AccountScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/              # ViewModels для каждого экрана
│   ├── AccountViewModel.kt
│   ├── SwipeViewModel.kt
│   ├── ApplicationViewModel.kt
│   └── QuestionnaireViewModel.kt
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
- Настроенный Firebase проект с файлом `google-services.json` в `app/`

## 🔥 Firebase конфигурация

### Коллекции Firestore
| Коллекция | Описание |
|-----------|----------|
| `users` | Профили пользователей (uid, email, name) |
| `pets` | Данные питомцев (имя, возраст, вид, описание, фото) |
| `applications` | Заявки на пристройство (userId, petId, status, timestamp) |
| `questionnaire_answers` | Ответы на опросник (userId + все поля анкеты) |
| `users/{userId}/likes` | Лайки пользователя (petId, timestamp) |

### Статусы заявок
- `pending` — заявка на рассмотрении
- `approved` — заявка одобрена
- `rejected` — заявка отклонена

## 📝 Опросник (6 разделов, 37 вопросов)

### Раздел 1: Основная информация (5 вопросов)
- Как вас зовут?
- Сколько вам лет?
- В каком городе вы живёте?
- Чем вы занимаетесь?
- Как с вами лучше связаться?

### Раздел 2: Жилищные условия (10 вопросов)
- Где вы живёте? (Квартира/Частный дом/Съёмное жильё/Другое)
- Разрешены ли животные в вашем жилье? (Да/Нет)
- С кем вы живёте? (Один/Семья/Друзья/Другое)
- Все ли члены семьи согласны? (Да/Нет)
- Есть ли у вас дети? (Да/Нет)
- Если да — какого возраста?
- Есть ли у вас другие животные? (Да/Нет)
- Если да — то какие?
- Сколько часов в день питомец будет один?
- Кто будет ухаживать в ваше отсутствие?

### Раздел 3: Опыт с животными (6 вопросов)
- Были ли у вас раньше питомцы? (Да/Нет)
- Что с ними сейчас?
- Опыт ухода за собаками? (Да/Нет)
- Опыт ухода за кошками? (Да/Нет)
- Опыт с животными с особенностями? (Да/Нет)
- Почему решили взять питомца именно сейчас?

### Раздел 4: Ответственность и готовность (9 вопросов)
- Понимаете ли вы, что потребуется: время, внимание, обучение, ветпомощь? (чекбоксы)
- Готовы ли к расходам на: корм, ветеринара, лекарства, прививки, груминг? (чекбоксы)
- Что будете делать, если питомец испортит мебель?
- Что будете делать, если будет шуметь?
- Что будете делать, если окажется пугливым?
- Что будете делать при долгой адаптации?
- Готовы ли к воспитанию и адаптации? (Да/Нет)
- Что будете делать при изменении жизненных обстоятельств?
- Есть ли препятствия в ближайший год?

### Раздел 5: Безопасность (3 вопроса)
- Установлены ли: сетки на окнах, безопасные балконы, ограждения? (чекбоксы)
- Готовы ли: стерилизовать, соблюдать рекомендации, использовать адресник? (чекбоксы)
- Готовы ли поддерживать связь после пристройства? (Да/Нет)

### Раздел 6: Эмоциональная часть (3 вопроса)
- Что для вас значит "ответственный хозяин"?
- Как представляете жизнь с питомцем?
- Почему именно вы станете хорошим хозяином?

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
| `InfoItem` | Элемент информации (иконка + label + value) |

## 🔧 Архитектурные паттерны

### Репозитории
```kotlin
@Singleton
class PetRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getPets(): List<Pet> { ... }
    suspend fun submitApplication(application: Application) { ... }
}
```
- Внедрение через `@Inject constructor`
- Все методы `suspend` с `.await()` для Firebase
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
composable("questionnaire") { QuestionnaireScreen(onFinish = {...}) }
composable("swipe") { SwipeScreen(...) }
composable("account") { AccountScreen(onRetakeQuestionnaire = {...}) }

// Переход с аргументами
navController.navigate("details_from_application/$petId")
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

### Тестирование
```bash
# Unit тесты
./gradlew test

# Интеграционные тесты
./gradlew connectedAndroidTest
```
- Unit тесты: `src/test/`
- Интеграционные тесты: `src/androidTest/`

## 🚧 Roadmap

- [ ] Добавить Push-уведомления (Firebase Cloud Messaging)
- [ ] Реализовать чат между приютом и пользователем
- [ ] Добавить фильтрацию питомцев по параметрам (вид, возраст, пол)
- [ ] Интегрировать Firebase Analytics
- [ ] Добавить офлайн-режим (Room DB + WorkManager)
- [ ] Поддержка тёмной темы (Material 3 Dynamic Color)
- [ ] Многоязычность (ресурсы en/ru)

## 🐛 Известные проблемы

1. **Loading-экран** — нет таймаута, может зависнуть при проблемах с Firebase
2. **Placeholder изображений** — используется `via.placeholder.com` вместо реальных заглушек
3. **ProGuard** — правила пустые, R8 не настроен для release-сборки
4. **Нет CI/CD** — отсутствует GitHub Actions для автоматических тестов

## 📞 Контакты

Проект создан в учебных целях. Для вопросов обращайтесь к разработчику.

---

*Файл сгенерирован 17 мая 2026 г.*
