-- Flyway migration: add missing notification columns used by JPA entity
-- Adds: read_at, related_entity_id, related_entity_type

CREATE SCHEMA IF NOT EXISTS core_service;

SET search_path TO core_service;

-- Add read_at timestamp for marking when a notification was read
ALTER TABLE core_service.notifications
    ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;

-- Add optional related entity references (frontend may navigate using these)
ALTER TABLE core_service.notifications
    ADD COLUMN IF NOT EXISTS related_entity_id UUID;

ALTER TABLE core_service.notifications
    ADD COLUMN IF NOT EXISTS related_entity_type VARCHAR(50);

-- (Optional) ensure indexes for common lookups; avoid duplicate index creation
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_notif_related_entity'
    ) THEN
        CREATE INDEX idx_notif_related_entity ON core_service.notifications(related_entity_type, related_entity_id);
    END IF;
END$$;
