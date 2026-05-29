-- Назначение роли приюту для пользователя danya2000@gmail.com
-- Выполнить в Supabase SQL Editor

-- 1. Проверить текущего пользователя
SELECT id, email, name, role FROM users WHERE email = 'danya2000@gmail.com';

-- 2. Назначить роль shelter
UPDATE users 
SET role = 'shelter', updated_at = NOW()
WHERE email = 'danya2000@gmail.com';

-- 3. Проверить результат
SELECT id, email, name, role FROM users WHERE email = 'danya2000@gmail.com';

-- Результат должен быть:
-- id | email | name | role
-- 3cf9296c-5088-4dfd-acce-288956761912 | danya2000@gmail.com | Даниил | shelter
