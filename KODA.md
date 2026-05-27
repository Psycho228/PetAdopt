# KODA.md — Справочник по проекту PetAdopt

## 📱 Обзор проекта

**PetAdopt** — мобильное приложение для пристройства питомцев из приютов. Реализовано с использованием современной Android-архитектуры (MVVM + Clean Architecture) и Supabase бэкенда.

### Основные функции
- **Свайп-механика** — поиск питомцев по принципу Tinder (лайк/дизлайк)
- **Заявки** — подача заявок на пристройство питомца с отслеживанием статуса
- **Опросник** — детальная анкета потенциального хозяина (6 разделов, 37 вопросов) с Material 3 UI
- **Профиль** — управление личными данными, просмотр истории заявок и ответов на опросник
- **Совпадения** — список лайкнутых питомцев с возможностью удаления лайков
- **Админ-панель** — управление питомцами (добавление, редактирование) для приютов
- **Кабинет приюта** — статистика, управление питомцами, просмотр заявок
- **Управление заявками** — просмотр списка заявок на питомца, детальный экран с опросником и оценкой рисков, подтверждение/отклонение заявок
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
2. Заполните `.env` своими ключами (S3, GigaChat, Supabase)

### Команды сборки
```bash
# Сборка debug-версии
./gradlew assembleDebug

# Сборка release-версии (с ProGuard)
./gradlew assembleRelease

# Запуск тестов
./gradlew testDebugUnitTest

# Очистка и пересборка
./gradlew clean assembleDebug
```

> ⚠️ **Важно:** Перед первым запуском создайте `.env` файл на основе `.env.example`. Без ключей сборка покажет предупреждение, но продолжится.

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
- `admin` — администратор приюта (управляет всеми питомцами)
- `shelter` — сотрудник приюта (управляет своими питомцами)

### Статусы заявок
- `pending` — заявка на рассмотрении
- `processing` — заявка в работе (автоматически при открытии списка)
- `approved` — заявка одобрена
- `rejected` — заявка отклонена

## 📝 Опросник (6 разделов, 37 вопросов)

### UI-реализация
- **Пошаговая навигация** — 6 секций с прогресс-баром
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

### Раздел 1: Основная информация (5 вопросов)
- Как вас зовут? (Text, **обязательно**)
- Сколько вам лет? (Text, **обязательно**, только числа 18-120)
- В каком городе вы живёте? (Dropdown, **обязательно**)
- Чем вы занимаетесь? (Text, **обязательно**)
- Как с вами лучше связаться? (Text, **обязательно**)

### Раздел 2: Жилищные условия (10 вопросов)
- Где вы живёте? (Dropdown: Квартира/Частный дом/Съёмное жильё/Другое, **обязательно**)
- Разрешены ли животные в вашем жилье? (YesNo, **обязательно**)
- С кем вы живёте? (Dropdown: Один/Семья/Друзья/Другое, **обязательно**)
- Все ли члены семьи согласны? (YesNo, **обязательно**)
- Есть ли у вас дети? (YesNo, **обязательно**)
- Если да — какого возраста? (Text, **обязательно если есть дети**, только числа)
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

### Раздел 4: Ответственность и готовность (9 вопросов)
- Понимаете ли вы, что потребуется? (CheckboxGroup: Время/Внимание/Обучение/Ветпомощь, **минимум 1**)
- Готовы ли к расходам на? (CheckboxGroup: Корм/Ветеринар/Лекарства/Прививки/Груминг, **минимум 1**)
- Что будете делать, если питомец испортит мебель? (Text, **обязательно**)
- Что будете делать, если будет шуметь? (Text, **обязательно**)
- Что будете делать, если окажется пугливым? (Text, **обязательно**)
- Что будете делать при долгой адаптации? (Text, **обязательно**)
- Готовы ли к воспитанию и адаптации? (YesNo, **обязательно**)
- Что будете делать при изменении обстоятельств? (Text, **обязательно**)
- Есть ли препятствия в ближайший год? (Text, **обязательно**)

