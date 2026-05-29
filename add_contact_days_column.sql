-- Добавление колонки contact_days в таблицу applications
ALTER TABLE applications ADD COLUMN IF NOT EXISTS contact_days TEXT;

-- Добавление комментария к колонке
COMMENT ON COLUMN applications.contact_days IS 'Дни недели для связи (например: "Пн, Вт, Ср, Чт, Пт")';
