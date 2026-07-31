# Хвостики

«Хвостики» — Android-приложение и веб-панель для пристройства животных из
приютов и публикации проверенных предложений от заводчиков. Пользователь
заполняет анкету, просматривает питомцев, отправляет заявку и общается с
приютом. Сотрудники приюта управляют питомцами и заявками, а администраторы
проверяют профили заводчиков и объявления.

## Возможности

### Для будущих владельцев

- регистрация и вход через Supabase Auth;
- пошаговый опросник потенциального владельца;
- оценка анкеты через GigaChat;
- свайп-карточки и список понравившихся питомцев;
- подробная карточка питомца;
- отправка и отслеживание заявок;
- чат с приютом в контексте заявки;
- отдельный каталог проверенных объявлений от заводчиков;
- профиль с сохраненными ответами и историей заявок.

### Для заводчиков

- профиль питомника с контактами, городом и породами;
- создание и редактирование объявлений о продаже;
- статусы проверки, публикации, резерва, продажи и архива;
- повторная отправка исправленного профиля или объявления на модерацию.

### Для приюта

- добавление, редактирование, архивирование и импорт питомцев;
- загрузка фотографий в S3-совместимое хранилище;
- статистика по питомцам и заявкам;
- фильтрация и обработка заявок;
- просмотр анкеты и оценки рисков;
- принятие или отклонение заявки;
- чат с заявителем.

## Состав проекта

```text
.
|-- app/                         Android-приложение
|   `-- src/main/java/com/example/petadopt/
|       |-- data/                модели и репозитории
|       |-- domain/usecase/      сценарии приложения
|       |-- di/                  Hilt-модули
|       |-- navigation/          Compose Navigation
|       |-- ui/                  экраны, компоненты и тема
|       |-- util/                Supabase и S3-конфигурация
|       `-- viewmodel/           StateFlow-состояния экранов
|-- web-panel/                   React/Vite-панель приюта
|-- gradle/libs.versions.toml    версии Android-зависимостей
`-- *.sql                        схема, миграции и RLS-политики
```

Android-часть является одним Gradle-модулем `:app`. Основной поток:

```text
data.model -> data.repository -> domain.usecase -> viewmodel -> ui.screens
```

## Технологии

### Android

- Kotlin 2.1.20, AGP 8.5.2, JDK 17;
- compileSdk/targetSdk 34, minSdk 24;
- Jetpack Compose и Material 3;
- Navigation Compose, Coil, Coroutines и StateFlow;
- Hilt 2.51.1;
- Supabase Auth, PostgREST и Storage;
- Ktor с OkHttp;
- AWS SDK for Kotlin и SigV4 для S3;
- GigaChat API для оценки анкеты.

### Веб-панель

- React 18 и TypeScript;
- Vite 5;
- Tailwind CSS;
- Supabase JS;
- Recharts и Lucide Icons;
- XLSX/ZIP-импорт питомцев.

## Требования

- Android Studio с JDK 17;
- Android SDK 34;
- Node.js и npm для веб-панели;
- настроенный Supabase;
- S3-совместимое хранилище;
- учетные данные GigaChat.

## Конфигурация Android

Скопируйте `.env.example` в `.env` в корне проекта и заполните значения:

```env
SUPABASE_URL=https://your-supabase.example
SUPABASE_ANON_KEY=your_anon_key

S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key
S3_BUCKET_NAME=pet-photos
S3_ENDPOINT_URL=https://s3.regru.cloud

GIGACHAT_CLIENT_ID=your_client_id
GIGACHAT_SCOPE=GIGACHAT_API_PERS
GIGACHAT_AUTH_KEY=your_authorization_key
```

Gradle передает эти значения в `BuildConfig`. Файл `.env` и реальные ключи
нельзя добавлять в Git.

GigaChat использует корневой сертификат НУЦ Минцифры. Сертификат находится в
`app/src/main/res/raw/`, а доверие ограничено доменами GigaChat через
`network_security_config.xml`. Не отключайте проверку TLS в HTTP-клиенте.

## Сборка Android

В PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat testDebugUnitTest
```

Debug APK создается в:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release-сборка использует R8, но перед публикацией необходимо настроить
подпись и проверить правила в `proguard-rules.pro`.

## Настройка Supabase

