-- Fix Teaching Assignment Data for Collaboration Management
-- Run this to create proper test data

BEGIN;

-- 1. Add collaborators to existing assignments
-- Get some different lecturer IDs
DO $$
DECLARE
    v_assignment_record RECORD;
    v_collab_lecturer_id UUID;
    v_main_lecturer_id UUID;
    v_counter INT := 0;
BEGIN
    -- For each assignment, add 1-2 collaborators
    FOR v_assignment_record IN 
        SELECT id, main_lecturer_id FROM core_service.teaching_assignments LIMIT 5
    LOOP
        v_main_lecturer_id := v_assignment_record.main_lecturer_id;
        
        -- Get 1-2 different lecturers (not the main lecturer)
        FOR v_collab_lecturer_id IN 
            SELECT id FROM core_service.users 
            WHERE id != v_main_lecturer_id 
            AND id IN (SELECT user_id FROM core_service.user_roles WHERE role_id IN (SELECT id FROM core_service.roles WHERE code LIKE '%LECTURER%'))
            LIMIT 2
        LOOP
            INSERT INTO core_service.teaching_assignment_collaborators (
                id, assignment_id, lecturer_id, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                v_assignment_record.id,
                v_collab_lecturer_id,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            ) ON CONFLICT (assignment_id, lecturer_id) DO NOTHING;
            
            v_counter := v_counter + 1;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Added % collaborators', v_counter;
END $$;

-- 2. Update teaching assignments to remove link to approved syllabi
-- They should link to DRAFT versions or NULL (will be created when lecturer starts working)
UPDATE core_service.teaching_assignments
SET syllabus_version_id = NULL,
    status = 'pending',
    comments = 'Chờ giáo viên bắt đầu soạn đề cương'
WHERE status = 'pending';

-- 3. Create some assignments with in-progress status (has DRAFT syllabus)
DO $$
DECLARE
    v_assignment_id UUID;
    v_subject_id UUID;
    v_term_id UUID;
    v_draft_version_id UUID;
BEGIN
    -- Find an assignment
    SELECT id, subject_id INTO v_assignment_id, v_subject_id
    FROM core_service.teaching_assignments
    LIMIT 1;
    
    IF v_assignment_id IS NOT NULL THEN
        -- Get academic term
        SELECT academic_term_id INTO v_term_id
        FROM core_service.teaching_assignments
        WHERE id = v_assignment_id;
        
        -- Create a DRAFT syllabus for this assignment
        INSERT INTO core_service.syllabus_versions (
            id, subject_id, academic_term_id, version_no, 
            status, created_by, updated_by,
            created_at, updated_at
        ) VALUES (
            gen_random_uuid(), v_subject_id, v_term_id, 'v2.0-draft',
            'DRAFT'::core_service.syllabus_status,
            (SELECT main_lecturer_id FROM core_service.teaching_assignments WHERE id = v_assignment_id),
            (SELECT main_lecturer_id FROM core_service.teaching_assignments WHERE id = v_assignment_id),
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        ) RETURNING id INTO v_draft_version_id;
        
        -- Link assignment to this DRAFT
        UPDATE core_service.teaching_assignments
        SET syllabus_version_id = v_draft_version_id,
            status = 'in-progress',
            comments = 'Giáo viên đang soạn đề cương'
        WHERE id = v_assignment_id;
    END IF;
END $$;

COMMIT;

-- Verify results
SELECT 
    ta.id,
    s.code as subject_code,
    ta.status::text,
    COUNT(tac.id) as collab_count,
    CASE 
        WHEN ta.syllabus_version_id IS NULL THEN 'No syllabus'
        ELSE sv.status::text 
    END as syllabus_status
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
LEFT JOIN core_service.teaching_assignment_collaborators tac ON ta.id = tac.assignment_id
LEFT JOIN core_service.syllabus_versions sv ON ta.syllabus_version_id = sv.id
GROUP BY ta.id, s.code, ta.status, ta.syllabus_version_id, sv.status
ORDER BY ta.status DESC
LIMIT 10;
