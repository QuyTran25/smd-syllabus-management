/*
 * V10__full_migration_checklist.sql
 * TỔNG HỢP TOÀN BỘ CHECKLIST:
 * 1. [Principal/Admin]: Status mới, actor_role, audit_logs, users info.
 * 2. [AA]: Chuẩn hóa subjects (description, recommended_term, type, component, default hours).
 * 3. [HOD/Lecturer]: Ghi chú phân công (comments).
 * 4. [Student]: Chuẩn hóa phân loại lỗi (error_report_section).
 * UPDATED: Thêm DROP VIEW để tránh lỗi 42P16 và kiểm tra cột description.
 */

SET search_path TO core_service;

-- ==========================================
-- 1. [PRINCIPAL] THÊM GIÁ TRỊ CHO ENUM syllabus_status
-- ==========================================
ALTER TYPE syllabus_status ADD VALUE IF NOT EXISTS 'APPROVED' AFTER 'PENDING_PRINCIPAL';
ALTER TYPE syllabus_status ADD VALUE IF NOT EXISTS 'REVISION_IN_PROGRESS' AFTER 'REJECTED';
ALTER TYPE syllabus_status ADD VALUE IF NOT EXISTS 'PENDING_HOD_REVISION' AFTER 'REVISION_IN_PROGRESS';
ALTER TYPE syllabus_status ADD VALUE IF NOT EXISTS 'PENDING_ADMIN_REPUBLISH' AFTER 'PENDING_HOD_REVISION';

-- ==========================================
-- 2. ENUM MỚI HỆ THỐNG
-- ==========================================
DO $$
BEGIN
    -- [AA] Loại môn học
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subject_type') THEN
        CREATE TYPE subject_type AS ENUM ('REQUIRED', 'ELECTIVE');
    END IF;
    
    -- [AA] Cấu phần môn học
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subject_component') THEN
        CREATE TYPE subject_component AS ENUM ('THEORY', 'PRACTICE', 'BOTH');
    END IF;
    
    -- [PRINCIPAL] Vai trò người duyệt
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'actor_role_type') THEN
        CREATE TYPE actor_role_type AS ENUM ('HOD', 'AA', 'PRINCIPAL', 'ADMIN', 'LECTURER');
    END IF;
    
    -- [ADMIN] Trạng thái Audit
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'audit_status') THEN
        CREATE TYPE audit_status AS ENUM ('SUCCESS', 'FAILED');
    END IF;

    -- [STUDENT] Phân loại lỗi báo cáo
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'error_report_section') THEN
        CREATE TYPE error_report_section AS ENUM (
            'subject_info',       -- Thông tin môn học
            'objectives',         -- Mục tiêu học phần
            'assessment_matrix',  -- Ma trận đánh giá
            'clo',                -- Chuẩn đầu ra học phần
            'clo_plo_matrix',     -- Ma trận CLO-PLO
            'textbook',           -- Giáo trình
            'reference',          -- Tài liệu tham khảo
            'other'               -- Khác
        );
    END IF;
END $$;

-- ==========================================
-- 3. [AA] BỔ SUNG CỘT CHO BẢNG subjects
-- ==========================================
DO $$
BEGIN
    -- subject_type
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'subject_type') THEN
        ALTER TABLE subjects ADD COLUMN subject_type subject_type DEFAULT 'REQUIRED';
    END IF;
    
    -- component
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'component') THEN
        ALTER TABLE subjects ADD COLUMN component subject_component DEFAULT 'BOTH';
    END IF;
    
    -- default_theory_hours
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_theory_hours') THEN
        ALTER TABLE subjects ADD COLUMN default_theory_hours INT DEFAULT 0;
    END IF;
    
    -- default_practice_hours
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_practice_hours') THEN
        ALTER TABLE subjects ADD COLUMN default_practice_hours INT DEFAULT 0;
    END IF;
    
    -- default_self_study_hours
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_self_study_hours') THEN
        ALTER TABLE subjects ADD COLUMN default_self_study_hours INT DEFAULT 0;
    END IF;
    
    -- description (Kiểm tra kỹ để tránh lỗi V9)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'description') THEN
        ALTER TABLE subjects ADD COLUMN description TEXT;
    END IF;
    
    -- recommended_term
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'recommended_term') THEN
        ALTER TABLE subjects ADD COLUMN recommended_term INT CHECK (recommended_term >= 1 AND recommended_term <= 10);
    END IF;
END $$;

