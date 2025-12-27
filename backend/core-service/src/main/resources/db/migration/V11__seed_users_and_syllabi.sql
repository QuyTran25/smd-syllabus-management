/*
 * V11__seed_users_and_syllabi.sql
 * Seed Users và Syllabus Versions cho testing và demo
 * Tạo data mẫu cho đầy đủ workflow: Principal, HOD, AA, Lecturers
 */

BEGIN;

SET search_path TO core_service;

-- Log start
DO $$BEGIN RAISE NOTICE 'Starting V11 Migration: Seeding Users and Syllabi...'; END$$;

-- ==========================================
-- 0. INSERT ROLES FIRST (Required for user_roles foreign key)
-- ==========================================
INSERT INTO roles (code, name, description, is_system) VALUES 
('ADMIN', 'Administrator', 'System Administrator', TRUE),
('PRINCIPAL', 'Principal', 'School Principal / Hiệu trưởng', TRUE),
('AA', 'Academic Affairs', 'Academic Affairs / Phòng Đào tạo', TRUE),
('HOD', 'Head of Department', 'Head of Department / Trưởng Bộ môn', TRUE),
('LECTURER', 'Lecturer', 'Lecturer / Giảng viên', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 1. INSERT USERS (với password: "123456" - BCrypt hash)
-- ==========================================
-- Password hash for "123456": $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW

INSERT INTO users (
    email, password_hash, full_name, phone, gender, status, auth_provider
) VALUES 
-- ADMIN
('admin@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Nguyễn Văn Quản Trị', '0901234567', 'MALE', 'ACTIVE', 'LOCAL'),

-- PRINCIPAL (Hiệu trưởng)
('principal@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Trần Thị Hiệu Trưởng', '0901234568', 'FEMALE', 'ACTIVE', 'LOCAL'),

-- AA (Academic Affairs - Phòng Đào tạo)
('aa1@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Lê Văn Đào Tạo', '0901234569', 'MALE', 'ACTIVE', 'LOCAL'),
('aa2@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Phạm Thị Thu Hương', '0901234570', 'FEMALE', 'ACTIVE', 'LOCAL'),

-- HOD (Head of Department - Trưởng Bộ môn)
('hod.khmt@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Nguyễn Văn Khoa', '0901234571', 'MALE', 'ACTIVE', 'LOCAL'),
('hod.ktpm@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Trần Thị Mai', '0901234572', 'FEMALE', 'ACTIVE', 'LOCAL'),
('hod.httt@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Lê Văn Hệ Thống', '0901234573', 'MALE', 'ACTIVE', 'LOCAL'),

-- LECTURERS (Giảng viên)
('gv.nguyen@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Nguyễn Minh Tuấn', '0901234574', 'MALE', 'ACTIVE', 'LOCAL'),
('gv.tran@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Trần Thị Lan', '0901234575', 'FEMALE', 'ACTIVE', 'LOCAL'),
('gv.le@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Lê Hoàng Nam', '0901234576', 'MALE', 'ACTIVE', 'LOCAL'),
('gv.pham@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Phạm Thị Hoa', '0901234577', 'FEMALE', 'ACTIVE', 'LOCAL'),
('gv.hoang@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Hoàng Văn Đức', '0901234578', 'MALE', 'ACTIVE', 'LOCAL'),
('gv.vo@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 
 'Võ Thị Ngọc', '0901234579', 'FEMALE', 'ACTIVE', 'LOCAL')
ON CONFLICT (email) DO NOTHING;

-- ==========================================
-- 2. ASSIGN ROLES
-- ==========================================
WITH 
    user_ids AS (
        SELECT email, id FROM users 
        WHERE email IN (
            'admin@smd.edu.vn', 'principal@smd.edu.vn', 
            'aa1@smd.edu.vn', 'aa2@smd.edu.vn',
            'hod.khmt@smd.edu.vn', 'hod.ktpm@smd.edu.vn', 'hod.httt@smd.edu.vn',
            'gv.nguyen@smd.edu.vn', 'gv.tran@smd.edu.vn', 'gv.le@smd.edu.vn',
            'gv.pham@smd.edu.vn', 'gv.hoang@smd.edu.vn', 'gv.vo@smd.edu.vn'
        )
    ),
    role_ids AS (
        SELECT name, id FROM roles 
        WHERE name IN ('ADMIN', 'PRINCIPAL', 'AA', 'HOD', 'LECTURER')
    )
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM user_ids u, role_ids r
WHERE 
    (u.email = 'admin@smd.edu.vn' AND r.name = 'ADMIN') OR
    (u.email = 'principal@smd.edu.vn' AND r.name = 'PRINCIPAL') OR
    (u.email LIKE 'aa%@smd.edu.vn' AND r.name = 'AA') OR
    (u.email LIKE 'hod%@smd.edu.vn' AND r.name = 'HOD') OR
    (u.email LIKE 'gv%@smd.edu.vn' AND r.name = 'LECTURER')
ON CONFLICT DO NOTHING;

-- ==========================================
-- 3. ASSIGN LECTURERS TO DEPARTMENTS
-- ==========================================
WITH 
    dept_khmt AS (SELECT id FROM departments WHERE code = 'KHMT'),
    dept_ktpm AS (SELECT id FROM departments WHERE code = 'KTPM'),
    dept_httt AS (SELECT id FROM departments WHERE code = 'HTTT')
INSERT INTO lecturer_profiles (user_id, department_id, lecturer_code, title, specialization)
SELECT u.id, d.dept_id, 
       'GV' || LPAD(ROW_NUMBER() OVER ()::TEXT, 4, '0'),
       CASE WHEN RANDOM() < 0.3 THEN 'Giáo sư' 
            WHEN RANDOM() < 0.6 THEN 'Phó Giáo sư' 
            ELSE 'Giảng viên' END,
       'Công nghệ Phần mềm'
FROM users u
CROSS JOIN LATERAL (
    SELECT CASE 
        WHEN u.email IN ('gv.nguyen@smd.edu.vn', 'gv.tran@smd.edu.vn') THEN (SELECT id FROM dept_khmt)
        WHEN u.email IN ('gv.le@smd.edu.vn', 'gv.pham@smd.edu.vn') THEN (SELECT id FROM dept_ktpm)
        ELSE (SELECT id FROM dept_httt)
    END AS dept_id
) d
WHERE u.email LIKE 'gv%@smd.edu.vn'
ON CONFLICT (user_id) DO NOTHING;

-- ==========================================
-- 4. CREATE ACADEMIC TERM
-- ==========================================
INSERT INTO academic_terms (code, name, start_date, end_date, is_active)
VALUES 
('HK1_2024', 'Học kỳ 1 năm 2024-2025', '2024-09-01', '2025-01-15', TRUE),
('HK2_2024', 'Học kỳ 2 năm 2024-2025', '2025-01-20', '2025-06-30', FALSE)
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 5. INSERT SYLLABUS_VERSIONS
-- ==========================================
-- Tạo syllabus với các trạng thái khác nhau để test workflow

WITH 
    term_id AS (SELECT id FROM academic_terms WHERE code = 'HK1_2024'),
    lecturer_ids AS (
        SELECT u.id, u.full_name, lp.department_id,
               ROW_NUMBER() OVER () as rn
        FROM users u
        JOIN lecturer_profiles lp ON u.id = lp.user_id
        WHERE u.email LIKE 'gv%@smd.edu.vn'
    ),
    subjects_sample AS (
        SELECT s.id as subject_id, s.code, s.current_name_vi, s.department_id,
               ROW_NUMBER() OVER (ORDER BY s.code) as rn
        FROM subjects s
        WHERE s.code IN (
            '122042', '124001', '124002', '122003', '121000', '122002',
            '121002', '121008', '122005', '123002', '125000', '125001',
            '121031', '121034', '122036', '123013', '123033', '124003',
            '122038', '122039', '126000', '126001', '121033'
        )
    )
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    created_by, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
    theory_hours, practice_hours, self_study_hours,
    keywords, objectives, student_tasks,
    review_deadline, effective_date
)
SELECT 
    s.subject_id,
    (SELECT id FROM term_id),
    'v1.0',
    -- Phân bố trạng thái:
    CASE 
        WHEN s.rn <= 5 THEN 'PENDING_PRINCIPAL'::core_service.syllabus_status  -- 5 chờ Hiệu trưởng
        WHEN s.rn <= 8 THEN 'PENDING_AA'::core_service.syllabus_status         -- 3 chờ AA
        WHEN s.rn <= 12 THEN 'PENDING_HOD'::core_service.syllabus_status       -- 4 chờ HOD
        WHEN s.rn <= 16 THEN 'PUBLISHED'::core_service.syllabus_status         -- 4 đã xuất bản
        WHEN s.rn <= 18 THEN 'DRAFT'::core_service.syllabus_status             -- 2 nháp
        ELSE 'PENDING_PRINCIPAL'::core_service.syllabus_status                 -- Còn lại chờ Principal
    END,
    l.id, -- created_by (owner)
    s.code, -- snap_subject_code
    s.current_name_vi, -- snap_subject_name_vi
    'Subject ' || s.code, -- snap_subject_name_en
    3, -- snap_credit_count
    30, 45, 60, -- hours
    ARRAY['Lập trình', 'Phần mềm', 'Công nghệ'], -- keywords as array
    'Sinh viên nắm vững kiến thức nền tảng về ' || s.current_name_vi,
    'Bài tập tuần, Đồ án giữa kỳ, Bài thi cuối kỳ',
    CURRENT_DATE + INTERVAL '30 days',
    CURRENT_DATE + INTERVAL '60 days'
