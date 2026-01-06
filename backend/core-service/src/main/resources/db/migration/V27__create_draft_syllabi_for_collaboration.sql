-- V27: Create more DRAFT syllabi for collaboration testing
-- Add DRAFT syllabi for gv.tran and gv.hoang so they can be assigned as collaborators

DO $$
DECLARE
    v_gv_tran_id UUID;
    v_gv_hoang_id UUID;
    v_term_id UUID;
    v_syllabus_tran UUID;
    v_syllabus_hoang UUID;
BEGIN
    -- Get user IDs
    SELECT id INTO v_gv_tran_id FROM core_service.users WHERE email = 'gv.tran@smd.edu.vn';
    SELECT id INTO v_gv_hoang_id FROM core_service.users WHERE email = 'gv.hoang@smd.edu.vn';
    
    -- Get current academic term
    SELECT id INTO v_term_id 
    FROM core_service.academic_terms 
    WHERE is_active = TRUE 
    LIMIT 1;
    
    -- Create DRAFT syllabus for gv.tran (subject 121033 - Trí tuệ nhân tạo)
    INSERT INTO core_service.syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'DRAFT', v_gv_tran_id, v_term_id
    FROM core_service.subjects s 
    WHERE s.code = '121033'
    AND NOT EXISTS (
        SELECT 1 FROM core_service.syllabus_versions sv 
        WHERE sv.subject_id = s.id AND sv.created_by = v_gv_tran_id AND sv.status = 'DRAFT'
    )
    RETURNING id INTO v_syllabus_tran;
    
    -- Create DRAFT syllabus for gv.hoang (subject 122043 - Thực tập chuyên ngành II)
    INSERT INTO core_service.syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'DRAFT', v_gv_hoang_id, v_term_id
    FROM core_service.subjects s 
    WHERE s.code = '122043'
    AND NOT EXISTS (
        SELECT 1 FROM core_service.syllabus_versions sv 
        WHERE sv.subject_id = s.id AND sv.created_by = v_gv_hoang_id AND sv.status = 'DRAFT'
    )
    RETURNING id INTO v_syllabus_hoang;
    
    RAISE NOTICE 'Created DRAFT syllabi: gv.tran=%, gv.hoang=%', v_syllabus_tran, v_syllabus_hoang;
END $$;