Базовая схема находится в `supabase-schema.sql`. Дополнительные SQL-файлы в
корне добавляют:

- роли пользователей и RLS-политики;
- поля питомцев и заявок;
- индексы заявок;
- ответы опросника и оценки рисков;
- таблицу и статусы сообщений чата;
- таблицы, роли и RLS-политики маркетплейса из `add_breeder_marketplace.sql`;
- политики хранилища.

Применяйте миграции осознанно и сначала проверяйте их на тестовом окружении.
Основные таблицы приложения:

| Таблица | Назначение |
| --- | --- |
| `users` | профили и роли пользователей |
| `pets` | карточки питомцев |
| `likes` | понравившиеся питомцы |
| `applications` | заявки на пристройство |
| `questionnaire_answers` | ответы пользователя |
| `risk_assessments` | результаты анализа GigaChat |
| `chat_messages` | переписка по заявкам |
| `breeder_profiles` | проверяемые профили заводчиков |
| `sale_listings` | объявления о продаже питомцев |

Роли: `user`, `shelter`, `breeder`, `admin`. Статусы заявок: `pending`, `processing`,
`approved`, `rejected`.

## Веб-панель

Создайте `web-panel/.env` на основе `web-panel/.env.example`:

```env
VITE_SUPABASE_URL=https://your-supabase.example
VITE_SUPABASE_ANON_KEY=your_anon_key

VITE_S3_ACCESS_KEY=your_access_key
VITE_S3_SECRET_KEY=your_secret_key
VITE_S3_BUCKET_NAME=pet-photos
VITE_S3_ENDPOINT_URL=https://s3.regru.cloud
```

Запуск и production-сборка:

```powershell
cd web-panel
npm.cmd install
npm.cmd run dev
npm.cmd run build
```

Готовые статические файлы создаются в `web-panel/dist/`.

> Важно: переменные `VITE_*` попадают в клиентский JavaScript. Текущая
> реализация прямой S3-загрузки подходит только для ограниченного окружения.
> Для production следует перенести подпись S3 и GigaChat на серверный backend.

### Развертывание панели

Текущая конфигурация Nginx использует домен `hvostiki-admpan.online` и каталог:

```text
/var/www/petadopt
```

После `npm.cmd run build` загрузите **содержимое** `web-panel/dist/` в этот
каталог. Для FileZilla используйте SFTP, сервер `89.111.143.6`, порт `22`.
Конфигурация виртуального хоста находится в `web-panel/nginx-site.conf`.

## Чат

Android-чат обновляет сообщения каждые 5 секунд, пока экран открыт. Фоновое
обновление не блокирует ввод и останавливается при выходе с экрана.

Веб-панель использует Supabase Realtime. Если WebSocket недоступен, включается
резервное обновление каждые 5 секунд. Отправленное сообщение добавляется в UI
сразу из ответа PostgREST и не зависит от Realtime.

## Дизайн

Android и веб-панель используют общую визуальную систему «Хвостиков»:

| Назначение | Цвет |
| --- | --- |
| основной | `#2F7D6B` |
| темный основной | `#1F5F52` |
| дополнительный | `#FF8A5B` |
| фон | `#F7F3EC` |
| основной текст | `#17231F` |
| вторичный текст | `#6C766F` |

## Тесты и ограничения

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedAndroidTest
```

Автоматических тестов пока мало: присутствуют в основном стандартные примеры.
Известные технические задачи:

- добавить тесты репозиториев, use case и ViewModel;
- восстановить стабильный Supabase Realtime на сервере;
- заменить polling Android-чата на Realtime после стабилизации WebSocket;
- вынести клиентские секреты и интеграции на backend;
- заполнить и проверить правила R8;
- убрать разрешение cleartext traffic после миграции всех адресов на HTTPS.

## Безопасность

- не коммитьте `.env`, ключи Supabase, S3 и GigaChat;
- не отключайте TLS-проверку и hostname verification;
- сохраняйте RLS включенным для пользовательских таблиц;
- перед применением SQL и загрузкой новой веб-сборки делайте резервную копию;
- anon key Supabase не заменяет RLS и сам по себе не защищает данные.

Проект развивается под брендом «Хвостики». Внутренние package names и имя
Gradle-проекта пока сохранены как `PetAdopt`, чтобы не выполнять рискованную
миграцию идентификаторов приложения.
