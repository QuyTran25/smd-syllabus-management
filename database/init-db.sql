/* * INIT-DB.SQL (FINAL HARDENED)
 * Mục tiêu: Chuẩn bị môi trường Microservices - Security Hardening
 * Yêu cầu: Docker Image phải là 'pgvector/pgvector:pg16' (cho Point 3)
 */

-- 1. Enable Extensions
-- Lưu ý: Cần image hỗ trợ pgvector, nếu không sẽ lỗi dòng dưới
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- 2. Tạo Schemas (Physical Isolation)
CREATE SCHEMA IF NOT EXISTS core_service;
CREATE SCHEMA IF NOT EXISTS ai_service;

-- [FIX 2] REVOKE quyền mặc định từ PUBLIC (Security Hardening)
-- Đảm bảo chỉ user được cấp quyền mới thấy được schema
REVOKE ALL ON SCHEMA core_service FROM PUBLIC;
REVOKE ALL ON SCHEMA ai_service FROM PUBLIC;

-- 3. Tạo Roles (Mô phỏng Microservices Security)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'core_user') THEN
        -- [FIX 1] Note: Password này chỉ dùng cho Demo/Dev. 
        -- Trong Production, password sẽ được inject qua Secret Manager (Vault/AWS Secrets).
        CREATE ROLE core_user LOGIN PASSWORD 'core_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ai_user') THEN
        CREATE ROLE ai_user LOGIN PASSWORD 'ai_password';
    END IF;
END
$$;

-- 4. Cấp quyền sở hữu (Data Ownership)
ALTER SCHEMA core_service OWNER TO core_user;
ALTER SCHEMA ai_service OWNER TO ai_user;

-- Cấp quyền sử dụng cụ thể (Explicit Grant)
GRANT USAGE ON SCHEMA core_service TO core_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_service TO core_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_service TO core_user;

GRANT USAGE ON SCHEMA ai_service TO ai_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ai_service TO ai_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ai_service TO ai_user;

-- Set search_path để tránh lỗi Flyway
ALTER ROLE core_user SET search_path TO core_service, public;
ALTER ROLE ai_user SET search_path TO ai_service, public;

RAISE NOTICE '--- DB INFRASTRUCTURE INITIALIZED & SECURED ---';