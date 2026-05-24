-- Миграция: Замена колонки q1_contact_method на q1_phone и q1_email
-- Выполнить в Supabase SQL Editor

-- Добавляем новые колонки
ALTER TABLE public.questionnaire_answers 
ADD COLUMN IF NOT EXISTS q1_phone TEXT,
ADD COLUMN IF NOT EXISTS q1_email TEXT;

-- Копируем данные из q1_contact_method в q1_phone (если там был телефон)
-- и очищаем старую колонку
UPDATE public.questionnaire_answers 
SET q1_phone = q1_contact_method
WHERE q1_contact_method IS NOT NULL AND q1_contact_method != '';

-- Удаляем старую колонку (опционально, можно оставить для совместимости)
-- ALTER TABLE public.questionnaire_answers DROP COLUMN IF EXISTS q1_contact_method;

-- Проверяем, что колонки добавлены
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'questionnaire_answers' 
AND column_name IN ('q1_phone', 'q1_email', 'q1_contact_method');