-- ==========================================
-- 4. BỔ SUNG CỘT CHO BẢNG syllabus_versions
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'syllabus_versions' AND column_name = 'description') THEN
        ALTER TABLE syllabus_versions ADD COLUMN description TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'syllabus_versions' AND column_name = 'objectives') THEN
        ALTER TABLE syllabus_versions ADD COLUMN objectives TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'syllabus_versions' AND column_name = 'student_tasks') THEN
        ALTER TABLE syllabus_versions ADD COLUMN student_tasks TEXT;
    END IF;
END $$;

-- ==========================================
-- 5. BỔ SUNG CỘT CHO BẢNG academic_terms
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'academic_terms' AND column_name = 'academic_year') THEN
        ALTER TABLE academic_terms ADD COLUMN academic_year VARCHAR(20);
    END IF;
END $$;

-- ==========================================
-- 6. [ADMIN] BỔ SUNG CỘT CHO BẢNG users
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'users' AND column_name = 'is_active') THEN
        ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'users' AND column_name = 'last_login') THEN
        ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'users' AND column_name = 'created_by') THEN
        ALTER TABLE users ADD COLUMN created_by UUID REFERENCES users(id);
    END IF;
END $$;

-- ==========================================
-- 7. [PRINCIPAL] BỔ SUNG CỘT CHO BẢNG approval_history
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'approval_history' AND column_name = 'actor_role') THEN
        ALTER TABLE approval_history ADD COLUMN actor_role actor_role_type;
    END IF;
END $$;

-- ==========================================
-- 8. [ADMIN] BỔ SUNG CỘT CHO BẢNG audit_logs
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'audit_logs' AND column_name = 'description') THEN
        ALTER TABLE audit_logs ADD COLUMN description TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'core_service' AND table_name = 'audit_logs' AND column_name = 'status') THEN
        ALTER TABLE audit_logs ADD COLUMN status audit_status DEFAULT 'SUCCESS';
    END IF;
END $$;

-- ==========================================
-- 11. INDEXES BỔ SUNG
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_subjects_type ON subjects(subject_type);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_approval_actor_role ON approval_history(actor_role);
CREATE INDEX IF NOT EXISTS idx_academic_terms_year ON academic_terms(academic_year);
CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs(status);

-- ==========================================
-- 12. CẬP NHẬT VIEW v_syllabus_full
-- ==========================================
-- ⭐ BẮT BUỘC DROP VIEW TRƯỚC KHI TẠO LẠI ĐỂ TRÁNH LỖI CẤU TRÚC (42P16)
DROP VIEW IF EXISTS v_syllabus_full;

CREATE VIEW v_syllabus_full AS
SELECT 
    sv.*,
    s.code AS subject_code, 
    s.current_name_vi AS subject_name_vi,
    s.subject_type,
    s.component,
    s.default_theory_hours AS subject_theory_hours,
    s.default_practice_hours AS subject_practice_hours,
    s.default_self_study_hours AS subject_self_study_hours,
    d.name AS department_name, 
    d.code AS department_code,
    f.name AS faculty_name,
    f.code AS faculty_code,
    at.code AS term_code,
    at.name AS term_name,
    at.academic_year
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id
JOIN departments d ON s.department_id = d.id
JOIN faculties f ON d.faculty_id = f.id
LEFT JOIN academic_terms at ON sv.academic_term_id = at.id
WHERE sv.is_deleted = FALSE;

-- ==========================================
-- 13. [STUDENT] CHUẨN HÓA SECTION BÁO LỖI
-- ==========================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'core_service' AND table_name = 'syllabus_error_reports' AND column_name = 'section') THEN
        
        IF (SELECT data_type FROM information_schema.columns 
            WHERE table_schema = 'core_service' AND table_name = 'syllabus_error_reports' AND column_name = 'section') != 'USER-DEFINED' THEN
            
            ALTER TABLE syllabus_error_reports 
            ALTER COLUMN section TYPE error_report_section 
            USING (CASE 
                WHEN section::text = 'subject_info' THEN 'subject_info'::error_report_section
                WHEN section::text = 'objectives' THEN 'objectives'::error_report_section
                WHEN section::text = 'assessment_matrix' THEN 'assessment_matrix'::error_report_section
                WHEN section::text = 'clo' THEN 'clo'::error_report_section
                WHEN section::text = 'clo_plo_matrix' THEN 'clo_plo_matrix'::error_report_section
                WHEN section::text = 'textbook' THEN 'textbook'::error_report_section
                WHEN section::text = 'reference' THEN 'reference'::error_report_section
                ELSE 'other'::error_report_section 
            END);
        END IF;
    ELSE
        ALTER TABLE syllabus_error_reports ADD COLUMN section error_report_section DEFAULT 'other';
    END IF;
END $$;

-- ==========================================
-- LOG COMPLETION
-- ==========================================
DO $$
BEGIN 
    RAISE NOTICE 'V10 Migration Completed Successfully.';
END $$;