-- V22: Seed Teaching Assignment Collaborators and In-Progress Data
-- Purpose: Add collaborators to teaching assignments and create realistic in-progress assignments with DRAFT syllabi

-- ============================================
-- 1. Add Collaborators to Teaching Assignments
-- ============================================
-- Each assignment should have 1-2 collaborators working with the main lecturer

INSERT INTO core_service.teaching_assignment_collaborators (id, assignment_id, lecturer_id, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    ta.id,
    u.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM core_service.teaching_assignments ta
CROSS JOIN LATERAL (
    SELECT id 
    FROM core_service.users 
    WHERE id != ta.main_lecturer_id 
    AND id IN (
        SELECT user_id 
        FROM core_service.user_roles ur
        JOIN core_service.roles r ON ur.role_id = r.id
        WHERE r.code LIKE '%LECTURER%'
    )
    ORDER BY random() 
    LIMIT 2
) u
ON CONFLICT (assignment_id, lecturer_id) DO NOTHING;

-- ============================================
-- 2. Update Teaching Assignments with Diverse Lecturers
-- ============================================
-- Assign different main lecturers to make data more realistic

DO $$
DECLARE
    v_hod_id UUID;
    v_assignment_ids UUID[];
    v_lecturer_ids UUID[];
BEGIN
    -- Get HOD for assigned_by
    SELECT id INTO v_hod_id 
    FROM core_service.users 
    WHERE email LIKE 'hod%' 
    LIMIT 1;
    
    -- Get lecturer IDs
    SELECT ARRAY_AGG(id) INTO v_lecturer_ids
    FROM (
        SELECT u.id 
        FROM core_service.users u
        JOIN core_service.user_roles ur ON u.id = ur.user_id
        JOIN core_service.roles r ON ur.role_id = r.id
        WHERE r.code LIKE '%LECTURER%'
        ORDER BY u.full_name
        LIMIT 6
    ) lecturers;
    
    -- Get assignment IDs
    SELECT ARRAY_AGG(id) INTO v_assignment_ids
    FROM (
        SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 5
    ) assignments;
    
    -- Assign different lecturers
    IF v_assignment_ids IS NOT NULL AND v_lecturer_ids IS NOT NULL THEN
        FOR i IN 1..LEAST(array_length(v_assignment_ids, 1), array_length(v_lecturer_ids, 1))
        LOOP
            UPDATE core_service.teaching_assignments
            SET main_lecturer_id = COALESCE(v_lecturer_ids[i], main_lecturer_id),
                assigned_by = COALESCE(assigned_by, v_hod_id, (SELECT id FROM core_service.users LIMIT 1))
            WHERE id = v_assignment_ids[i];
        END LOOP;
    END IF;
END $$;

-- ============================================
-- 3. Create In-Progress Assignments with DRAFT Syllabi
-- ============================================
-- Change 2 assignments to in-progress status with actual DRAFT syllabi

DO $$
DECLARE
    v_assignment_record RECORD;
    v_approved_syllabus RECORD;
    v_draft_syllabus_id UUID;
    v_counter INT := 0;
BEGIN
    -- For first 2 assignments, create DRAFT syllabi
    FOR v_assignment_record IN 
        SELECT ta.id, ta.subject_id, ta.academic_term_id, ta.main_lecturer_id
        FROM core_service.teaching_assignments ta
        ORDER BY ta.id
        LIMIT 2
    LOOP
        -- Find an approved syllabus for this subject to copy structure from
        SELECT * INTO v_approved_syllabus
        FROM core_service.syllabus_versions
        WHERE subject_id = v_assignment_record.subject_id
        AND status = 'APPROVED'
        LIMIT 1;
        
        IF v_approved_syllabus.id IS NOT NULL THEN
            -- Create DRAFT syllabus based on approved one
            INSERT INTO core_service.syllabus_versions (
                subject_id, academic_term_id, version_no,
                snap_subject_code, snap_subject_name_vi, snap_subject_name_en,
                snap_credit_count, status,
                created_by, updated_by, created_at, updated_at
            ) VALUES (
                v_assignment_record.subject_id,
                v_assignment_record.academic_term_id,
                'v2.' || v_counter || '-draft',
                v_approved_syllabus.snap_subject_code,
                v_approved_syllabus.snap_subject_name_vi,
                v_approved_syllabus.snap_subject_name_en,
                v_approved_syllabus.snap_credit_count,
                'DRAFT'::core_service.syllabus_status,
                v_assignment_record.main_lecturer_id,
                v_assignment_record.main_lecturer_id,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            ) RETURNING id INTO v_draft_syllabus_id;
            
            -- Update assignment to in-progress with the DRAFT syllabus
            UPDATE core_service.teaching_assignments
            SET status = 'in-progress',
                syllabus_version_id = v_draft_syllabus_id,
                comments = CASE 
                    WHEN v_counter = 0 THEN 'Giáo viên đang soạn phần mục tiêu môn học. GV cộng tác đã review và góp ý về phần tài liệu tham khảo.'
                    ELSE 'Đã hoàn thành phần nội dung chương trình. Đang chờ GV cộng tác review phần đánh giá.'
                END,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = v_assignment_record.id;
            
            v_counter := v_counter + 1;
        END IF;
    END LOOP;
END $$;

-- ============================================
-- 4. Update Remaining Assignments
-- ============================================
-- Set pending assignments to have proper comments and no syllabus link

UPDATE core_service.teaching_assignments
SET syllabus_version_id = NULL,
    comments = 'Chờ giáo viên bắt đầu soạn đề cương',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'pending' 
AND syllabus_version_id IN (
    SELECT id FROM core_service.syllabus_versions WHERE status = 'APPROVED'
);

-- ============================================
-- Verification Query
-- ============================================
SELECT 
    s.code as subject_code,
    u.full_name as lecturer,
    ta.status::text,
    COUNT(tac.id) as collaborator_count,
    CASE 
        WHEN sv.id IS NULL THEN 'No syllabus'
        ELSE sv.status::text || ' (' || sv.version_no || ')'
    END as syllabus_info,
    LEFT(ta.comments, 50) as comment_preview
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
JOIN core_service.users u ON ta.main_lecturer_id = u.id
LEFT JOIN core_service.teaching_assignment_collaborators tac ON ta.id = tac.assignment_id
LEFT JOIN core_service.syllabus_versions sv ON ta.syllabus_version_id = sv.id
GROUP BY s.code, u.full_name, ta.status, sv.id, sv.status, sv.version_no, ta.comments
ORDER BY ta.status DESC, s.code
LIMIT 10;
