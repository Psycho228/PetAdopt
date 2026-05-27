-- Обновление RLS политики для questionnaire_answers
-- Позволяет приютам просматривать анкеты пользователей, подавших заявки на их питомцев

-- Удаление старой политики
DROP POLICY IF EXISTS "Shelters can view questionnaire for any pet application" ON public.questionnaire_answers;

-- Создание новой политики с правильными условиями
CREATE POLICY "Shelters can view questionnaire for any pet application" ON public.questionnaire_answers
FOR SELECT USING (
  EXISTS (
      SELECT 1 FROM public.applications
      WHERE applications.user_id = questionnaire_answers.user_id
        AND applications.status IN ('pending', 'processing', 'approved')
        AND EXISTS (
            SELECT 1 FROM public.pets
            WHERE pets.id = applications.pet_id
              AND pets.shelter_id = auth.uid()
        )
  )
);
