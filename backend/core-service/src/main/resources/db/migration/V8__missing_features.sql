/*
 * V8__missing_features.sql
 * Mục tiêu: Bổ sung các chức năng còn thiếu & Đồng bộ Frontend
 * Updated: FIXED SYNTAX ERROR in VIEW & REFINED CONSTRAINTS
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
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_code VARCHAR(20) UNIQUE,
    curriculum_id UUID REFERENCES curriculums(id),
    enrollment_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lecturer_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plo_id UUID NOT NULL REFERENCES plos(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_code UNIQUE (plo_id, code)
);

CREATE TABLE IF NOT EXISTS clo_pi_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    clo_id UUID NOT NULL REFERENCES clos(id) ON DELETE CASCADE,
    pi_id UUID NOT NULL REFERENCES performance_indicators(id) ON DELETE CASCADE,
    level mapping_level NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clo_pi UNIQUE (clo_id, pi_id)
);

-- ==========================================
-- 9 & 10. PLO & TEACHING ASSIGNMENTS
-- ==========================================
ALTER TABLE plos ADD COLUMN IF NOT EXISTS category plo_category DEFAULT 'Knowledge';

CREATE TABLE IF NOT EXISTS teaching_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    academic_term_id UUID NOT NULL REFERENCES academic_terms(id),
    main_lecturer_id UUID NOT NULL REFERENCES users(id),
    deadline DATE NOT NULL,
    status assignment_status DEFAULT 'pending',
    syllabus_version_id UUID REFERENCES syllabus_versions(id),
    assigned_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_teaching_assignment UNIQUE (subject_id, academic_term_id),
    CONSTRAINT chk_assignment_completion CHECK (status != 'completed' OR syllabus_version_id IS NOT NULL)
);

-- ==========================================
-- 11. CO-LECTURERS
-- ==========================================
CREATE TABLE IF NOT EXISTS teaching_assignment_collaborators (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id UUID NOT NULL REFERENCES teaching_assignments(id) ON DELETE CASCADE,
    lecturer_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_assignment_collaborator UNIQUE (assignment_id, lecturer_id)
);

-- ==========================================
-- 12, 13, 14. LOGS & RECORDS
-- ==========================================
ALTER TABLE approval_history ADD COLUMN IF NOT EXISTS step_number INT, ADD COLUMN IF NOT EXISTS role_code VARCHAR(50);

CREATE TABLE IF NOT EXISTS publication_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id),
    published_by UUID NOT NULL REFERENCES users(id),
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    is_republish BOOLEAN DEFAULT FALSE,
    previous_publication_id UUID REFERENCES publication_records(id) ON DELETE SET NULL
);

ALTER TABLE syllabus_error_reports 
    ADD COLUMN IF NOT EXISTS type feedback_type DEFAULT 'ERROR',
    ADD COLUMN IF NOT EXISTS title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS section VARCHAR(100),
    ADD COLUMN IF NOT EXISTS admin_response TEXT,
    ADD COLUMN IF NOT EXISTS responded_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS responded_at TIMESTAMP;

-- ==========================================
-- 15. SEED DATA & 17. VIEW (FIXED)
-- ==========================================
INSERT INTO system_settings (key, value, description) 
VALUES ('max_clo_count', '{"value": 10}', 'Số lượng CLO tối đa'),
       ('min_clo_count', '{"value": 3}', 'Số lượng CLO tối thiểu'),
       ('grading_scale_default', '{"scale": "10", "pass_threshold": 5}', 'Thang điểm mặc định')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

-- Sửa lỗi: Thêm từ khóa "ON" vào phép JOIN faculties
CREATE OR REPLACE VIEW v_syllabus_full AS
SELECT sv.*, s.code AS subject_code, s.current_name_vi AS subject_name_vi,
       d.name AS department_name, f.name AS faculty_name
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id
JOIN departments d ON s.department_id = d.id
JOIN faculties f ON d.faculty_id = f.id
WHERE sv.is_deleted = FALSE;