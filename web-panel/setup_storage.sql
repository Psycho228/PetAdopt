-- Настройка Storage bucket для фото питомцев
-- Выполнить в Supabase SQL Editor

-- 1. Создать bucket (если не существует)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'pet-photos',
  'pet-photos',
  true,
  5242880,
  ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO UPDATE SET
  public = true,
  file_size_limit = 5242880,
  allowed_mime_types = ARRAY['image/jpeg', 'image/png', 'image/webp'];

-- 2. Политика: все могут читать фото
CREATE POLICY IF NOT EXISTS "Public read pet-photos"
  ON storage.objects
  FOR SELECT
  USING (bucket_id = 'pet-photos');

-- 3. Политика: авторизованные могут загружать
CREATE POLICY IF NOT EXISTS "Authenticated upload pet-photos"
  ON storage.objects
  FOR INSERT
  WITH CHECK (
    bucket_id = 'pet-photos'
    AND auth.role() = 'authenticated'
  );

-- 4. Политика: авторизованные могут удалять свои файлы
CREATE POLICY IF NOT EXISTS "Authenticated delete pet-photos"
  ON storage.objects
  FOR DELETE
  USING (
    bucket_id = 'pet-photos'
    AND auth.role() = 'authenticated'
  );

-- 5. Проверить результат
SELECT * FROM storage.buckets WHERE id = 'pet-photos';
