-- Политики RLS для storage bucket pet-photos
-- Разрешаем чтение всем
CREATE POLICY "Public Access" ON storage.objects
FOR SELECT USING (
    bucket_id = 'pet-photos'
);

-- Разрешаем загрузку авторизованным пользователям
CREATE POLICY "Authenticated Upload" ON storage.objects
FOR INSERT WITH CHECK (
    bucket_id = 'pet-photos' 
    AND auth.role() = 'authenticated'
);

-- Разрешаем обновление авторизованным пользователям
CREATE POLICY "Authenticated Update" ON storage.objects
FOR UPDATE USING (
    bucket_id = 'pet-photos' 
    AND auth.role() = 'authenticated'
);

-- Разрешаем удаление авторизованным пользователям
CREATE POLICY "Authenticated Delete" ON storage.objects
FOR DELETE USING (
    bucket_id = 'pet-photos' 
    AND auth.role() = 'authenticated'
);
