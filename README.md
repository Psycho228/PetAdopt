# 🐾 PetAdopt

**Мобильное приложение для пристройства питомцев из приютов**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-1.5.0-purple.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange.svg)](https://firebase.google.com)
[![Hilt](https://img.shields.io/badge/Hilt-2.51.1-red.svg)](https://dagger.dev/hilt/)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-green.svg)](https://developer.android.com/topic/architecture)

---

## 📱 О проекте

PetAdopt — это современное Android-приложение, которое связывает питомцев из приютов с заботливыми хозяевами. Приложение использует знакомый свайп-интерфейс (как в Tinder) для удобного поиска питомца и подробный опросник для оценки готовности пользователя стать ответственным хозяином.

### ✨ Ключевые возможности

- 🔥 **Свайп-механика** — листайте карточки питомцев, лайкайте тех, кто пришёлся по душе
- 📝 **Подробный опросник** — 6 разделов, 37 вопросов для оценки готовности к питомцу
- 📬 **Заявки на пристройство** — отправляйте заявки и отслеживайте их статус
- 👤 **Личный кабинет** — история заявок, ответы на опросник, настройки профиля
- 🔐 **Безопасная аутентификация** — вход через Firebase Auth (email/пароль или анонимно)
- 🌐 **Облачные данные** — синхронизация через Firestore в реальном времени

---

## 🛠 Стек технологий

| Компонент | Технология |
|-----------|------------|
| **Язык** | Kotlin 1.9.24 |
| **UI** | Jetpack Compose + Material 3 |
| **Архитектура** | MVVM + Clean Architecture |
| **DI** | Hilt 2.51.1 |
| **Бэкенд** | Firebase Auth + Firestore |
| **Навигация** | Navigation Compose |
| **Изображения** | Coil 2.6.0 |
| **Асинхронность** | Kotlin Coroutines + Flow |
| **Сборка** | Gradle 8.13 + AGP 8.13.2 |

### Минимальные требования
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 34 (Android 14)

---

## 🏗 Архитектура проекта

Проект построен по принципам **Clean Architecture** с разделением на слои:

```
app/src/main/java/com/example/petadopt/
├── data/
│   ├── model/             # Модели данных (User, Pet, Application...)
│   └── repository/         # Репозитории (Firebase-реализация)
├── domain/
│   ├── model/              # Доменные модели
│   └── usecase/           # Бизнес-логика (UseCase'и)
├── di/                    # Модули внедрения зависимостей (Hilt)
├── navigation/            # NavGraph и routes
├── ui/
│   ├── components/         # Переиспользуемые UI компоненты
│   ├── screens/           # Экраны приложения
│   └── theme/             # Цвета, типографика, темы
├── viewmodel/             # ViewModels для каждого экрана
└── util/                  # Утилиты и расширения
```

---

## 📲 Установка и запуск

### Предварительные требования
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34
- Настроенный Firebase проект (файл `google-services.json`)

### Быстрый старт

```bash
# Клонировать репозиторий
git clone https://github.com/ваш-юзернейм/PetAdopt.git
cd PetAdopt

# Создать Firebase проект и добавить google-services.json в app/

# Собрать debug-версию
./gradlew assembleDebug

# Запустить тесты
./gradlew testDebugUnitTest

# Очистка и пересборка
./gradlew clean assembleDebug
```

> ⚠️ **Важно:** Перед первым запуском создайте Firebase проект, добавьте Android-приложение и скачайте `google-services.json` в папку `app/`.

---

## 🎨 Дизайн-система

### Цветовая палитра
- **Primary**: `#6200EE` — основной фиолетовый
- **Background**: `#FFFFFF` — белый фон
- **TextSecondary**: `#757575` — вторичный текст

### UI компоненты
- `PrimaryButton` — кнопка на всю ширину с фирменным цветом
- `Screen` — контейнер с отступами 16dp и белым фоном
- `SwipeCard` — карточка с поддержкой жестов свайпа (порог 320px)
- `PetCard` — карточка питомца с изображением и тегами
- `InfoItem` — элемент информации (иконка + метка + значение)

---

## 📊 Структура данных (Firestore)

### Коллекции
| Коллекция | Описание |
|-----------|----------|
| `users` | Профили пользователей (uid, email, name) |
| `pets` | Данные питомцев (имя, возраст, вид, фото, описание) |
| `applications` | Заявки на пристройство (status: pending/approved/rejected) |
| `questionnaire_answers` | Ответы на опросник (полная анкета хозяина) |
| `users/{userId}/likes` | Лайки пользователя (petId, timestamp) |

---

## 📝 Опросник потенциального хозяина

**6 разделов, 37 вопросов** для оценки готовности:

1. **Основная информация** (5 вопросов) — имя, возраст, город, контакты
2. **Жилищные условия** (10 вопросов) — тип жилья, разрешение на животных, состав семьи
3. **Опыт с животными** (6 вопросов) — прошлые питомцы, опыт с собаками/кошками
4. **Ответственность и готовность** (9 вопросов) — понимание затрат, времени, рисков
5. **Безопасность** (3 вопроса) — сетки на окнах, стерилизация, адресник
6. **Эмоциональная часть** (3 вопроса) — мотивация, видение жизни с питомцем

---

## 🚀 Roadmap

### В планах
- [ ] Push-уведомления (Firebase Cloud Messaging)
- [ ] Чат между приютом и пользователем
- [ ] Фильтрация питомцев (вид, возраст, пол)
- [ ] Firebase Analytics
- [ ] Офлайн-режим (Room DB + WorkManager)
- [ ] Тёмная тема (Material 3 Dynamic Color)
- [ ] Многоязычность (ru/en)

---

## 🧪 Тестирование

```bash
# Unit-тесты
./gradlew test

# Интеграционные тесты
./gradlew connectedAndroidTest
```

> ⚠️ На данный момент тесты представлены только boilerplate-кодом. Добро пожаловать вклад в покрытие тестами!

---

## 📄 Лицензия

Проект создан в **учебных целях**. Все права защищены.

---

## 🤝 Вклад

Вносите свой вклад в развитие проекта! Открыты к PR и issue'ам.

1. Fork проекта
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit изменений (`git commit -m 'Add some AmazingFeature'`)
4. Push в branch (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

---

## 📞 Контакты

Проект разработан в учебных целях. По вопросам обращайтесь через GitHub Issues.

---

<div align="center">

**Сделано с ❤️ для питомцев, ищущих дом**

</div>