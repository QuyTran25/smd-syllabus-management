-- V18: Seed Teaching Assignments Data
-- Created: 2025-12-29
-- Purpose: Seed realistic teaching assignment data for testing

SET search_path TO core_service;

-- Insert teaching assignments
-- Note: These assignments link subjects with lecturers and define syllabus creation tasks

WITH 
    term_data AS (
        SELECT id FROM academic_terms LIMIT 1
    ),
    -- Lecturers
    gv_tuan AS (SELECT id FROM users WHERE email = 'gv.nguyen@smd.edu.vn' LIMIT 1),
    gv_lan AS (SELECT id FROM users WHERE email = 'gv.tran@smd.edu.vn' LIMIT 1),
    gv_nam AS (SELECT id FROM users WHERE email = 'gv.le@smd.edu.vn' LIMIT 1),
    hod AS (SELECT id FROM users WHERE email LIKE 'hod%' OR email LIKE '%admin%' LIMIT 1)
    
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
    COALESCE(
        CASE 
            WHEN s.code = '121031' THEN (SELECT id FROM gv_tuan)
            WHEN s.code = '121034' THEN (SELECT id FROM gv_tuan)
            WHEN s.code = '122036' THEN (SELECT id FROM gv_tuan)
            WHEN s.code = '123013' THEN (SELECT id FROM gv_lan)
            WHEN s.code = '123033' THEN (SELECT id FROM gv_lan)
            WHEN s.code = '124003' THEN (SELECT id FROM gv_nam)
        END,
        (SELECT id FROM users LIMIT 1)  -- Fallback to any user
    ),
    CASE 
        WHEN s.code IN ('121031', '121034', '122036') THEN '2026-01-26'::date
        WHEN s.code IN ('123013', '123033') THEN '2026-01-11'::date
        WHEN s.code = '124003' THEN '2025-12-22'::date
    END,
    CASE 
        WHEN s.code IN ('121031', '121034', '122036') THEN 'pending'::core_service.assignment_status
        WHEN s.code IN ('123013', '123033') THEN 'in-progress'::core_service.assignment_status
        WHEN s.code = '124003' THEN 'submitted'::core_service.assignment_status
    END,
    COALESCE((SELECT id FROM hod), (SELECT id FROM users LIMIT 1)),  -- Fallback to any user
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
