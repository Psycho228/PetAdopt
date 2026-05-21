-- Добавить колонку size в таблицу pets
ALTER TABLE public.pets 
ADD COLUMN IF NOT EXISTS size TEXT DEFAULT 'medium' CHECK (size IN ('small', 'medium', 'large'));