FROM subjects_sample s
CROSS JOIN LATERAL (
    SELECT id FROM lecturer_ids 
    WHERE MOD(s.rn, 6) + 1 = lecturer_ids.rn
    LIMIT 1
) l
ON CONFLICT DO NOTHING;

-- ==========================================
-- 6. UPDATE TIMESTAMPS FOR REALISM
-- ==========================================
-- Cập nhật timestamps để giống thực tế

-- PUBLISHED syllabus - đã qua tất cả workflow
UPDATE syllabus_versions 
SET 
    submitted_at = CURRENT_TIMESTAMP - INTERVAL '85 days',
    hod_approved_at = CURRENT_TIMESTAMP - INTERVAL '80 days',
    hod_approved_by = (SELECT id FROM users WHERE email = 'hod.ktpm@smd.edu.vn' LIMIT 1),
    aa_approved_at = CURRENT_TIMESTAMP - INTERVAL '70 days',
    aa_approved_by = (SELECT id FROM users WHERE email = 'aa1@smd.edu.vn' LIMIT 1),
    principal_approved_at = CURRENT_TIMESTAMP - INTERVAL '60 days',
    principal_approved_by = (SELECT id FROM users WHERE email = 'principal@smd.edu.vn' LIMIT 1),
    published_at = CURRENT_TIMESTAMP - INTERVAL '50 days'
