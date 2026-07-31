-- Создание таблицы для хранения оценок рисков
-- Выполнить в SQL редакторе Supabase

CREATE TABLE IF NOT EXISTS risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    questionnaire_answer_id VARCHAR(255) NOT NULL,
    
    -- Результаты оценки
    "overallRisk" VARCHAR(50) NOT NULL,  -- LOW, MEDIUM, HIGH, VERY_HIGH
    "riskScore" INTEGER NOT NULL CHECK ("riskScore" >= 0 AND "riskScore" <= 100),
    "recommendation" TEXT NOT NULL,
    
    -- Данные для отображения
    "detailedAnalysis" TEXT NOT NULL,
    "riskFactorsJson" JSONB NOT NULL DEFAULT '[]'::jsonb,
    "positiveFactorsJson" JSONB NOT NULL DEFAULT '[]'::jsonb,
    "recommendationsJson" JSONB NOT NULL DEFAULT '[]'::jsonb,
    
    -- Метаданные
    "gigachat_request_id" VARCHAR(255),
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Индексы для быстрого поиска
CREATE INDEX idx_risk_assessments_user_id ON risk_assessments(user_id);
CREATE INDEX idx_risk_assessments_created_at ON risk_assessments(created_at DESC);
CREATE INDEX idx_risk_assessments_overall_risk ON risk_assessments("overallRisk");

-- RLS политики (Row Level Security)
ALTER TABLE risk_assessments ENABLE ROW LEVEL SECURITY;

-- Пользователи могут читать только свои оценки
CREATE POLICY "Users can view their own risk assessments"
    ON risk_assessments
    FOR SELECT
    USING (auth.uid() = user_id);

-- Пользователи могут вставлять свои оценки
CREATE POLICY "Users can insert their own risk assessments"
    ON risk_assessments
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Админы и приюты могут видеть все оценки
CREATE POLICY "Admins can view all risk assessments"
    ON risk_assessments
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users
            WHERE users.id = auth.uid()
            AND (users.role = 'admin' OR users.role = 'shelter')
        )
    );

-- Комментарии к столбцам
COMMENT ON TABLE risk_assessments IS 'Оценки рисков пристройства питомца от GigaChat';
COMMENT ON COLUMN risk_assessments."overallRisk" IS 'Общий уровень риска: LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN risk_assessments."riskScore" IS 'Балл риска от 0 до 100';
COMMENT ON COLUMN risk_assessments."recommendation" IS 'Рекомендация: APPROVE, APPROVE_WITH_CONDITIONS, REVIEW_REQUIRED, REJECT';
COMMENT ON COLUMN risk_assessments."detailedAnalysis" IS 'Развёрнутый анализ от GigaChat';
COMMENT ON COLUMN risk_assessments."riskFactorsJson" IS 'JSON массив факторов риска';
COMMENT ON COLUMN risk_assessments."positiveFactorsJson" IS 'JSON массив положительных факторов';
COMMENT ON COLUMN risk_assessments."recommendationsJson" IS 'JSON массив рекомендаций';
COMMENT ON COLUMN risk_assessments."gigachat_request_id" IS 'ID запроса к GigaChat для трассировки';
