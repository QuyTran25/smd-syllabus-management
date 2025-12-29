/*
 * V10__full_migration_checklist.sql
 * TỔNG HỢP TOÀN BỘ CHECKLIST HỆ THỐNG
 */

-- Chuyển ngữ cảnh làm việc vào schema dự án và public
SET search_path TO core_service, public;

-- ==========================================
-- 1. [PRINCIPAL] THÊM GIÁ TRỊ CHO ENUM syllabus_status
-- ==========================================
DO $$
BEGIN
    ALTER TYPE syllabus_status ADD VALUE 'APPROVED' AFTER 'PENDING_PRINCIPAL';
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$
BEGIN
    ALTER TYPE syllabus_status ADD VALUE 'REVISION_IN_PROGRESS' AFTER 'REJECTED';
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$
BEGIN
    ALTER TYPE syllabus_status ADD VALUE 'PENDING_HOD_REVISION' AFTER 'REVISION_IN_PROGRESS';
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$
BEGIN
    ALTER TYPE syllabus_status ADD VALUE 'PENDING_ADMIN_REPUBLISH' AFTER 'PENDING_HOD_REVISION';
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ==========================================
-- 2. ĐỊNH NGHĨA CÁC ENUM MỚI
-- ==========================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subject_type') THEN
        CREATE TYPE subject_type AS ENUM ('REQUIRED', 'ELECTIVE');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subject_component') THEN
        CREATE TYPE subject_component AS ENUM ('THEORY', 'PRACTICE', 'BOTH');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'actor_role_type') THEN
        CREATE TYPE actor_role_type AS ENUM ('HOD', 'AA', 'PRINCIPAL', 'ADMIN', 'LECTURER');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'audit_status') THEN
        CREATE TYPE audit_status AS ENUM ('SUCCESS', 'FAILED');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'error_report_section') THEN
        CREATE TYPE error_report_section AS ENUM (
            'subject_info', 'objectives', 'assessment_matrix', 
            'clo', 'clo_plo_matrix', 'textbook', 'reference', 'other'
        );
    END IF;
END $$;

-- ==========================================
-- 3. [AA] CẬP NHẬT CẤU TRÚC BẢNG subjects VÀ academic_terms
-- ==========================================
DO $$
BEGIN
    -- Thêm cột academic_year cho bảng academic_terms (QUAN TRỌNG: Để View không lỗi)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'academic_terms' AND column_name = 'academic_year') THEN
        ALTER TABLE academic_terms ADD COLUMN academic_year VARCHAR(20);
    END IF;

    -- Thêm các cột cho bảng subjects
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'subject_type') THEN
        ALTER TABLE subjects ADD COLUMN subject_type subject_type DEFAULT 'REQUIRED';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'component') THEN
        ALTER TABLE subjects ADD COLUMN component subject_component DEFAULT 'BOTH';
    END IF;

    -- Xử lý các cột giờ học (theory/practice/self-study)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_theory_hours') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'theory_hours') THEN
             ALTER TABLE subjects RENAME COLUMN theory_hours TO default_theory_hours;
        ELSE
             ALTER TABLE subjects ADD COLUMN default_theory_hours INT DEFAULT 0;
        END IF;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'recommended_term') THEN
        ALTER TABLE subjects ADD COLUMN recommended_term INT CHECK (recommended_term >= 1 AND recommended_term <= 10);
    END IF;
END $$;

-- ==========================================
-- 4. [ADMIN/LECTURER] CẬP NHẬT CÁC BẢNG KHÁC
-- ==========================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
ALTER TABLE approval_history ADD COLUMN IF NOT EXISTS actor_role actor_role_type;
ALTER TABLE teaching_assignments ADD COLUMN IF NOT EXISTS comments TEXT;

-- ==========================================
-- 5. CẬP NHẬT VIEW v_syllabus_full (Bản fix column exist)
-- ==========================================
DROP VIEW IF EXISTS v_syllabus_full;

CREATE VIEW v_syllabus_full AS
SELECT 
    sv.*,
    s.code AS subject_code, 
    s.current_name_vi AS subject_name_vi,
    s.subject_type,
    s.component,
    s.default_theory_hours AS subject_theory_hours,
    d.name AS department_name, 
    f.name AS faculty_name,
    at.code AS term_code,
    at.name AS term_name,
    at.academic_year -- Cột này đã được định nghĩa an toàn ở Bước 3
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id
JOIN departments d ON s.department_id = d.id
JOIN faculties f ON d.faculty_id = f.id
LEFT JOIN academic_terms at ON sv.academic_term_id = at.id
WHERE sv.is_deleted = FALSE;

-- ==========================================
-- 6. [STUDENT] CHUẨN HÓA SECTION BÁO LỖI
-- ==========================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'syllabus_error_reports' AND column_name = 'section') THEN
        -- Ép kiểu dữ liệu từ TEXT sang ENUM an toàn
        ALTER TABLE syllabus_error_reports 
        ALTER COLUMN section TYPE error_report_section 
        USING (section::error_report_section);
    ELSE
        ALTER TABLE syllabus_error_reports ADD COLUMN section error_report_section DEFAULT 'other';
    END IF;
END $$;

-- Log hoàn tất
DO $$ BEGIN RAISE NOTICE 'V10 Migration Completed Successfully.'; END $$;