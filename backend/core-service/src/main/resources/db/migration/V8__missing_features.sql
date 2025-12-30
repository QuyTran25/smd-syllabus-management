/*
 * V8__missing_features.sql
 * Mục tiêu: Bổ sung các chức năng còn thiếu & Đồng bộ Frontend
 * Updated: FIX SYNTAX ERROR & ADD MISSING INDEXES
 */

SET search_path TO core_service;

-- ==========================================
-- 0. ENUMS BỔ SUNG
-- ==========================================
CREATE TYPE course_type AS ENUM ('required', 'elective', 'free');
CREATE TYPE component_type AS ENUM ('major', 'foundation', 'general', 'thesis');
CREATE TYPE plo_category AS ENUM ('Knowledge', 'Skills', 'Competence', 'Attitude');
CREATE TYPE feedback_type AS ENUM ('ERROR', 'SUGGESTION', 'QUESTION', 'OTHER');
CREATE TYPE feedback_status AS ENUM ('PENDING', 'IN_REVIEW', 'RESOLVED', 'REJECTED');
CREATE TYPE assignment_status AS ENUM ('pending', 'in-progress', 'submitted', 'completed');
CREATE TYPE mapping_level AS ENUM ('H', 'M', 'L');

-- ==========================================
-- 1. SYLLABUS SUBSCRIPTIONS
-- ==========================================
CREATE TABLE syllabus_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    syllabus_version_id UUID REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subscription UNIQUE (user_id, subject_id, syllabus_version_id)
);

CREATE INDEX idx_subscriptions_user ON syllabus_subscriptions(user_id);
CREATE INDEX idx_subscriptions_subject ON syllabus_subscriptions(subject_id);
CREATE TRIGGER update_subscriptions_time BEFORE UPDATE ON syllabus_subscriptions FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 2. BỔ SUNG CỘT CHO SYLLABUS_VERSIONS
-- ==========================================
-- [FIX 1] Gộp các lệnh ADD COLUMN vào chung các câu lệnh ALTER TABLE hợp lệ, ngắt bằng dấu chấm phẩy

-- Nhóm 1: Post-publication & Workflow
ALTER TABLE syllabus_versions 
    ADD COLUMN effective_date DATE,
    ADD COLUMN unpublished_at TIMESTAMP,
    ADD COLUMN unpublished_by UUID REFERENCES users(id),
    ADD COLUMN unpublish_reason TEXT,
    ADD COLUMN is_edit_enabled BOOLEAN DEFAULT FALSE,
    ADD COLUMN edit_enabled_by UUID REFERENCES users(id),
    ADD COLUMN edit_enabled_at TIMESTAMP,
    ADD COLUMN workflow_id UUID REFERENCES approval_workflows(id),
    ADD COLUMN current_approval_step INT DEFAULT 0;

-- Nhóm 2: Logic Constraint
ALTER TABLE syllabus_versions
    ADD CONSTRAINT chk_edit_enabled 
    CHECK (is_edit_enabled = FALSE OR (edit_enabled_by IS NOT NULL AND edit_enabled_at IS NOT NULL));

-- Nhóm 3: Frontend Detail Fields (FIXED SYNTAX)
ALTER TABLE syllabus_versions
    ADD COLUMN course_type course_type DEFAULT 'required',
    ADD COLUMN component_type component_type DEFAULT 'major',
    ADD COLUMN theory_hours INT DEFAULT 0,
    ADD COLUMN practice_hours INT DEFAULT 0,
    ADD COLUMN self_study_hours INT DEFAULT 0,
    ADD COLUMN grading_scale_id UUID REFERENCES grading_scales(id) ON DELETE SET NULL,
    ADD COLUMN student_duties TEXT;

-- ==========================================
-- 3. TRỌNG SỐ CLO
-- ==========================================
ALTER TABLE clos 
    ADD COLUMN weight DECIMAL(5,2) DEFAULT 0 CHECK (weight >= 0 AND weight <= 100);

-- ==========================================
-- 4. ASSESSMENT-CLO MAPPING
-- ==========================================
CREATE TABLE assessment_clo_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_scheme_id UUID NOT NULL REFERENCES assessment_schemes(id) ON DELETE CASCADE,
    clo_id UUID NOT NULL REFERENCES clos(id) ON DELETE CASCADE,
    contribution_percent DECIMAL(5,2) DEFAULT 100 CHECK (contribution_percent >= 0 AND contribution_percent <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_assessment_clo UNIQUE (assessment_scheme_id, clo_id)
);

CREATE INDEX idx_assess_clo_assessment ON assessment_clo_mappings(assessment_scheme_id);
CREATE TRIGGER update_assess_clo_time BEFORE UPDATE ON assessment_clo_mappings FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 5. & 6. PROFILES
-- ==========================================
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_code VARCHAR(20) UNIQUE,
    curriculum_id UUID REFERENCES curriculums(id),
    enrollment_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_student_curriculum ON student_profiles(curriculum_id);
CREATE INDEX idx_student_code ON student_profiles(student_code);
CREATE TRIGGER update_student_profile_time BEFORE UPDATE ON student_profiles FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TABLE lecturer_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    lecturer_code VARCHAR(20) UNIQUE,
    department_id UUID REFERENCES departments(id),
    title VARCHAR(100),
    specialization TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_lecturer_department ON lecturer_profiles(department_id);
