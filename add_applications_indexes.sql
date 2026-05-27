-- Индексы для таблицы applications для ускорения запросов
-- Выполнить в Supabase SQL Editor

-- Индекс по pet_id для быстрого поиска заявок на питомца
CREATE INDEX IF NOT EXISTS idx_applications_pet_id ON applications(pet_id);

-- Индекс по status для фильтрации pending заявок
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);

-- Композитный индекс для поиска pending заявок по питомцу
CREATE INDEX IF NOT EXISTS idx_applications_pet_status ON applications(pet_id, status);

-- Индекс по user_id для быстрого поиска заявок пользователя
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON applications(user_id);

-- Индекс по created_at для сортировки
CREATE INDEX IF NOT EXISTS idx_applications_created_at ON applications(created_at DESC);

-- Композитный индекс для основного сценария: pending заявки по питомцу с сортировкой
CREATE INDEX IF NOT EXISTS idx_applications_pet_status_created ON applications(pet_id, status, created_at DESC);
