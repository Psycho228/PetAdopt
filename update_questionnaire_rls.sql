-- Обновление RLS политики для questionnaire_answers
-- Теперь приюты могут просматривать анкеты для ВСЕХ заявок на своих питомцев
-- (не только одобренных)

-- Удаляем старую политику
DROP POLICY IF EXISTS "Shelters can view questionnaire for approved applications" ON public.questionnaire_answers;

-- Создаём новую политику
CREATE POLICY "Shelters can view questionnaire for any pet application" ON public.questionnaire_answers
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.applications
            WHERE applications.user_id = questionnaire_answers.user_id
            AND (
                applications.status = 'approved'
                OR applications.status = 'processing'
                OR applications.status = 'pending'
            )
            AND EXISTS (
                SELECT 1 FROM public.pets
                WHERE pets.id = applications.pet_id
                AND pets.shelter_id = auth.uid()
            )
        )
    );

-- Примечание: Приюты теперь видят анкеты для заявок в статусах:
-- - pending (в ожидании)
-- - processing (в работе)
-- - approved (одобрена)
