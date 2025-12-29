-- Flyway migration: create notifications table with required columns
-- This creates the table if it does not already exist. It also attempts
-- to create the uuid extension if available (harmless if already present).

-- Note: If your environment does not allow creating extensions, remove
-- the CREATE EXTENSION line and ensure the application supplies UUIDs.

CREATE SCHEMA IF NOT EXISTS core_service;

-- Create uuid extension if possible (uuid_generate_v4)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'uuid-ossp') THEN
        BEGIN
            PERFORM pg_catalog.set_config('search_path', 'public', false);
            EXECUTE 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp"';
        EXCEPTION WHEN OTHERS THEN
            -- ignore if cannot create extension (no superuser)
            RAISE NOTICE 'Could not create uuid-ossp extension: %', SQLERRM;
        END;
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS core_service.notifications (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid,
    title varchar(255),
    message text,
    type varchar(50),
    payload jsonb,
    is_read boolean DEFAULT false,
    read_at timestamptz,
    related_entity_id uuid,
    related_entity_type varchar(50),
    created_at timestamptz DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notif_user_created ON core_service.notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_related_entity ON core_service.notifications(related_entity_type, related_entity_id);
