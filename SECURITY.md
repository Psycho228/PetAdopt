# Безопасное хранение секретов в PetAdopt

## 🔒 Как настроить

### 1. Создайте файл `.env` в корне проекта
```bash
cp .env.example .env
```

### 2. Заполните `.env` своими значениями
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

### 3. Убедитесь, что `.env` в `.gitignore`
Файл `.env` автоматически игнорируется Git.

## ⚠️ Важные правила

1. **НИКОГДА** не коммитьте `.env` в Git
2. **НИКОГДА** не делитесь секретными ключами
3. Регулярно обновляйте ключи (ротация)
4. Используйте разные ключи для dev/prod
5. Для production используйте CI/CD secrets

## 🚨 Если ключи уже закоммичены

1. Сгенерируйте новые ключи в сервисах (S3, GigaChat, Supabase)
2. Обновите `.env` новыми ключами
3. Удалите старые ключи из истории Git:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch app/build.gradle.kts" \
     --prune-empty --tag-name-filter cat -- --all
   ```
4. Форсируйте пуш (если нужно):
   ```bash
   git push origin --force --all
   ```

## 🏗️ Сборка

Секреты автоматически подгрузятся из `.env` при сборке:
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## 📱 Production

Для production-сборок используйте:
- **GitHub Secrets** в CI/CD
- **Android Keystore** для хранения чувствительных данных
- **Backend API** для работы с секретными ключами (рекомендуется)