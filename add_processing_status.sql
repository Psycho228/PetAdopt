-- Добавление статуса "processing" в таблицу applications
-- Выполнить в Supabase SQL Editor

-- Сначала удаляем старый constraint
ALTER TABLE applications DROP CONSTRAINT IF EXISTS applications_status_check;

-- Добавляем новый constraint с поддержкой статуса "processing"
ALTER TABLE applications 
ADD CONSTRAINT applications_status_check 
CHECK (status IN ('pending', 'processing', 'approved', 'rejected'));

-- Примечание: Статусы заявок:
-- pending — заявка в ожидании
-- processing — заявка в работе (приют открыл для просмотра)
-- approved — заявка принята
-- rejected — заявка отклонена
