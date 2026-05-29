-- Добавление колонки status в таблицу chat_messages
ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'sent' 
CHECK (status IN ('sent', 'delivered', 'read', 'failed'));

-- Коммент для новой колонки
COMMENT ON COLUMN chat_messages.status IS 'Статус сообщения: sent (отправлено), delivered (доставлено), read (прочитано), failed (ошибка)';

-- Индекс для статусов (опционально, для фильтрации)
CREATE INDEX IF NOT EXISTS idx_chat_messages_status ON chat_messages(status);
