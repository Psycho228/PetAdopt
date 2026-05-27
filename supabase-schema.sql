-- ============================================
-- SQL скрипт для настройки базы данных PetAdopt в Supabase
-- ============================================

-- Включить UUID расширение (если ещё не включено)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. Таблица users (профили пользователей)
-- ============================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    name TEXT NOT NULL,
    phone TEXT,
    city TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

-- ============================================
-- 2. Таблица pets (питомцы из приютов)
-- ============================================
CREATE TABLE IF NOT EXISTS public.pets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shelter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    age INTEGER NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('dog', 'cat', 'bird', 'other')),
    gender TEXT CHECK (gender IN ('male', 'female')),
    breed TEXT,
    color TEXT,
    weight DECIMAL(5,2),
    size TEXT DEFAULT 'medium' CHECK (size IN ('small', 'medium', 'large')),
    traits TEXT[] DEFAULT '{}',
    description TEXT,
    photo_url TEXT NOT NULL,
    additional_photos TEXT[],
    is_neutered BOOLEAN DEFAULT FALSE,
    has_vaccination BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_pets_shelter_id ON public.pets(shelter_id);
CREATE INDEX IF NOT EXISTS idx_pets_type ON public.pets(type);
CREATE INDEX IF NOT EXISTS idx_pets_is_active ON public.pets(is_active);

-- ============================================
-- 3. Таблица applications (заявки на пристройство)
-- ============================================
CREATE TABLE IF NOT EXISTS public.applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    pet_id UUID NOT NULL REFERENCES public.pets(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    message TEXT,
    user_name TEXT,
    user_email TEXT,
    pet_name TEXT,
    contact_time TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, pet_id)
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON public.applications(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_pet_id ON public.applications(pet_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON public.applications(status);

-- ============================================
-- 4. Таблица questionnaire_answers (ответы на опросник)
-- ============================================
CREATE TABLE IF NOT EXISTS public.questionnaire_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    
    -- Раздел 1: Основная информация
    q1_full_name TEXT,
    q1_age INTEGER,
    q1_city TEXT,
    q1_occupation TEXT,
    q1_contact_method TEXT,
    
    -- Раздел 2: Жилищные условия
    q2_housing_type TEXT CHECK (q2_housing_type IN ('apartment', 'house', 'rented', 'other')),
    q2_pets_allowed BOOLEAN,
    q2_living_with TEXT[],
    q2_family_consent BOOLEAN,
    q2_has_children BOOLEAN,
    q2_children_ages TEXT,
    q2_has_other_pets BOOLEAN,
    q2_other_pets_types TEXT[],
    q2_hours_alone INTEGER,
    q2_caregiver TEXT,
    
    -- Раздел 3: Опыт с животными
    q3_had_pets_before BOOLEAN,
    q3_what_happened TEXT,
    q3_dog_experience BOOLEAN,
    q3_cat_experience BOOLEAN,
    q3_special_needs_experience BOOLEAN,
    q3_why_now TEXT,
    
    -- Раздел 4: Ответственность и готовность
    q4_understand_requirements BOOLEAN DEFAULT FALSE,
    q4_understand_time BOOLEAN DEFAULT FALSE,
    q4_understand_attention BOOLEAN DEFAULT FALSE,
    q4_understand_training BOOLEAN DEFAULT FALSE,
    q4_understand_vet_care BOOLEAN DEFAULT FALSE,
    q4_ready_expenses BOOLEAN DEFAULT FALSE,
    q4_ready_food BOOLEAN DEFAULT FALSE,
    q4_ready_vet BOOLEAN DEFAULT FALSE,
    q4_ready_medication BOOLEAN DEFAULT FALSE,
    q4_ready_vaccinations BOOLEAN DEFAULT FALSE,
    q4_ready_grooming BOOLEAN DEFAULT FALSE,
    q4_furniture_damage_plan TEXT,
    q4_noise_plan TEXT,
    q4_shy_pet_plan TEXT,
    q4_long_adaptation_plan TEXT,
    q4_ready_education BOOLEAN DEFAULT FALSE,
    q4_life_changes_plan TEXT,
    q4_obstacles_next_year TEXT,
    
    -- Раздел 5: Безопасность
    q5_safety_measures TEXT[],
    q5_ready_neuter BOOLEAN DEFAULT FALSE,
    q5_ready_recommendations BOOLEAN DEFAULT FALSE,
    q5_ready_tracker BOOLEAN DEFAULT FALSE,
    q5_ready_keep_contact BOOLEAN DEFAULT FALSE,
    
    -- Раздел 6: Эмоциональная часть
    q6_responsible_owner_meaning TEXT,
    q6_life_with_pet_vision TEXT,
    q6_why_good_owner TEXT,
    
    -- Раздел 7: Желаемые виды животных
    q7_desired_pets TEXT[],
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_questionnaire_user_id ON public.questionnaire_answers(user_id);

-- ============================================
-- 5. Таблица user_likes (лайки пользователей)
-- ============================================
CREATE TABLE IF NOT EXISTS public.user_likes (
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    pet_id UUID NOT NULL REFERENCES public.pets(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, pet_id)
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON public.user_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_pet_id ON public.user_likes(pet_id);

-- ============================================
-- 6. Таблица shelters (приюты)
-- ============================================
CREATE TABLE IF NOT EXISTS public.shelters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    address TEXT,
    phone TEXT,
    website TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_shelters_user_id ON public.shelters(user_id);

-- ============================================
-- 7. Функция обновления updated_at
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггеры для обновления updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pets_updated_at BEFORE UPDATE ON public.pets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON public.applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_questionnaire_updated_at BEFORE UPDATE ON public.questionnaire_answers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shelters_updated_at BEFORE UPDATE ON public.shelters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 8. Row Level Security (RLS) политики
-- ============================================

-- Включаем RLS для всех таблиц
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questionnaire_answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.shelters ENABLE ROW LEVEL SECURITY;

-- --- Users ---
-- Пользователи могут читать только свои данные
CREATE POLICY "Users can view own profile" ON public.users
    FOR SELECT USING (auth.uid() = id);

-- Пользователи могут обновлять свои данные
CREATE POLICY "Users can update own profile" ON public.users
    FOR UPDATE USING (auth.uid() = id);

-- Все могут видеть публичные данные пользователей (для отображения в профиле)
CREATE POLICY "Everyone can view basic user info" ON public.users
    FOR SELECT USING (true);

-- --- Pets ---
-- Все могут просматривать активных питомцев
CREATE POLICY "Everyone can view active pets" ON public.pets
    FOR SELECT USING (is_active = true);

-- Приюты могут создавать питомцев
CREATE POLICY "Shelters can insert pets" ON public.pets
    FOR INSERT WITH CHECK (
        auth.uid() = shelter_id AND 
        EXISTS (SELECT 1 FROM public.shelters WHERE user_id = auth.uid())
    );

-- Приюты могут обновлять свои питомцы
CREATE POLICY "Shelters can update own pets" ON public.pets
    FOR UPDATE USING (auth.uid() = shelter_id);

-- Приюты могут удалять свои питомцы
CREATE POLICY "Shelters can delete own pets" ON public.pets
    FOR DELETE USING (auth.uid() = shelter_id);

-- --- Applications ---
-- Пользователи могут видеть свои заявки
CREATE POLICY "Users can view own applications" ON public.applications
    FOR SELECT USING (auth.uid() = user_id);

-- Приюты могут видеть заявки на своих питомцев
CREATE POLICY "Shelters can view pet applications" ON public.applications
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.pets 
            WHERE pets.id = applications.pet_id 
            AND pets.shelter_id = auth.uid()
        )
    );

-- Все могут создавать заявки
CREATE POLICY "Everyone can create applications" ON public.applications
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Пользователи могут обновлять свои заявки (только статус от приюта)
CREATE POLICY "Users can update own applications" ON public.applications
    FOR UPDATE USING (auth.uid() = user_id);

-- Приюты могут обновлять статус заявок
CREATE POLICY "Shelters can update application status" ON public.applications
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.pets 
            WHERE pets.id = applications.pet_id 
            AND pets.shelter_id = auth.uid()
        )
    );

