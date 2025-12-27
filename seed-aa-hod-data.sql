/*
 * Seed data cho AA và HoD testing
 * - Tạo syllabus với trạng thái PENDING_HOD, PENDING_AA, PENDING_PRINCIPAL
 * - Tạo lecturer profiles
 */

SET search_path TO core_service;

-- Tạo academic term nếu chưa có
INSERT INTO academic_terms (code, name, start_date, end_date, is_active)
VALUES ('HK1_2024', 'Hoc ky 1 nam 2024-2025', '2024-09-01', '2025-01-15', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Tạo lecturer profiles
INSERT INTO lecturer_profiles (user_id, department_id, lecturer_code, title, specialization)
SELECT u.id, d.id, 'GV' || LPAD(ROW_NUMBER() OVER ()::TEXT, 4, '0'), 'Giang vien', 'Cong nghe Phan mem'
FROM users u
CROSS JOIN (SELECT id FROM departments WHERE code = 'KTPM' LIMIT 1) d
WHERE u.email LIKE 'gv.%@smd.edu.vn'
ON CONFLICT (user_id) DO NOTHING;

-- Insert PENDING_HOD syllabus (chờ Trưởng Bộ môn duyệt)
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    snap_subject_code, snap_subject_name_vi, snap_credit_count, 
    created_by, theory_hours, practice_hours, self_study_hours
)
SELECT 
    s.id, t.id, '1.0', 'PENDING_HOD'::syllabus_status,
    s.code, s.current_name_vi, s.default_credits,
    u.id, 30, 15, 45
FROM subjects s
CROSS JOIN (SELECT id FROM academic_terms WHERE code = 'HK1_2024') t
CROSS JOIN (SELECT id FROM users WHERE email = 'gv.nguyen@smd.edu.vn') u
WHERE s.code IN ('121000', '122003', '124001')
ON CONFLICT DO NOTHING;

-- Insert PENDING_AA syllabus (chờ Phòng Đào tạo duyệt)  
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    snap_subject_code, snap_subject_name_vi, snap_credit_count, 
    created_by, theory_hours, practice_hours, self_study_hours
)
SELECT 
    s.id, t.id, '1.0', 'PENDING_AA'::syllabus_status,
    s.code, s.current_name_vi, s.default_credits,
    u.id, 30, 15, 45
FROM subjects s
CROSS JOIN (SELECT id FROM academic_terms WHERE code = 'HK1_2024') t
CROSS JOIN (SELECT id FROM users WHERE email = 'gv.tran@smd.edu.vn') u
WHERE s.code IN ('124002', '122002', '123002')
ON CONFLICT DO NOTHING;

-- Insert PENDING_PRINCIPAL syllabus (chờ Hiệu trưởng duyệt)
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    snap_subject_code, snap_subject_name_vi, snap_credit_count, 
    created_by, theory_hours, practice_hours, self_study_hours
)
SELECT 
    s.id, t.id, '1.0', 'PENDING_PRINCIPAL'::syllabus_status,
    s.code, s.current_name_vi, s.default_credits,
    u.id, 30, 15, 45
FROM subjects s
CROSS JOIN (SELECT id FROM academic_terms WHERE code = 'HK1_2024') t
CROSS JOIN (SELECT id FROM users WHERE email = 'gv.le@smd.edu.vn') u
WHERE s.code IN ('125001', '121002')
ON CONFLICT DO NOTHING;

-- Insert APPROVED syllabus (đã duyệt, chờ Admin xuất bản)
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    snap_subject_code, snap_subject_name_vi, snap_credit_count, 
    created_by, theory_hours, practice_hours, self_study_hours
)
SELECT 
    s.id, t.id, '1.0', 'APPROVED'::syllabus_status,
    s.code, s.current_name_vi, s.default_credits,
    u.id, 30, 15, 45
FROM subjects s
CROSS JOIN (SELECT id FROM academic_terms WHERE code = 'HK1_2024') t
CROSS JOIN (SELECT id FROM users WHERE email = 'gv.pham@smd.edu.vn') u
WHERE s.code IN ('122005', '121008')
ON CONFLICT DO NOTHING;

-- Insert DRAFT syllabus (giảng viên đang soạn)
INSERT INTO syllabus_versions (
    subject_id, academic_term_id, version_no, status, 
    snap_subject_code, snap_subject_name_vi, snap_credit_count, 
    created_by, theory_hours, practice_hours, self_study_hours
)
SELECT 
    s.id, t.id, '1.0', 'DRAFT'::syllabus_status,
    s.code, s.current_name_vi, s.default_credits,
    u.id, 30, 15, 45
FROM subjects s
CROSS JOIN (SELECT id FROM academic_terms WHERE code = 'HK1_2024') t
CROSS JOIN (SELECT id FROM users WHERE email = 'gv.hoang@smd.edu.vn') u
WHERE s.code IN ('121031', '121034', '122036')
ON CONFLICT DO NOTHING;
