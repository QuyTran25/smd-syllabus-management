-- Script tạo version giả để test chức năng so sánh (Simplified)
-- Copy version hiện tại và tạo version mới

DO $$
DECLARE
    v_subject_id UUID;
    v_current_version_id UUID;
    v_new_version_id UUID := gen_random_uuid();
    v_current_version_no VARCHAR;
    v_new_version_no VARCHAR;
BEGIN
    -- Lấy subject_id của AI26
    SELECT id INTO v_subject_id 
    FROM core_service.subjects 
    WHERE code = 'AI26';
    
    -- Lấy version hiện tại
    SELECT id, version_no INTO v_current_version_id, v_current_version_no
    FROM core_service.syllabus_versions
    WHERE subject_id = v_subject_id AND is_deleted = false
    ORDER BY created_at DESC
    LIMIT 1;
    
    -- Tạo version_no mới
    v_new_version_no := 'v' || (COALESCE(NULLIF(regexp_replace(v_current_version_no, '[^0-9]', '', 'g'), ''), '1')::INT + 1)::TEXT;
    
    RAISE NOTICE 'Creating new version from: %, new version_no: %', v_current_version_id, v_new_version_no;
    
    -- Insert version mới (copy tất cả trừ các trường cần thay đổi)
    INSERT INTO core_service.syllabus_versions
    SELECT
        v_new_version_id,
        subject_id,
        academic_term_id,
        v_new_version_no,
        'DRAFT',
        NULL, -- previous_version_id
        NULL, -- review_deadline
        snap_subject_code,
        snap_subject_name_vi || ' (Test)',
        snap_subject_name_en,
        snap_credit_count,
        keywords,
        content,
        NULL, -- approved_by
        created_by,
        updated_by,
        NULL, -- published_at
        false, -- is_deleted
        NOW() - INTERVAL '2 hours', -- created_at trước version hiện tại
        NOW() - INTERVAL '2 hours', -- updated_at
        NULL, -- effective_date
        NULL, -- unpublished_at
        NULL, -- unpublished_by
        NULL, -- unpublish_reason
        false, -- is_edit_enabled
        NULL, -- edit_enabled_by
        NULL, -- edit_enabled_at
        workflow_id,
        0, -- current_approval_step
        theory_hours,
        practice_hours,
        self_study_hours,
        grading_scale_id,
        student_duties,
        NULL, -- submitted_at
        NULL, -- hod_approved_at
        NULL -- hod_approved_by
    FROM core_service.syllabus_versions
    WHERE id = v_current_version_id;
    
    RAISE NOTICE '✅ Created test version: %', v_new_version_id;
END $$;

-- Verify - hiển thị tất cả versions
SELECT 
    sv.id,
    sv.version_no,
    sv.status,
    sv.snap_subject_code,
    sv.snap_subject_name_vi,
    sv.created_at
FROM core_service.syllabus_versions sv
WHERE sv.subject_id = (SELECT id FROM core_service.subjects WHERE code = 'AI26')
  AND sv.is_deleted = false
ORDER BY sv.created_at DESC;
