-- Breeder marketplace for "Hvostiki".
-- Run this migration in the Supabase SQL editor after the base schema.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION public.is_marketplace_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.users
        WHERE id = auth.uid() AND role = 'admin'
    );
$$;

REVOKE ALL ON FUNCTION public.is_marketplace_admin() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_marketplace_admin() TO authenticated;

CREATE OR REPLACE FUNCTION public.protect_user_role()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NEW.role IS DISTINCT FROM OLD.role
       AND NOT public.is_marketplace_admin() THEN
        RAISE EXCEPTION 'Only administrators can change user roles';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS protect_user_role_on_update ON public.users;
CREATE TRIGGER protect_user_role_on_update
    BEFORE UPDATE OF role ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.protect_user_role();

CREATE TABLE IF NOT EXISTS public.breeder_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES public.users(id) ON DELETE CASCADE,
    kennel_name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    city TEXT NOT NULL,
    phone TEXT NOT NULL,
    website TEXT,
    breeds TEXT[] NOT NULL DEFAULT '{}',
    verification_status TEXT NOT NULL DEFAULT 'pending'
        CHECK (verification_status IN ('pending', 'verified', 'rejected')),
    moderation_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.sale_listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    breeder_id UUID NOT NULL REFERENCES public.breeder_profiles(id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('cat', 'dog', 'bird', 'other')),
    gender TEXT NOT NULL CHECK (gender IN ('male', 'female')),
    breed TEXT NOT NULL,
    birth_date DATE,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency TEXT NOT NULL DEFAULT 'RUB' CHECK (currency = 'RUB'),
    description TEXT NOT NULL,
    photo_url TEXT NOT NULL DEFAULT '',
    additional_photos TEXT[] NOT NULL DEFAULT '{}',
    vaccinated BOOLEAN NOT NULL DEFAULT FALSE,
    vet_passport BOOLEAN NOT NULL DEFAULT FALSE,
    pedigree BOOLEAN NOT NULL DEFAULT FALSE,
    chipped BOOLEAN NOT NULL DEFAULT FALSE,
    delivery_available BOOLEAN NOT NULL DEFAULT FALSE,
    status TEXT NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft', 'pending', 'available', 'reserved', 'sold', 'rejected', 'archived')),
    moderation_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION public.assign_breeder_role_after_verification()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NEW.verification_status = 'verified'
       AND OLD.verification_status IS DISTINCT FROM 'verified' THEN
        UPDATE public.users
        SET role = 'breeder'
        WHERE id = NEW.user_id AND role = 'user';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS assign_breeder_role_after_verification ON public.breeder_profiles;
CREATE TRIGGER assign_breeder_role_after_verification
    AFTER UPDATE OF verification_status ON public.breeder_profiles
    FOR EACH ROW EXECUTE FUNCTION public.assign_breeder_role_after_verification();

CREATE INDEX IF NOT EXISTS idx_breeder_profiles_user_id
    ON public.breeder_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_breeder_profiles_verification
    ON public.breeder_profiles(verification_status);
CREATE INDEX IF NOT EXISTS idx_sale_listings_owner
    ON public.sale_listings(owner_id);
CREATE INDEX IF NOT EXISTS idx_sale_listings_breeder
    ON public.sale_listings(breeder_id);
CREATE INDEX IF NOT EXISTS idx_sale_listings_catalog
    ON public.sale_listings(status, type, created_at DESC);

DROP TRIGGER IF EXISTS update_breeder_profiles_updated_at ON public.breeder_profiles;
CREATE TRIGGER update_breeder_profiles_updated_at
    BEFORE UPDATE ON public.breeder_profiles
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

DROP TRIGGER IF EXISTS update_sale_listings_updated_at ON public.sale_listings;
CREATE TRIGGER update_sale_listings_updated_at
    BEFORE UPDATE ON public.sale_listings
    FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

ALTER TABLE public.breeder_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sale_listings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Public can view verified breeders" ON public.breeder_profiles;
CREATE POLICY "Public can view verified breeders"
    ON public.breeder_profiles
    FOR SELECT
    USING (
        verification_status = 'verified'
        OR user_id = auth.uid()
        OR public.is_marketplace_admin()
    );

DROP POLICY IF EXISTS "Users can create own breeder profile" ON public.breeder_profiles;
CREATE POLICY "Users can create own breeder profile"
    ON public.breeder_profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (
        user_id = auth.uid()
        AND verification_status = 'pending'
    );

DROP POLICY IF EXISTS "Users can update own breeder profile" ON public.breeder_profiles;
CREATE POLICY "Users can update own breeder profile"
    ON public.breeder_profiles
    FOR UPDATE
    TO authenticated
    USING (user_id = auth.uid() OR public.is_marketplace_admin())
    WITH CHECK (
        public.is_marketplace_admin()
        OR (user_id = auth.uid() AND verification_status IN ('pending', 'rejected'))
    );

DROP POLICY IF EXISTS "Public can view approved sale listings" ON public.sale_listings;
CREATE POLICY "Public can view approved sale listings"
    ON public.sale_listings
    FOR SELECT
    USING (
        owner_id = auth.uid()
        OR public.is_marketplace_admin()
        OR (
            status IN ('available', 'reserved')
            AND EXISTS (
                SELECT 1
                FROM public.breeder_profiles profile
                WHERE profile.id = breeder_id
                  AND profile.verification_status = 'verified'
            )
        )
    );

DROP POLICY IF EXISTS "Breeders can create own sale listings" ON public.sale_listings;
CREATE POLICY "Breeders can create own sale listings"
    ON public.sale_listings
    FOR INSERT
    TO authenticated
    WITH CHECK (
        owner_id = auth.uid()
        AND status IN ('draft', 'pending')
        AND EXISTS (
            SELECT 1
            FROM public.breeder_profiles profile
            WHERE profile.id = breeder_id
              AND profile.user_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Breeders can update own sale listings" ON public.sale_listings;
CREATE POLICY "Breeders can update own sale listings"
    ON public.sale_listings
    FOR UPDATE
    TO authenticated
    USING (owner_id = auth.uid() OR public.is_marketplace_admin())
    WITH CHECK (
        public.is_marketplace_admin()
        OR (
            owner_id = auth.uid()
            AND status IN ('draft', 'pending', 'reserved', 'sold', 'archived')
        )
    );

DROP POLICY IF EXISTS "Breeders can delete own draft listings" ON public.sale_listings;
CREATE POLICY "Breeders can delete own draft listings"
    ON public.sale_listings
    FOR DELETE
    TO authenticated
    USING (
        public.is_marketplace_admin()
        OR (owner_id = auth.uid() AND status IN ('draft', 'rejected', 'archived'))
    );

GRANT SELECT ON public.breeder_profiles, public.sale_listings TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.breeder_profiles, public.sale_listings TO authenticated;
