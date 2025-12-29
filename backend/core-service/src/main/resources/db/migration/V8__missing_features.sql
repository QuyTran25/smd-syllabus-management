/*
 * V8__missing_features.sql
 * Mục tiêu: Bổ sung các chức năng còn thiếu & Đồng bộ Frontend
 * Đã fix: Thống nhất dùng gen_random_uuid() và thêm IF NOT EXISTS để an toàn hơn
 */

-- Chuyển ngữ cảnh làm việc vào schema dự án và public
SET search_path TO core_service, public;

-- ==========================================
-- 0. ENUMS BỔ SUNG
-- ==========================================
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'course_type') THEN
        CREATE TYPE course_type AS ENUM ('required', 'elective', 'free');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'component_type') THEN
        CREATE TYPE component_type AS ENUM ('major', 'foundation', 'general', 'thesis');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'plo_category') THEN
        CREATE TYPE plo_category AS ENUM ('Knowledge', 'Skills', 'Competence', 'Attitude');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'feedback_type') THEN
        CREATE TYPE feedback_type AS ENUM ('ERROR', 'SUGGESTION', 'QUESTION', 'OTHER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'feedback_status') THEN
        CREATE TYPE feedback_status AS ENUM ('PENDING', 'IN_REVIEW', 'RESOLVED', 'REJECTED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'assignment_status') THEN
        CREATE TYPE assignment_status AS ENUM ('pending', 'in-progress', 'submitted', 'completed');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'mapping_level') THEN
        CREATE TYPE mapping_level AS ENUM ('H', 'M', 'L');
    END IF;
END $$;

-- ==========================================
-- 1. SYLLABUS SUBSCRIPTIONS
-- ==========================================
CREATE TABLE IF NOT EXISTS syllabus_subscriptions (
    -- MERGE: Dùng gen_random_uuid() theo Team
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    syllabus_version_id UUID REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subscription UNIQUE (user_id, subject_id, syllabus_version_id)
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user ON syllabus_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_subject ON syllabus_subscriptions(subject_id);

-- Trigger cập nhật updated_at
CREATE OR REPLACE TRIGGER update_subscriptions_time 
BEFORE UPDATE ON syllabus_subscriptions 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 2. BỔ SUNG CỘT CHO SYLLABUS_VERSIONS
-- ==========================================
ALTER TABLE syllabus_versions 
    ADD COLUMN IF NOT EXISTS effective_date DATE,
    ADD COLUMN IF NOT EXISTS unpublished_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS unpublished_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS unpublish_reason TEXT,
    ADD COLUMN IF NOT EXISTS is_edit_enabled BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS edit_enabled_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS edit_enabled_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS workflow_id UUID REFERENCES approval_workflows(id),
    ADD COLUMN IF NOT EXISTS current_approval_step INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS course_type course_type DEFAULT 'required',
    ADD COLUMN IF NOT EXISTS component_type component_type DEFAULT 'major',
    ADD COLUMN IF NOT EXISTS theory_hours INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS practice_hours INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS self_study_hours INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS grading_scale_id UUID REFERENCES grading_scales(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS student_duties TEXT,
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS hod_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS hod_approved_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS aa_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS aa_approved_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS principal_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS principal_approved_by UUID REFERENCES users(id);

-- Constraint kiểm tra logic
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_edit_enabled') THEN
        ALTER TABLE syllabus_versions ADD CONSTRAINT chk_edit_enabled 
        CHECK (is_edit_enabled = FALSE OR (edit_enabled_by IS NOT NULL AND edit_enabled_at IS NOT NULL));
    END IF;
END $$;

-- ==========================================
-- 3. TRỌNG SỐ CLO
-- ==========================================
ALTER TABLE clos ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) DEFAULT 0 CHECK (weight >= 0 AND weight <= 100);

-- ==========================================
-- 4. ASSESSMENT-CLO MAPPING
-- ==========================================
CREATE TABLE IF NOT EXISTS assessment_clo_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_scheme_id UUID NOT NULL REFERENCES assessment_schemes(id) ON DELETE CASCADE,
    clo_id UUID NOT NULL REFERENCES clos(id) ON DELETE CASCADE,
    contribution_percent DECIMAL(5,2) DEFAULT 100 CHECK (contribution_percent >= 0 AND contribution_percent <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_assessment_clo UNIQUE (assessment_scheme_id, clo_id)
);

CREATE OR REPLACE TRIGGER update_assess_clo_time 
BEFORE UPDATE ON assessment_clo_mappings 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 5 & 6. PROFILES
-- ==========================================
CREATE TABLE IF NOT EXISTS student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_code VARCHAR(20) UNIQUE,
    curriculum_id UUID REFERENCES curriculums(id),
    enrollment_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lecturer_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    lecturer_code VARCHAR(20) UNIQUE,
    department_id UUID REFERENCES departments(id),
    title VARCHAR(100),
    specialization TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 7 & 8. PERFORMANCE INDICATORS (PI)
-- ==========================================
CREATE TABLE IF NOT EXISTS performance_indicators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plo_id UUID NOT NULL REFERENCES plos(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_code UNIQUE (plo_id,