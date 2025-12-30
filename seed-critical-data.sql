SET search_path TO core_service;

-- Update existing lecturer users to have department_id
DO $$
DECLARE
    v_ktpm_dept_id UUID;
    v_httt_dept_id UUID;
    v_khmt_dept_id UUID;
BEGIN
    -- Get department IDs
    SELECT id INTO v_ktpm_dept_id FROM departments WHERE code = 'KTPM' LIMIT 1;
    SELECT id INTO v_httt_dept_id FROM departments WHERE code = 'HTTT' LIMIT 1;
    SELECT id INTO v_khmt_dept_id FROM departments WHERE code = 'KHMT' LIMIT 1;
    
    -- Update lecturer users
    UPDATE users 
    SET department_id = v_ktpm_dept_id
    WHERE email LIKE 'gv.%@smd.edu.vn' AND (department_id IS NULL OR email LIKE '%nguyen%' OR email LIKE '%tran%');
    
    UPDATE users 
    SET department_id = v_httt_dept_id
    WHERE email LIKE 'gv.le%@smd.edu.vn';
    
    UPDATE users 
    SET department_id = v_khmt_dept_id
    WHERE email LIKE 'gv.pham%@smd.edu.vn';
    
    RAISE NOTICE 'Updated users with departments';
END $$;

-- Create teaching assignments
DO $$
DECLARE
    v_term_id UUID;
    v_lecturer_id UUID;
    v_syllabus_id UUID;
    v_subject_id UUID;
BEGIN
    -- Get term
    SELECT id INTO v_term_id FROM academic_terms WHERE is_active = TRUE LIMIT 1;
    
    -- Get a lecturer
    SELECT id INTO v_lecturer_id FROM users WHERE email LIKE 'gv.%@smd.edu.vn' LIMIT 1;
    
    -- Get some approved syllabi
    FOR v_syllabus_id, v_subject_id IN 
        SELECT id, subject_id FROM syllabus_versions WHERE status = 'APPROVED' LIMIT 5
    LOOP
        INSERT INTO teaching_assignments (
            subject_id, 
            academic_term_id, 
            main_lecturer_id, 
            syllabus_version_id,
            assigned_by,
            deadline,
            status,
            comments
        )
        SELECT 
            v_subject_id,
            v_term_id,
            v_lecturer_id,
            v_syllabus_id,
            (SELECT id FROM users WHERE email = 'admin@smd.edu.vn'),
            NOW() + INTERVAL '30 days',
            'pending',
            'Auto-generated test assignment'
        WHERE NOT EXISTS (
            SELECT 1 FROM teaching_assignments 
            WHERE subject_id = v_subject_id 
            AND academic_term_id = v_term_id 
            AND main_lecturer_id = v_lecturer_id
        );
    END LOOP;
    
    RAISE NOTICE 'Created teaching assignments';
END $$;

-- Verify data
SELECT 'PLOs: ' || COUNT(*) FROM plos
UNION ALL
SELECT 'CLO-PLO: ' || COUNT(*) FROM clo_plo_mappings
UNION ALL
SELECT 'Users with dept: ' || COUNT(*) FROM users WHERE department_id IS NOT NULL
UNION ALL
SELECT 'Assignments: ' || COUNT(*) FROM teaching_assignments;
