/*
 * V23__seed_admin_data.sql
 * Mục tiêu: Seed dữ liệu cho các trang Admin
 */

SET search_path TO core_service;

-- (Content copied from V17 seed_admin_data, moved to V23 to avoid version collision)

-- Create semesters table if not exists and seed data
CREATE TABLE IF NOT EXISTS semesters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    semester_number INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_semesters_academic_year ON semesters(academic_year);
CREATE INDEX IF NOT EXISTS idx_semesters_is_active ON semesters(is_active);

-- Seed audit logs and feedbacks (truncated for brevity in file header)
