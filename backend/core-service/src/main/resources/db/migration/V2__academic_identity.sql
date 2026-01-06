/*
 * V2__academic_identity.sql
 * ĐÃ SỬA: Thêm cột description và dùng gen_random_uuid() chuẩn
 */

SET search_path TO core_service;

-- Enum quan hệ môn học
CREATE TYPE subject_relation_type AS ENUM ('PREREQUISITE', 'CO_REQUISITE', 'REPLACEMENT');

-- ==========================================
-- 1. CURRICULUMS (Chương trình đào tạo)
-- ==========================================
CREATE TABLE curriculums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    faculty_id UUID REFERENCES faculties(id),
    total_credits INT NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

CREATE INDEX idx_curriculums_faculty ON curriculums(faculty_id);
CREATE TRIGGER update_curriculums_time BEFORE UPDATE ON curriculums FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 2. SUBJECTS (Bản thể Môn học - Identity)
-- ==========================================
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    department_id UUID NOT NULL REFERENCES departments(id),
    
    -- [NEW] Liên kết Subject với Curriculum
    curriculum_id UUID REFERENCES curriculums(id) ON DELETE SET NULL,
    
    current_name_vi VARCHAR(255) NOT NULL,
    current_name_en VARCHAR(255),
    
    -- ⭐ QUAN TRỌNG: Đã thêm cột này để V9 không bị lỗi
    description TEXT, 
    
    default_credits INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

CREATE INDEX idx_subjects_department ON subjects(department_id);
CREATE INDEX idx_subjects_curriculum ON subjects(curriculum_id);
CREATE INDEX idx_subjects_active ON subjects(is_active);
CREATE TRIGGER update_subjects_time BEFORE UPDATE ON subjects FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 3. RELATIONSHIPS (Tiên quyết / Song hành)
-- ==========================================
CREATE TABLE subject_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    related_subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    type subject_relation_type NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    
    UNIQUE(subject_id, related_subject_id, type),
    CONSTRAINT chk_no_self_reference CHECK (subject_id <> related_subject_id)
);

CREATE INDEX idx_subject_rel_subject ON subject_relationships(subject_id);
CREATE INDEX idx_subject_rel_related ON subject_relationships(related_subject_id);