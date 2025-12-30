/* * INIT-DB.SQL (SMD HARDENED)
 * Mục tiêu: Tự động tạo User/Schema khớp với Code Java
 * Yêu cầu: Docker Image hỗ trợ pgvector (nếu dùng AI sau này)
 */

-- 1. Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- 2. Tạo Schemas
CREATE SCHEMA IF NOT EXISTS core_service;

-- [SECURITY] REVOKE quyền mặc định từ PUBLIC
REVOKE ALL ON SCHEMA core_service FROM PUBLIC;

-- 3. Tạo Roles (Dùng smd_user để khớp với cấu hình Java)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'smd_user') THEN
        CREATE ROLE smd_user LOGIN PASSWORD 'smd_password';
    END IF;
END
$$;

-- 4. Cấp quyền sở hữu (Data Ownership)
ALTER SCHEMA core_service OWNER TO smd_user;

-- 5. Cấp quyền sử dụng cụ thể
GRANT USAGE ON SCHEMA core_service TO smd_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_service TO smd_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_service TO smd_user;

-- 6. Set search_path (Quan trọng để Flyway migrate đúng vào schema core_service)
ALTER ROLE smd_user SET search_path TO core_service, public;

-- Log xác nhận khởi tạo thành công
DO $$ BEGIN RAISE NOTICE '--- SMD DB INFRASTRUCTURE INITIALIZED & SECURED ---'; END $$;