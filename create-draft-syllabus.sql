DO $$
DECLARE
    v_assignment_record RECORD;
    v_subject_record RECORD;
    v_syllabus_id UUID;
BEGIN
    FOR v_assignment_record IN 
        SELECT ta.id as assignment_id, ta.subject_id, ta.academic_term_id, ta.main_lecturer_id
        FROM core_service.teaching_assignments ta
        WHERE ta.status = 'in-progress' AND ta.syllabus_version_id IS NULL
    LOOP
        SELECT * INTO v_subject_record 
        FROM core_service.subjects 
        WHERE id = v_assignment_record.subject_id;
        
        INSERT INTO core_service.syllabus_versions (
            id, subject_id, academic_term_id, version_no,
            snap_subject_code, snap_subject_name_vi, snap_subject_name_en,
            snap_credit_count, snap_theory_hours, snap_practice_hours, snap_self_study_hours,
            status, created_by, updated_by, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            v_assignment_record.subject_id,
            v_assignment_record.academic_term_id,
            'v2.0-draft',
            v_subject_record.code,
            v_subject_record.current_name_vi,
            COALESCE(v_subject_record.current_name_en, 'N/A'),
            v_subject_record.default_credits,
            COALESCE(v_subject_record.default_theory_hours, 0),
            COALESCE(v_subject_record.default_practice_hours, 0),
            COALESCE(v_subject_record.default_self_study_hours, 0),
            'DRAFT'::core_service.syllabus_status,
            v_assignment_record.main_lecturer_id,
            v_assignment_record.main_lecturer_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        ) RETURNING id INTO v_syllabus_id;
        
        UPDATE core_service.teaching_assignments
        SET syllabus_version_id = v_syllabus_id
        WHERE id = v_assignment_record.assignment_id;
        
        RAISE NOTICE 'Created DRAFT syllabus for assignment %', v_assignment_record.assignment_id;
    END LOOP;
END $$;

SELECT ta.id, s.code, u.full_name, ta.status::text, sv.version_no, sv.status::text as syllabus_status
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
JOIN core_service.users u ON ta.main_lecturer_id = u.id
LEFT JOIN core_service.syllabus_versions sv ON ta.syllabus_version_id = sv.id
WHERE ta.status = 'in-progress';
