-- V12__add_timestamps_to_roles.sql
-- Add created_at and updated_at columns to roles table and trigger

ALTER TABLE core_service.roles
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- create or replace update_timestamp() function if not exists (idempotent)
CREATE OR REPLACE FUNCTION core_service.update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- attach trigger to roles table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'update_roles_time') THEN
        CREATE TRIGGER update_roles_time
        BEFORE UPDATE ON core_service.roles
        FOR EACH ROW EXECUTE FUNCTION core_service.update_timestamp();
    END IF;
END$$;
