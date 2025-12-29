/* * INIT-DB.SQL (MERGED, SMD HARDENED)
 * Goal: initialize DB schemas/roles used by core-service and optionally ai_service.
 * Note: Extension "vector" requires a postgres image that supports pgvector.
 */

-- 1. Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- 2. Create Schemas
CREATE SCHEMA IF NOT EXISTS core_service;
CREATE SCHEMA IF NOT EXISTS ai_service;

-- Security: revoke default PUBLIC privileges on schemas
REVOKE ALL ON SCHEMA core_service FROM PUBLIC;
REVOKE ALL ON SCHEMA ai_service FROM PUBLIC;

-- 3. Create Roles (dev passwords; replace with secrets in production)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'smd_user') THEN
        CREATE ROLE smd_user LOGIN PASSWORD 'smd_password';
    END IF;

    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'core_user') THEN
        CREATE ROLE core_user LOGIN PASSWORD 'core_password';
    END IF;

    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ai_user') THEN
        CREATE ROLE ai_user LOGIN PASSWORD 'ai_password';
    END IF;
END
$$;

-- 4. Set schema ownerships
-- Prefer `smd_user` as owner for core_service to match application.yml in core-service.
ALTER SCHEMA core_service OWNER TO smd_user;
ALTER SCHEMA ai_service OWNER TO ai_user;

-- 5. Grants for core_service
GRANT USAGE ON SCHEMA core_service TO smd_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_service TO smd_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_service TO smd_user;

-- Also allow core_user (if present) to operate on core_service for compatibility
GRANT USAGE ON SCHEMA core_service TO core_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_service TO core_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_service TO core_user;

-- 6. Grants for ai_service
GRANT USAGE ON SCHEMA ai_service TO ai_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ai_service TO ai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ai_service TO ai_user;

-- 7. Set search_path for roles (helps Flyway/JPAs)
ALTER ROLE smd_user SET search_path TO core_service, public;
ALTER ROLE core_user SET search_path TO core_service, public;
ALTER ROLE ai_user SET search_path TO ai_service, public;

DO $$ BEGIN RAISE NOTICE '--- SMD DB INFRASTRUCTURE INITIALIZED & SECURED ---'; END $$;
