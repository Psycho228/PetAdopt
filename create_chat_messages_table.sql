-- Создание таблицы сообщений чата
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    application_id UUID REFERENCES applications(id) ON DELETE CASCADE NOT NULL,
    sender_id UUID REFERENCES auth.users(id) NOT NULL,
    sender_role TEXT NOT NULL CHECK (sender_role IN ('user', 'shelter', 'admin')),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_chat_messages_application_id ON chat_messages(application_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);

-- Коммент для таблицы
COMMENT ON TABLE chat_messages IS 'Сообщения чата между пользователем и приютом по заявке';

-- RLS политики
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;

-- Политика чтения: пользователи могут читать сообщения только своих заявок
CREATE POLICY "Users can read chat messages for their applications"
    ON chat_messages FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM applications
            WHERE applications.id = chat_messages.application_id
            AND applications.user_id = auth.uid()
        )
    );

-- Политика чтения для приютов: могут читать сообщения заявок на свои питомцы
CREATE POLICY "Shelters can read chat messages for their pets applications"
    ON chat_messages FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM applications
            JOIN pets ON pets.id = applications.pet_id
            WHERE applications.id = chat_messages.application_id
            AND (pets.shelter_id = auth.uid() OR EXISTS (
                SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role IN ('shelter', 'admin')
            ))
        )
    );

-- Политика вставки: пользователи могут писать в свои заявки
CREATE POLICY "Users can insert chat messages for their applications"
    ON chat_messages FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM applications
            WHERE applications.id = chat_messages.application_id
            AND applications.user_id = auth.uid()
        )
    );

-- Политика вставки для приютов
CREATE POLICY "Shelters can insert chat messages for their pets applications"
    ON chat_messages FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM applications
            JOIN pets ON pets.id = applications.pet_id
            WHERE applications.id = chat_messages.application_id
            AND (pets.shelter_id = auth.uid() OR EXISTS (
                SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role IN ('shelter', 'admin')
            ))
        )
    );

-- Политика обновления (только для отметки прочитанным)
CREATE POLICY "Users can update their own chat messages"
    ON chat_messages FOR UPDATE
    USING (sender_id = auth.uid());

-- Функция для автоматической установки роли отправителя
CREATE OR REPLACE FUNCTION set_chat_message_sender_role()
RETURNS TRIGGER AS $$
BEGIN
    -- Проверяем роль пользователя из таблицы users
    IF EXISTS (
        SELECT 1 FROM users WHERE id = NEW.sender_id AND role = 'shelter'
    ) THEN
        NEW.sender_role := 'shelter';
    ELSIF EXISTS (
        SELECT 1 FROM users WHERE id = NEW.sender_id AND role = 'admin'
    ) THEN
        NEW.sender_role := 'admin';
    ELSE
        NEW.sender_role := 'user';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для автоматической установки роли
CREATE TRIGGER set_chat_message_sender_role_trigger
    BEFORE INSERT ON chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION set_chat_message_sender_role();

-- Грант на использование схемы
GRANT USAGE ON SCHEMA public TO authenticated;
GRANT SELECT, INSERT, UPDATE ON chat_messages TO authenticated;
