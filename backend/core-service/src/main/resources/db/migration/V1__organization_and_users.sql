/*
 * V1__organization_and_users.sql
 * Mục tiêu: Quản lý Cơ cấu tổ chức & Người dùng (RBAC)
 * Đã fix: Thống nhất dùng gen_random_uuid() theo chuẩn mới của Team
 */

-- 0. KHỞI TẠO MÔI TRƯỜNG
CREATE SCHEMA IF NOT EXISTS core_service;

-- Chuyển ngữ cảnh làm việc vào schema của dự án
SET search_path TO core_service, public;

-- ==========================================
-- 1. ENUMS
-- ==========================================
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BANNED');
CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE', 'MICROSOFT');

-- ==========================================
-- 2. ORGANIZATION STRUCTURE (Khoa - Bộ môn)
-- ==========================================

-- Faculties (Khoa)
CREATE TABLE faculties (
    -- MERGE: Dùng gen_random_uuid() của Team để tối ưu hiệu năng
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE, -- VD: CNTT
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Departments (Bộ môn)
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    faculty_id UUID NOT NULL REFERENCES faculties(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL UNIQUE, 
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. USERS (Người dùng)
-- ==========================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Định danh
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) UNIQUE,
    
    -- Authentication (Hybrid)
    password_hash VARCHAR(255), 
    auth_provider auth_provider DEFAULT 'LOCAL',
    provider_id VARCHAR(255), 
    
    -- Thông tin cá nhân
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    gender gender_type,
    avatar_url TEXT,
    
    -- Trạng thái
    status user_status DEFAULT 'ACTIVE',
    
    -- Organization Mapping
    faculty_id UUID REFERENCES faculties(id),
    department_id UUID REFERENCES departments(id),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 4. RBAC (Phân quyền)
-- ==========================================

-- Roles (Vai trò hệ thống)
CREATE TABLE roles (
    -- MERGE: Thống nhất dùng gen_random_uuid()
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE, -- VD: ADMIN, DEAN, LECTURER
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE 
);

-- User Roles (Mapping User - Role theo phạm vi)
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    
    -- Scope: GLOBAL, FACULTY, DEPARTMENT
    scope_type VARCHAR(20) CHECK (scope_type IN ('GLOBAL', 'FACULTY', 'DEPARTMENT')),
    scope_id UUID, 
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id, scope_type, scope_id)
);

-- ==========================================
-- 5. TRIGGER & INDEXES
-- ==========================================
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_faculties_time BEFORE UPDATE ON faculties FOR EACH ROW EXECUTE FUNCTION update_timestamp();
CREATE TRIGGER update_departments_time BEFORE UPDATE ON departments FOR EACH ROW EXECUTE FUNCTION update_timestamp();
CREATE TRIGGER update_users_time BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Index để tối ưu truy vấn
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_faculty ON users(faculty_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);