WHERE status = 'PUBLISHED';

-- PENDING_PRINCIPAL - đã qua HOD và AA
UPDATE syllabus_versions 
SET 
    submitted_at = CURRENT_TIMESTAMP - INTERVAL '40 days',
    hod_approved_at = CURRENT_TIMESTAMP - INTERVAL '35 days',
    hod_approved_by = (SELECT id FROM users WHERE email = 'hod.khmt@smd.edu.vn' LIMIT 1),
    aa_approved_at = CURRENT_TIMESTAMP - INTERVAL '25 days',
    aa_approved_by = (SELECT id FROM users WHERE email = 'aa2@smd.edu.vn' LIMIT 1)
WHERE status = 'PENDING_PRINCIPAL';

-- PENDING_AA - đã qua HOD
UPDATE syllabus_versions 
SET 
    submitted_at = CURRENT_TIMESTAMP - INTERVAL '25 days',
    hod_approved_at = CURRENT_TIMESTAMP - INTERVAL '20 days',
    hod_approved_by = (SELECT id FROM users WHERE email = 'hod.httt@smd.edu.vn' LIMIT 1)
WHERE status = 'PENDING_AA';

-- PENDING_HOD - mới submit
UPDATE syllabus_versions 
SET 
    submitted_at = CURRENT_TIMESTAMP - INTERVAL '10 days'
WHERE status = 'PENDING_HOD';

-- DRAFT - đang soạn thảo (không có submitted_at)
-- 7. LOG SUMMARY
-- ==========================================
DO $$
DECLARE
    user_count INT;
    syllabus_count INT;
    pending_principal INT;
    pending_aa INT;
    pending_hod INT;
    published INT;
    draft INT;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO syllabus_count FROM syllabus_versions;
    SELECT COUNT(*) INTO pending_principal FROM syllabus_versions WHERE status = 'PENDING_PRINCIPAL';
    SELECT COUNT(*) INTO pending_aa FROM syllabus_versions WHERE status = 'PENDING_AA';
    SELECT COUNT(*) INTO pending_hod FROM syllabus_versions WHERE status = 'PENDING_HOD';
    SELECT COUNT(*) INTO published FROM syllabus_versions WHERE status = 'PUBLISHED';
    SELECT COUNT(*) INTO draft FROM syllabus_versions WHERE status = 'DRAFT';
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'V11 Migration completed successfully!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Users created: %', user_count;
    RAISE NOTICE 'Syllabus versions created: %', syllabus_count;
    RAISE NOTICE '  - PENDING_PRINCIPAL: %', pending_principal;
    RAISE NOTICE '  - PENDING_AA: %', pending_aa;
    RAISE NOTICE '  - PENDING_HOD: %', pending_hod;
    RAISE NOTICE '  - PUBLISHED: %', published;
    RAISE NOTICE '  - DRAFT: %', draft;
    RAISE NOTICE '================================';
END $$;

COMMIT;
