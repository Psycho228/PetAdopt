# Инструкция по настройке безопасности PetAdopt

## 🚀 Быстрый старт

### 1. Создайте файл `.env` в корне проекта:
```bash
cp .env.example .env
```

### 2. Заполните `.env` своими ключами:
```env
S3_ACCESS_KEY=your_actual_access_key
S3_SECRET_KEY=your_actual_secret_key
S3_BUCKET_NAME=pet-photos
S3_ENDPOINT_URL=https://s3.regru.cloud

GIGACHAT_CLIENT_ID=your_actual_client_id
GIGACHAT_SCOPE=GIGACHAT_API_PERS
GIGACHAT_AUTH_KEY=your_actual_auth_key

SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_actual_anon_key
```

### 3. Запустите сборку:
```bash
./gradlew assembleDebug
```

## 🔒 Что было сделано

### 1. Удаление секретов из кода
- ✅ Все API-ключи удалены из `build.gradle.kts`
- ✅ Ключи теперь загружаются из `.env` файла
- ✅ `.env` автоматически игнорируется Git

### 2. Настройка ProGuard/R8
- ✅ Создан `proguard-rules.pro` с правилами для:
  - Hilt (DI)
  - Supabase (Postgrest, Gotrue, Storage)
  - Ktor (HTTP клиент)
  - AWS SDK (S3)
  - kotlinx.serialization
  - Jetpack Compose

### 3. Включение минификации для release
- ✅ `isMinifyEnabled = true` для release-сборок
- ✅ `isShrinkResources = true` для удаления неиспользуемого кода
- ✅ Применение `proguard-rules.pro`

### 4. Документация
- ✅ `SECURITY.md` — подробное руководство по безопасности
- ✅ `.env.example` — шаблон для секретов
- ✅ Обновлён `KODA.md` с инструкциями

## ⚠️ Если ключи уже закоммичены в Git

### КРИТИЧНО: Немедленно замените все ключи!

1. **Сгенерируйте новые ключи** в соответствующих сервисах:
   - S3 (reg.ru Cloud) — создайте новые Access Keys
   - GigaChat — обновите Client ID и Auth Key
   - Supabase — пересоздайте Anon Key

2. **Обновите `.env`** новыми ключами

3. **Очистите историю Git** (если нужно):
   ```bash
   # Удаление секретов из истории
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch app/build.gradle.kts" \
     --prune-empty --tag-name-filter cat -- --all
   
   # Очистка reflog
   git reflog expire --expire=now --all
   git gc --prune=now --aggressive
   ```

4. **Форсируйте пуш** (осторожно!):
   ```bash
   git push origin --force --all
   git push origin --force --tags
   ```

5. **Уведомите команду** о смене ключей

## 🏗️ Production-развёртывание

### Рекомендации для production:

1. **Используйте CI/CD Secrets** (GitHub Actions, GitLab CI):
   ```yaml
   # .github/workflows/build.yml
   env:
     S3_ACCESS_KEY: ${{ secrets.S3_ACCESS_KEY }}
     S3_SECRET_KEY: ${{ secrets.S3_SECRET_KEY }}
     # ... другие секреты
   ```

2. **Backend-прокси для секретов**:
   - Не храните секретные ключи в мобильном приложении
   - Создайте backend API для:
     - Загрузки фото в S3 (через ваш сервер)
     - Оценки рисков через GigaChat
     - Работы с Supabase

3. **Android Keystore** для локальных данных:
   ```kotlin
   val encryptedPrefs = EncryptedSharedPreferences.create(
       context,
       "secure_prefs",
       masterKey,
       EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
       EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
   )
   ```

4. **Certificate Pinning** для HTTPS:
   ```kotlin
   val certificatePinner = CertificatePinner.Builder()
       .add("your-api.com", "sha256/AAAAAAAA...")
       .build()
   
   val client = OkHttpClient.Builder()
       .certificatePinner(certificatePinner)
       .build()
   ```

## 📋 Чек-лист безопасности

- [ ] Все секреты удалены из кода
- [ ] `.env` добавлен в `.gitignore`
- [ ] Создан `.env.example` без реальных ключей
- [ ] ProGuard настроен для release-сборок
- [ ] RLS политики в Supabase проверены
- [ ] Certificate pinning настроен (опционально)
- [ ] Backend-прокси для секретов (рекомендуется)
- [ ] Логирование чувствительных данных отключено в release
- [ ] Debug инфо в production отключена

## 🆘 Помощь

При возникновении проблем:
1. Проверьте, что `.env` файл существует
2. Убедитесь, что ключи правильно скопированы
3. Проверьте права доступа к файлам
4. Попробуйте `./gradlew clean assembleDebug`
