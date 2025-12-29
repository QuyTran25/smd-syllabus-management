-- V13__sync_password_column.sql
-- Ensure users.password column exists and sync values from password_hash

ALTER TABLE core_service.users
    ADD COLUMN IF NOT EXISTS password VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'core_service' AND table_name = 'users' AND column_name = 'password_hash'
    ) THEN
        EXECUTE 'UPDATE core_service.users SET password = password_hash WHERE password IS NULL AND password_hash IS NOT NULL';
    END IF;
END$$;

-- keep password_hash for backward compatibility; do not drop to avoid surprises