CREATE INDEX idx_lecturer_code ON lecturer_profiles(lecturer_code);
CREATE TRIGGER update_lecturer_profile_time BEFORE UPDATE ON lecturer_profiles FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 7. PERFORMANCE INDICATORS (PI)
-- ==========================================
CREATE TABLE performance_indicators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plo_id UUID NOT NULL REFERENCES plos(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_code UNIQUE (plo_id, code)
);
CREATE INDEX idx_pi_plo ON performance_indicators(plo_id);
CREATE TRIGGER update_pi_time BEFORE UPDATE ON performance_indicators FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 8. CLO-PI MAPPINGS
-- ==========================================
CREATE TABLE clo_pi_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clo_id UUID NOT NULL REFERENCES clos(id) ON DELETE CASCADE,
    pi_id UUID NOT NULL REFERENCES performance_indicators(id) ON DELETE CASCADE,
    level mapping_level NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clo_pi UNIQUE (clo_id, pi_id)
);
CREATE INDEX idx_clo_pi_clo ON clo_pi_mappings(clo_id);
CREATE TRIGGER update_clo_pi_time BEFORE UPDATE ON clo_pi_mappings FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 9. BỔ SUNG CHO PLO
-- ==========================================
ALTER TABLE plos ADD COLUMN category plo_category DEFAULT 'Knowledge';

-- ==========================================
-- 10. TEACHING ASSIGNMENTS
-- ==========================================
CREATE TABLE teaching_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

-- [FIX 2] Bổ sung Index quan trọng cho Teaching Assignments
CREATE INDEX idx_teaching_subject ON teaching_assignments(subject_id);
CREATE INDEX idx_teaching_term ON teaching_assignments(academic_term_id);
CREATE INDEX idx_teaching_lecturer ON teaching_assignments(main_lecturer_id);
CREATE TRIGGER update_assignment_time BEFORE UPDATE ON teaching_assignments FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 11. CO-LECTURERS
-- ==========================================
CREATE TABLE teaching_assignment_collaborators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES teaching_assignments(id) ON DELETE CASCADE,
    lecturer_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_assignment_collaborator UNIQUE (assignment_id, lecturer_id)
);
CREATE TRIGGER update_collab_time BEFORE UPDATE ON teaching_assignment_collaborators FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 12. BỔ SUNG APPROVAL HISTORY
-- ==========================================
ALTER TABLE approval_history 
    ADD COLUMN step_number INT,
    ADD COLUMN role_code VARCHAR(50);

-- ==========================================
-- 13. PUBLICATION RECORDS
-- ==========================================
CREATE TABLE publication_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id),
    published_by UUID NOT NULL REFERENCES users(id),
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    is_republish BOOLEAN DEFAULT FALSE,
    previous_publication_id UUID REFERENCES publication_records(id) ON DELETE SET NULL
);

-- [FIX 3] Bổ sung Index cho Publication Records
CREATE INDEX idx_publication_syllabus ON publication_records(syllabus_version_id);
CREATE INDEX idx_publication_time ON publication_records(published_at); -- Hỗ trợ sort theo thời gian

-- ==========================================
-- 14. SYLLABUS ERROR REPORTS
-- ==========================================
ALTER TABLE syllabus_error_reports 
    ADD COLUMN type feedback_type DEFAULT 'ERROR',
    ADD COLUMN title VARCHAR(255),
    ADD COLUMN section VARCHAR(100),
    ADD COLUMN admin_response TEXT,
    ADD COLUMN responded_by UUID REFERENCES users(id),
    ADD COLUMN responded_at TIMESTAMP;

-- ==========================================
-- 15. SEED DATA
-- ==========================================
INSERT INTO system_settings (key, value, description) VALUES
('max_clo_count', '{"value": 10}', 'Số lượng CLO tối đa'),
('min_clo_count', '{"value": 3}', 'Số lượng CLO tối thiểu'),
('grading_scale_default', '{"scale": "10", "pass_threshold": 5}', 'Thang điểm mặc định')
ON CONFLICT (key) DO NOTHING;

-- ==========================================
-- 16. APPROVAL TRACKING COLUMNS
-- ==========================================
ALTER TABLE syllabus_versions 
    ADD COLUMN submitted_at TIMESTAMP,
    ADD COLUMN hod_approved_at TIMESTAMP,
    ADD COLUMN hod_approved_by UUID REFERENCES users(id),
    ADD COLUMN aa_approved_at TIMESTAMP,
    ADD COLUMN aa_approved_by UUID REFERENCES users(id),
    ADD COLUMN principal_approved_at TIMESTAMP,
    ADD COLUMN principal_approved_by UUID REFERENCES users(id);

-- ==========================================
-- 17. VIEW
-- ==========================================
CREATE OR REPLACE VIEW v_syllabus_full AS
SELECT 
    sv.*,
    s.code AS subject_code, s.current_name_vi AS subject_name_vi,
    d.name AS department_name, f.name AS faculty_name
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id
JOIN departments d ON s.department_id = d.id
JOIN faculties f ON d.faculty_id = f.id
WHERE sv.is_deleted = FALSE;