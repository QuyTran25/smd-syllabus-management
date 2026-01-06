/*
 * V25__seed_lecturer_realistic_data.sql
 * Purpose: Seed realistic data for Lecturer role
 * Created: 2026-01-02
 */

SET search_path TO core_service;

DO $$
DECLARE
    v_admin_id UUID;
    v_student_id UUID;
    v_hod_khmt_id UUID;
    v_hod_ktpm_id UUID;
    v_hod_httt_id UUID;
    v_aa1_id UUID;
    v_principal_id UUID;
    v_gv_nguyen_id UUID;
    v_gv_tran_id UUID;
    v_gv_le_id UUID;
    v_gv_pham_id UUID;
    v_gv_hoang_id UUID;
    v_gv_vo_id UUID;
    v_dept_khmt_id UUID;
    v_dept_ktpm_id UUID;
    v_dept_httt_id UUID;
    v_term_id UUID;
    v_syllabus_id UUID;

BEGIN
    RAISE NOTICE '=== V25: Starting Lecturer Data Seed ===';
    
    -- Get user IDs
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn';
    SELECT id INTO v_student_id FROM users WHERE email = 'student@smd.edu.vn';
    SELECT id INTO v_hod_khmt_id FROM users WHERE email = 'hod.khmt@smd.edu.vn';
    SELECT id INTO v_hod_ktpm_id FROM users WHERE email = 'hod.ktpm@smd.edu.vn';
    SELECT id INTO v_hod_httt_id FROM users WHERE email = 'hod.httt@smd.edu.vn';
    SELECT id INTO v_aa1_id FROM users WHERE email = 'aa1@smd.edu.vn';
    SELECT id INTO v_principal_id FROM users WHERE email = 'principal@smd.edu.vn';
    SELECT id INTO v_gv_nguyen_id FROM users WHERE email = 'gv.nguyen@smd.edu.vn';
    SELECT id INTO v_gv_tran_id FROM users WHERE email = 'gv.tran@smd.edu.vn';
    SELECT id INTO v_gv_le_id FROM users WHERE email = 'gv.le@smd.edu.vn';
    SELECT id INTO v_gv_pham_id FROM users WHERE email = 'gv.pham@smd.edu.vn';
    SELECT id INTO v_gv_hoang_id FROM users WHERE email = 'gv.hoang@smd.edu.vn';
    SELECT id INTO v_gv_vo_id FROM users WHERE email = 'gv.vo@smd.edu.vn';
    
    -- Get department IDs
    SELECT id INTO v_dept_khmt_id FROM departments WHERE code = 'KHMT';
    SELECT id INTO v_dept_ktpm_id FROM departments WHERE code = 'KTPM';
    SELECT id INTO v_dept_httt_id FROM departments WHERE code = 'HTTT';
    
    -- Get active term
    SELECT id INTO v_term_id FROM academic_terms WHERE is_active = TRUE LIMIT 1;
    
    -- Update lecturer profiles with departments
    UPDATE lecturer_profiles SET department_id = v_dept_khmt_id WHERE user_id = v_gv_nguyen_id;
    UPDATE lecturer_profiles SET department_id = v_dept_khmt_id WHERE user_id = v_gv_tran_id;
    UPDATE lecturer_profiles SET department_id = v_dept_ktpm_id WHERE user_id = v_gv_le_id;
    UPDATE lecturer_profiles SET department_id = v_dept_ktpm_id WHERE user_id = v_gv_pham_id;
    UPDATE lecturer_profiles SET department_id = v_dept_httt_id WHERE user_id = v_gv_hoang_id;
    UPDATE lecturer_profiles SET department_id = v_dept_httt_id WHERE user_id = v_gv_vo_id;
    
    RAISE NOTICE '✓ Assigned lecturers to departments';
    
    -- Create teaching assignments
    INSERT INTO teaching_assignments (subject_id, academic_term_id, main_lecturer_id, deadline, status, assigned_by)
    SELECT s.id, v_term_id, v_gv_nguyen_id, '2026-02-15', 'in-progress', v_hod_khmt_id
    FROM subjects s WHERE s.code = '122002' AND NOT EXISTS (
        SELECT 1 FROM teaching_assignments WHERE subject_id = s.id AND academic_term_id = v_term_id
    );
    
    INSERT INTO teaching_assignments (subject_id, academic_term_id, main_lecturer_id, deadline, status, assigned_by)
    SELECT s.id, v_term_id, v_gv_le_id, '2026-02-20', 'in-progress', v_hod_ktpm_id
    FROM subjects s WHERE s.code = '121031' AND NOT EXISTS (
        SELECT 1 FROM teaching_assignments WHERE subject_id = s.id AND academic_term_id = v_term_id
    );
    
    INSERT INTO teaching_assignments (subject_id, academic_term_id, main_lecturer_id, deadline, status, assigned_by)
    SELECT s.id, v_term_id, v_gv_hoang_id, '2026-02-25', 'in-progress', v_hod_httt_id
    FROM subjects s WHERE s.code = '121000' AND NOT EXISTS (
        SELECT 1 FROM teaching_assignments WHERE subject_id = s.id AND academic_term_id = v_term_id
    );
    
    RAISE NOTICE '✓ Created teaching assignments';
    
    -- Create DRAFT syllabi
    INSERT INTO syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'DRAFT', v_gv_nguyen_id, v_term_id
    FROM subjects s 
    WHERE s.code = '122002'
    AND NOT EXISTS (SELECT 1 FROM syllabus_versions sv WHERE sv.subject_id = s.id AND sv.created_by = v_gv_nguyen_id)
    RETURNING id INTO v_syllabus_id;
    
    -- Add collaborator
    IF v_syllabus_id IS NOT NULL THEN
        INSERT INTO syllabus_collaborators (syllabus_version_id, user_id, role)
        VALUES (v_syllabus_id, v_gv_tran_id, 'EDITOR');
        
        INSERT INTO review_comments (syllabus_version_id, section, content, created_by)
        VALUES (v_syllabus_id, 'CLO', 'Anh xem lại CLO 2 nhé, em thấy phần động từ hành động chưa rõ ràng lắm.', v_gv_tran_id);
    END IF;
    
    -- Create PENDING_HOD syllabus
    INSERT INTO syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id, submitted_at
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'PENDING_HOD', v_gv_le_id, v_term_id, NOW() - INTERVAL '1 day'
    FROM subjects s 
    WHERE s.code = '121031'
    AND NOT EXISTS (SELECT 1 FROM syllabus_versions sv WHERE sv.subject_id = s.id AND sv.created_by = v_gv_le_id);
    
    -- Create REJECTED syllabus
    INSERT INTO syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id, submitted_at
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'REJECTED', v_gv_hoang_id, v_term_id, NOW() - INTERVAL '5 days'
    FROM subjects s 
    WHERE s.code = '121000'
    AND NOT EXISTS (SELECT 1 FROM syllabus_versions sv WHERE sv.subject_id = s.id AND sv.created_by = v_gv_hoang_id AND sv.status = 'REJECTED')
    RETURNING id INTO v_syllabus_id;
    
    -- Add rejection comment
    IF v_syllabus_id IS NOT NULL THEN
        INSERT INTO approval_history (syllabus_version_id, actor_id, action, comment)
        VALUES (v_syllabus_id, v_hod_httt_id, 'REJECTED', 
                'CLO số 3 không rõ ràng về phương pháp đánh giá. Vui lòng bổ sung thang điểm chi tiết.');
    END IF;
    
    -- Create REVISION_IN_PROGRESS syllabus
    INSERT INTO syllabus_versions (
        subject_id, version_no, snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
        status, created_by, academic_term_id, submitted_at, is_edit_enabled, edit_enabled_by, edit_enabled_at
    )
    SELECT 
        s.id, 'v1.0', s.code, s.current_name_vi, s.current_name_en, s.default_credits,
        'REVISION_IN_PROGRESS', v_gv_nguyen_id, v_term_id, NOW() - INTERVAL '30 days',
        TRUE, v_admin_id, NOW() - INTERVAL '3 days'
    FROM subjects s 
    WHERE s.code = '124003'
    AND NOT EXISTS (SELECT 1 FROM syllabus_versions sv WHERE sv.subject_id = s.id AND sv.created_by = v_gv_nguyen_id AND sv.status = 'REVISION_IN_PROGRESS')
    RETURNING id INTO v_syllabus_id;
    
    -- Add student error report
    IF v_syllabus_id IS NOT NULL THEN
        INSERT INTO syllabus_error_reports (syllabus_version_id, user_id, description, status, type)
        VALUES (v_syllabus_id, v_student_id, 
                'Tài liệu tham khảo số 2 bị sai link, không thể tải xuống.', 
                'IN_REVIEW', 'ERROR');
    END IF;
    
    RAISE NOTICE '=== V25 Migration Completed ===';
    RAISE NOTICE '✓ Created 4 syllabi (DRAFT, PENDING_HOD, REJECTED, REVISION_IN_PROGRESS)';
    RAISE NOTICE '✓ Added collaborators and comments';
    
END $$;