### Раздел 5: Безопасность (3 вопроса)
- Установлены ли? (CheckboxGroup: Сетки/Безопасные балконы/Ограждения, **минимум 1**)
- Готовы ли? (CheckboxGroup: Стерилизовать/Соблюдать рекомендации/Адресник, **минимум 1**)
- Готовы ли поддерживать связь после пристройства? (YesNo, **обязательно**)

### Раздел 6: Эмоциональная часть (3 вопроса)
- Что для вас значит "ответственный хозяин"? (Text, **обязательно**)
- Как представляете жизнь с питомцем? (Text, **обязательно**)
- Почему именно вы станете хорошим хозяином? (Text, **обязательно**)

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
composable("admin") { AdminScreen(...) -> ShelterScreen(...) }
composable("shelter") { ShelterScreen(...) }
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
4. Добавьте ключи в `.env` файл:
   ```env
   S3_ACCESS_KEY=your_access_key
   S3_SECRET_KEY=your_secret_key
   S3_BUCKET_NAME=pet-photos
   S3_ENDPOINT_URL=https://s3.regru.cloud
   ```

## 🤖 GigaChat интеграция

Для оценки рисков при подаче заявок используется **GigaChat** от Сбера:

- **Интеграция**: Ktor Client + yandex-cloud SDK
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
- Загружаются из `.env` через Gradle
- Для production рекомендуется backend-прокси

### ProGuard/R8
- Настроен для release-сборок
- Правила в `proguard-rules.pro`
- Минификация включена (`isMinifyEnabled = true`)

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
- [x] Кабинет приюта (ShelterScreen) со статистикой и управлением
- [x] Экран совпадений (MatchesScreen) со списком лайкнутых питомцев
- [x] Экран завершения опросника (QuestionnaireCompleteScreen)
- [x] Улучшенный опросник с Material 3 UI
  - Выпадающие списки (ExposedDropdownMenuBox)
  - Чекбоксы для множественного выбора
  - Анимации переходов и прогресс-бар
  - Пошаговая навигация по разделам (6 секций, 37 вопросов)
- [x] Навигация с аутентификацией через Supabase Auth
- [x] Роли пользователей (user/admin/shelter) через `role` колонку в `users`
- [x] RLS политики для редактирования питомцев (приюты + админы)
- [x] Управление заявками:
  - Список заявок на питомца (PetApplicationsScreen) со статистикой
  - Детальный экран заявки (PetApplicationDetailScreen) с полной информацией
  - Отображение оценки рисков (GigaChat) на детальном экране
  - Кнопки "Подтвердить"/"Отклонить" с диалогами подтверждения
  - Автоматический перевод pending → processing при открытии списка

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
3. **Нет CI/CD** — отсутствует GitHub Actions для автоматических тестов
4. **Тесты** — только boilerplate код, нет покрытых тестов бизнес-логики
5. **Колонка `size` в `pets`** — добавлена через миграцию `add_size_column.sql`, проверьте актуальность схемы
6. **Устаревшие иконки** — `ArrowBack`, `TrendingUp` помечены как deprecated, нужно использовать `AutoMirrored` версии

> ✅ **Исправлено:**
> - ProGuard теперь настроен для release-сборок
> - S3/GigaChat ключи загружаются из `.env` файла (не вшиты в код)
> - Сохранение фото и тегов при редактировании питомца (shelter_id)
> - RLS политики для редактирования питомцев (поддержка ролей user/admin)
> - Загрузка оценки рисков для правильного пользователя (заявителя)
> - Добавлен статус `processing` в заявки

## 📞 Контакты

Проект создан в учебных целях. Для вопросов обращайтесь через GitHub Issues.

---

*Файл актуализирован 28 мая 2026 г.*
*Обновлено: добавлена безопасность, ProGuard, .env конфигурация, исправление редактирования питомцев, RLS политики, управление заявками с оценкой рисков*