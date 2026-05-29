# Чат между пользователем и приютом

## Обзор функциональности

Реализована система чата для общения между пользователями и приютами в контексте заявок на пристройство питомцев.

## Где доступен чат

### Мобильное приложение (Android)
1. **Мои заявки** — при нажатии на иконку чата в карточке заявки
2. **Детали заявки (приют)** — кнопка "Чат" в нижней панели действий

### Веб-панель приюта
- **Детали заявки** — блок чата в нижней части страницы заявки

## Архитектура

### База данных (Supabase)

#### Таблица `chat_messages`
| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | UUID | Первичный ключ |
| `application_id` | UUID | Ссылка на заявку |
| `sender_id` | UUID | ID отправителя (из auth.users) |
| `sender_role` | TEXT | Роль отправителя: 'user', 'shelter', 'admin' |
| `message` | TEXT | Текст сообщения |
| `is_read` | BOOLEAN | Статус прочтения |
| `created_at` | TIMESTAMPTZ | Время отправки |

#### RLS политики
- **Чтение**: Пользователи видят сообщения только своих заявок, приюты — заявок на своих питомцев
- **Запись**: Только участники заявки (пользователь или приют)
- **Автоматическая установка роли**: Триггер определяет роль отправителя из таблицы `users`

### Структура кода (Android)

```
app/src/main/java/com/example/petadopt/
├── data/
│   ├── model/
│   │   └── ChatMessage.kt              # Модели чата
│   └── repository/
│       └── ChatRepository.kt           # Репозиторий чата
├── viewmodel/
│   └── ChatViewModel.kt                # ViewModel чата
├── ui/
│   └── screens/
│       └── ChatScreen.kt               # Экран чата
└── navigation/
    └── NavGraph.kt                     # Route: chat/{applicationId}
```

### Компоненты (Web)

```
web-panel/src/
├── components/
│   └── Chat.tsx                        # Компонент чата
└── pages/
    └── ApplicationDetailPage.tsx       # Интеграция чата
```

## Установка

### 1. Применение SQL-скрипта

Выполните в Supabase SQL Editor:

```sql
\i create_chat_messages_table.sql
```

Или скопируйте содержимое файла `create_chat_messages_table.sql` и выполните вручную.

### 2. Проверка установки

```sql
-- Проверка таблицы
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'chat_messages';

-- Проверка RLS политик
SELECT schemaname, tablename, policyname, permissive, roles, cmd 
FROM pg_policies 
WHERE tablename = 'chat_messages';

-- Проверка триггера
SELECT tgname, tgrelid::regclass, tgfunc 
FROM pg_trigger 
WHERE tgname = 'set_chat_message_sender_role_trigger';
```

## Использование

### Мобильное приложение

1. **Пользователь**:
   - Перейдите в "Мои заявки" через профиль
   - Нажмите иконку чата 📱 в карточке заявки
   - Напишите и отправьте сообщение

2. **Приют**:
   - Откройте заявку на питомца
   - Нажмите "Чат" в нижней панели
   - Общайтесь с заявителем

### Веб-панель

1. Откройте любую заявку
2. Прокрутите до раздела "Чат с заявителем"
3. Пишите и получайте сообщения в реальном времени

## Особенности реализации

### Android
- Material 3 дизайн с фиолетовым акцентом
- Адаптивные пузырьки сообщений (свой/чужой)
- Аватары с инициалами по ролям
- Форматирование времени (сегодня/дата)
- Кнопка обновления сообщений

### Web
- Tailwind CSS стилизация
- Real-time обновления через Supabase Realtime
- Автоматическая прокрутка к последнему сообщению
- Цветовая индикация ролей (пользователь/приют)

## API методы

### ChatRepository (Kotlin)
```kotlin
suspend fun getMessages(applicationId: String): List<ChatMessage>
suspend fun sendMessage(applicationId: String, message: String): Result<ChatMessage>
suspend fun markAsRead(messageId: String): Boolean
```

### Chat component (React)
```typescript
// Пропсы компонента
interface ChatProps {
  applicationId: string
  currentUserId: string
}
```

## Тестирование

### Тестовые сценарии

1. **Отправка сообщения пользователем**:
   ```sql
   -- Создайте тестовое сообщение
   INSERT INTO chat_messages (application_id, sender_id, sender_role, message)
   VALUES ('<application_uuid>', '<user_uuid>', 'user', 'Тестовое сообщение');
   ```

2. **Проверка RLS политик**:
   ```sql
   -- Проверка чтения (должно вернуть 0 строк для чужой заявки)
   SET ROLE authenticated;
   SELECT * FROM chat_messages;
   RESET ROLE;
   ```

3. **Триггер роли**:
   ```sql
   -- Вставка без указания роли (должна установиться автоматически)
   INSERT INTO chat_messages (application_id, sender_id, message)
   VALUES ('<app_id>', '<user_id>', 'Тест');
   
   SELECT sender_role FROM chat_messages WHERE message = 'Тест';
   -- Ожидается: 'user'
   ```

## Известные ограничения

1. **Real-time в Android**: Не реализована подписка через Supabase Realtime (технические ограничения библиотеки). Сообщения обновляются при ручном нажатии кнопки обновления.

2. **История сообщений**: Не ограничена по количеству. Для production рекомендуется пагинация.

3. **Медиа-файлы**: Пока поддерживается только текст. Фото/файлы — в планах.

4. **Уведомления**: Push-уведомления о новых сообщениях не реализованы.

## Планы развития

- [ ] Real-time подписка в Android (исправление зависимости Supabase)
- [ ] Push-уведомления через Firebase
- [ ] Отправка фотографий в чате
- [ ] Статусы доставки (отправлено/прочитано)
- [ ] Поиск по истории сообщений
- [ ] Экспорт переписки в PDF

## Диагностика

### Проблемы с подключением

```sql
-- Проверка соединения с Supabase
SELECT * FROM chat_messages LIMIT 1;

-- Проверка прав доступа
SELECT has_table_privilege(
  current_user, 
  'chat_messages', 
  'SELECT'
);
```

### Ошибки RLS

```sql
-- Временное отключение RLS для отладки
ALTER TABLE chat_messages DISABLE ROW LEVEL SECURITY;
-- Выполните тесты
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
```

## Контакты

Возникли вопросы? Создайте issue в GitHub репозитории проекта.

---

*Документация актуализирована 29 мая 2026 г.*
