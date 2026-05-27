-- Добавление столбца traits (теги питомца) в таблицу pets
-- Выполнить в SQL редакторе Supabase

-- Добавляем столбец traits как текстовый массив
ALTER TABLE public.pets ADD COLUMN IF NOT EXISTS traits TEXT[] DEFAULT '{}';

-- Добавляем столбец size, если его нет
ALTER TABLE public.pets ADD COLUMN IF NOT EXISTS size TEXT DEFAULT 'medium' CHECK (size IN ('small', 'medium', 'large'));

-- Индекс для traits (для поиска по тегам)
CREATE INDEX IF NOT EXISTS idx_pets_traits ON public.pets USING GIN (traits);

-- Комментарии
COMMENT ON COLUMN public.pets.traits IS 'Теги/черты питомца (максимум 3)';
COMMENT ON COLUMN public.pets.size IS 'Размер питомца: small, medium, large';