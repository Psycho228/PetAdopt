-- ============================================
-- Исправление RLS политик для таблицы pets
-- ============================================

-- 1. Обновляем политику UPDATE, чтобы разрешить админам редактировать всех
DROP POLICY IF EXISTS "Shelters can update own pets" ON public.pets;
DROP POLICY IF EXISTS "Admins can update all pets" ON public.pets;

-- Политика для приютов: могут обновлять только своих питомцев
CREATE POLICY "Shelters can update own pets" ON public.pets
FOR UPDATE USING (
    auth.role() = 'authenticated' 
    AND shelter_id = auth.uid()
);

-- Политика для админов: могут обновлять всех питомцев
CREATE POLICY "Admins can update all pets" ON public.pets
FOR UPDATE USING (
    auth.role() = 'authenticated'
    AND EXISTS (
        SELECT 1 FROM public.users 
        WHERE users.id = auth.uid() 
        AND users.role = 'admin'
    )
);

-- 2. Обновляем политику DELETE аналогично
DROP POLICY IF EXISTS "Shelters can delete own pets" ON public.pets;
DROP POLICY IF EXISTS "Admins can delete all pets" ON public.pets;

-- Политика для приютов: могут удалять только своих питомцев
CREATE POLICY "Shelters can delete own pets" ON public.pets
FOR DELETE USING (
    auth.role() = 'authenticated' 
    AND shelter_id = auth.uid()
);

-- Политика для админов: могут удалять всех питомцев
CREATE POLICY "Admins can delete all pets" ON public.pets
FOR DELETE USING (
    auth.role() = 'authenticated'
    AND EXISTS (
        SELECT 1 FROM public.users 
        WHERE users.id = auth.uid() 
        AND users.role = 'admin'
    )
);

-- 3. (Опционально) Исправить shelter_id у существующих питомцев
-- Если у некоторых питомцев shelter_id пустой или некорректный,
-- можно вручную обновить их, указав правильный shelter_id
-- 
-- Пример: найти питомцев с некорректным shelter_id
-- SELECT id, name, shelter_id FROM pets WHERE shelter_id IS NULL OR shelter_id = '';
--
-- Пример: обновить shelter_id (замените 'correct-user-uuid' на правильный UUID)
-- UPDATE pets SET shelter_id = 'correct-user-uuid' WHERE shelter_id IS NULL;

-- ============================================
-- Готово! Политики обновлены.
-- ============================================