-- --- Questionnaire Answers ---
-- Пользователи могут видеть и обновлять только свои ответы
CREATE POLICY "Users can view own questionnaire" ON public.questionnaire_answers
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own questionnaire" ON public.questionnaire_answers
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own questionnaire" ON public.questionnaire_answers
    FOR UPDATE USING (auth.uid() = user_id);

-- Приюты могут просматривать анкеты для одобренных заявок
CREATE POLICY "Shelters can view questionnaire for approved applications" ON public.questionnaire_answers
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.applications
            WHERE applications.user_id = questionnaire_answers.user_id
            AND applications.status = 'approved'
            AND EXISTS (
                SELECT 1 FROM public.pets
                WHERE pets.id = applications.pet_id
                AND pets.shelter_id = auth.uid()
            )
        )
    );

-- --- User Likes ---
-- Пользователи могут видеть свои лайки
CREATE POLICY "Users can view own likes" ON public.user_likes
    FOR SELECT USING (auth.uid() = user_id);

-- Все могут создавать лайки
CREATE POLICY "Everyone can create likes" ON public.user_likes
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Пользователи могут удалять свои лайки
CREATE POLICY "Users can delete own likes" ON public.user_likes
    FOR DELETE USING (auth.uid() = user_id);

-- --- Shelters ---
-- Все могут просматривать приюты
CREATE POLICY "Everyone can view shelters" ON public.shelters
    FOR SELECT USING (true);

-- Пользователи могут создавать свой профиль приюта
CREATE POLICY "Users can create shelter profile" ON public.shelters
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Пользователи могут обновлять свой приют
CREATE POLICY "Users can update own shelter" ON public.shelters
    FOR UPDATE USING (auth.uid() = user_id);

-- Пользователи могут удалять свой приют
CREATE POLICY "Users can delete own shelter" ON public.shelters
    FOR DELETE USING (auth.uid() = user_id);

-- ============================================
-- 9. Функция для автоматического создания профиля пользователя
-- ============================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, name)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'name', 'User')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Триггер для автоматического создания профиля при регистрации
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================
-- 10. Хранилище для фотографий (нужно создать в панели Supabase)
-- ============================================
-- В панели Supabase перейдите в Storage и создайте bucket:
-- - Имя: "pet-photos"
-- - Public: true (для публичного доступа к фото питомцев)
-- - File size limit: 5242880 (5MB)
-- - Allowed MIME types: ["image/jpeg", "image/png", "image/webp"]

-- Политика для bucket pet-photos:
-- Все могут читать
-- Все могут загружать
-- Пользователи могут удалять свои файлы

-- ============================================
-- Готово! База данных настроена.
-- ============================================
