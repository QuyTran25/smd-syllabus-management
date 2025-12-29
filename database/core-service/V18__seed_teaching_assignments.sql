-- V18: Seed Teaching Assignments Data
-- Created: 2025-12-29
-- Purpose: Seed realistic teaching assignment data for testing

SET search_path TO core_service;

-- Insert teaching assignments
-- Note: These assignments link subjects with lecturers and define syllabus creation tasks

WITH 
    term_data AS (
        SELECT id FROM academic_terms WHERE term_year = '2024-2025' AND term_no = 1 LIMIT 1
    ),
    -- Lecturers
    gv_tuan AS (SELECT id FROM users WHERE email = 'gv.nguyen@smd.edu.vn' LIMIT 1),
    gv_lan AS (SELECT id FROM users WHERE email = 'gv.tran@smd.edu.vn' LIMIT 1),
    gv_nam AS (SELECT id FROM users WHERE email = 'gv.le@smd.edu.vn' LIMIT 1),
    hod AS (SELECT id FROM users WHERE email = 'hod@smd.edu.vn' LIMIT 1)
    
INSERT INTO teaching_assignments (
    id,
    subject_id,
    academic_term_id,
    main_lecturer_id,
    deadline,
    status,
    assigned_by,
    created_at,
    updated_at
)
SELECT 
    gen_random_uuid(),
    s.id,
    (SELECT id FROM term_data),
    CASE 
        WHEN s.code = '121031' THEN (SELECT id FROM gv_tuan)  -- Lập trình Web
        WHEN s.code = '121034' THEN (SELECT id FROM gv_tuan)  -- Lập trình Mobile
        WHEN s.code = '122036' THEN (SELECT id FROM gv_tuan)  -- Lập trình Java
        WHEN s.code = '123013' THEN (SELECT id FROM gv_lan)   -- Lập trình mạng
        WHEN s.code = '123033' THEN (SELECT id FROM gv_lan)   -- An toàn thông tin
        WHEN s.code = '124003' THEN (SELECT id FROM gv_nam)   -- Phân tích thiết kế giải thuật
    END,
    CASE 
        WHEN s.code IN ('121031', '121034', '122036') THEN '2026-01-26'::date
        WHEN s.code IN ('123013', '123033') THEN '2026-01-11'::date
        WHEN s.code = '124003' THEN '2025-12-22'::date
    END,
    CASE 
        WHEN s.code IN ('121031', '121034', '122036') THEN 'PENDING'::core_service.assignment_status
        WHEN s.code IN ('123013', '123033') THEN 'IN_PROGRESS'::core_service.assignment_status
        WHEN s.code = '124003' THEN 'SUBMITTED'::core_service.assignment_status
    END,
    (SELECT id FROM hod),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM subjects s
WHERE s.code IN ('121031', '121034', '122036', '123013', '123033', '124003')
ON CONFLICT DO NOTHING;

-- Display created assignments
SELECT 
    s.code as "Mã môn",
    s.current_name_vi as "Tên môn",
    u.full_name as "Giảng viên chính",
    ta.status as "Trạng thái",
    ta.deadline as "Deadline"
FROM teaching_assignments ta
JOIN subjects s ON ta.subject_id = s.id
JOIN users u ON ta.main_lecturer_id = u.id
ORDER BY ta.created_at DESC;
