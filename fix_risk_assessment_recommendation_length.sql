-- GigaChat may return a human-readable recommendation longer than 50 chars.
ALTER TABLE public.risk_assessments
    ALTER COLUMN "recommendation" TYPE TEXT;
