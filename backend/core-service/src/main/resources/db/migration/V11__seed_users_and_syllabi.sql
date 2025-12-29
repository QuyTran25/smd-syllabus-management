/*
 * V11__seed_users_and_syllabi.sql
 * Seed Users và Syllabus Versions cho testing và demo
 * Tạo data mẫu cho đầy đủ workflow: Principal, HOD, AA, Lecturers
 */

BEGIN;

-- Chuyển ngữ cảnh làm việc vào schema dự án
SET search_path TO core_service, public;

-- Log start
DO $$BEGIN RAISE NOTICE 'Starting V11 Migration: Seeding Users and Syllabi...'; END$$;

-- ==========================================
-- 0. INSERT ROLES (Đảm bảo vai trò hệ thống tồn tại)
-- ==========================================
INSERT INTO roles (code, name, description, is_system) VALUES 
('ADMIN', 'Administrator', 'System Administrator', TRUE),
('PRINCIPAL', 'Principal', 'School Principal / Hiệu trưởng', TRUE),
('AA', 'Academic Affairs', 'Academic Affairs / Phòng Đào tạo', TRUE),
('HOD', 'Head of Department', 'Head of Department / Trưởng Bộ môn', TRUE),
('LECTURER', 'Lecturer', 'Lecturer / Giảng viên', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 1. INSERT USERS (Password mặc định: "123456")
-- ==========================================
-- Password hash: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
INSERT INTO users (
    email, password_hash, full_name, phone, gender, status, auth_provider
) VALUES 
-- Quản trị & Ban giám hiệu
('admin@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Nguyễn Văn Quản Trị', '0901234567', 'MALE', 'ACTIVE', 'LOCAL'),
('principal@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Trần Thị Hiệu Trưởng', '0901234568', 'FEMALE', 'ACTIVE', 'LOCAL'),

-- Phòng Đào tạo (AA)
('aa1@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Lê Văn Đào Tạo', '0901234569', 'MALE', 'ACTIVE', 'LOCAL'),
('aa2@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Phạm Thị Thu Hương', '0901234570', 'FEMALE', 'ACTIVE', 'LOCAL'),

-- Trưởng bộ môn (HOD)
('hod.khmt@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Nguyễn Văn Khoa', '0901234571', 'MALE', 'ACTIVE', 'LOCAL'),
('hod.ktpm@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Trần Thị Mai', '0901234572', 'FEMALE', 'ACTIVE', 'LOCAL'),

-- Giảng viên (Lecturers)
('gv.nguyen@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Nguyễn Minh Tuấn', '0901234574', 'MALE', 'ACTIVE', 'LOCAL'),
('gv.tran@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Trần Thị Lan', '0901234575', 'FEMALE', 'ACTIVE', 'LOCAL'),
('gv.vo@smd.edu.vn', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Võ Thị Ngọc', '0901234579', 'FEMALE', 'ACTIVE', 'LOCAL')
ON CONFLICT (email) DO NOTHING;

-- ==========================================
-- 2. GÁN QUYỀN (Roles Assignment)
-- ==========================================
INSERT INTO user_roles (user_id, role_id, scope_type)
SELECT u.id, r.id, 'GLOBAL'
FROM users u, roles r
WHERE 
    (u.email = 'admin@smd.edu.vn' AND r.code = 'ADMIN') OR
    (u.email = 'principal@smd.edu.vn' AND r.code = 'PRINCIPAL') OR
    (u.email LIKE 'aa%@smd.edu.vn' AND r.code = 'AA') OR
    (u.email LIKE 'hod%@smd.edu.vn' AND r.code = 'HOD') OR
    (u.email LIKE 'gv%@smd.edu.vn' AND r.code = 'LECTURER')
ON CONFLICT DO NOTHING;

-- ==========================================
-- 3. HỒ SƠ GIẢNG VIÊN & HỌC KỲ
-- ==========================================
INSERT INTO lecturer_profiles (user_id, department_id, lecturer_code, specialization)
SELECT u.id, d.id, 'GV' || LPAD(u.id::text, 4, '0'), 'Computer Science'
FROM users u JOIN departments d ON d.code = 'KHMT'
WHERE u.email LIKE 'gv.nguyen%'
ON CONFLICT DO NOTHING;

INSERT INTO academic_terms (code, name, start_date, end_date, is_active)
VALUES ('HK1_2024', 'Học kỳ 1 năm 2024-2025', '2024-09-01', '2025-01-15', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 4. INSERT SYLLABUS_VERSIONS (Workflow Mockup)
-- ==========================================
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, created_by,
    snap_subject_code, snap_subject_name_vi, snap_credit_count,
    content, published_at
)
SELECT 
    s.id, (SELECT id FROM academic_terms WHERE code = 'HK1_2024'), 'v1.0',
    CASE 
        WHEN s.code = '122042' THEN 'PUBLISHED'::core_service.syllabus_status
        WHEN s.code = '124001' THEN 'PENDING_AA'::core_service.syllabus_status
        ELSE 'DRAFT'::core_service.syllabus_status
    END,
    (SELECT id FROM users WHERE email = 'gv.nguyen@smd.edu.vn'),
    s.code, s.current_name_vi, s.default_credits,
    '{"objectives": "Kiến thức nền tảng", "hours": 45}'::jsonb,
    CASE WHEN s.code = '122042' THEN CURRENT_TIMESTAMP ELSE NULL END
FROM subjects s
WHERE s.code IN ('122042', '124001', '124002')
ON CONFLICT DO NOTHING;

-- ==========================================
-- 5. LOG SUMMARY
-- ==========================================
DO $$
DECLARE
    u_count INT;
    s_count INT;
BEGIN
    SELECT COUNT(*) INTO u_count FROM users;
    SELECT COUNT(*) INTO s_count FROM syllabus_versions;
    RAISE NOTICE 'V11 Seed Completed: % Users and % Syllabi created.', u_count, s_count;
END $$;

COMMIT;