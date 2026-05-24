-- Миграция: Добавление колонки q7_desired_pets в таблицу questionnaire_answers
-- Выполнить в Supabase SQL Editor

-- Добавляем новую колонку для желаемых видов животных
ALTER TABLE public.questionnaire_answers 
ADD COLUMN IF NOT EXISTS q7_desired_pets TEXT[];

-- Обновляем существующие записи (если есть) - пустой массив
UPDATE public.questionnaire_answers 
SET q7_desired_pets = '{}' 
WHERE q7_desired_pets IS NULL;

-- Проверяем, что колонка добавлена
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'questionnaire_answers' 
AND column_name = 'q7_desired_pets';
