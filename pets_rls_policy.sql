-- Политики RLS для таблицы pets

-- Удаляем старые политики если есть
DROP POLICY IF EXISTS "Public read pets" ON public.pets;
DROP POLICY IF EXISTS "Authenticated insert pets" ON public.pets;
DROP POLICY IF EXISTS "Authenticated update pets" ON public.pets;
DROP POLICY IF EXISTS "Authenticated delete pets" ON public.pets;

-- Разрешаем чтение всем (без ограничений)
CREATE POLICY "Public read pets" ON public.pets
FOR SELECT USING (true);

-- Разрешаем создание авторизованным пользователям (владельцам приюта)
CREATE POLICY "Authenticated insert pets" ON public.pets
FOR INSERT WITH CHECK (
    auth.role() = 'authenticated' 
    AND shelter_id = auth.uid()
);

-- Разрешаем обновление владельцам
CREATE POLICY "Authenticated update pets" ON public.pets
FOR UPDATE USING (
    auth.role() = 'authenticated' 
    AND shelter_id = auth.uid()
);

-- Разрешаем удаление владельцам
CREATE POLICY "Authenticated delete pets" ON public.pets
FOR DELETE USING (
    auth.role() = 'authenticated' 
    AND shelter_id = auth.uid()
);
