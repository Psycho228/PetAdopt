-- ============================================
-- Добавление колонки role в таблицу users
-- ============================================

-- Добавляем колонку role в таблицу users
ALTER TABLE public.users 
ADD COLUMN IF NOT EXISTS role TEXT DEFAULT 'user';

-- Добавляем индекс для ускорения запросов по роли
CREATE INDEX IF NOT EXISTS idx_users_role ON public.users(role);

-- Обновляем триггер создания профиля для включения role
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, name, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'name', 'User'),
        'user' -- По умолчанию обычный пользователь
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Пересоздаём триггер
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================
-- Готово! Колонка role добавлена.
-- ============================================
