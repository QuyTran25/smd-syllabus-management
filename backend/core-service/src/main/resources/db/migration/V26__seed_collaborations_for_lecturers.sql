-- V26: Seed collaboration data for lecturers
-- Create collaboration assignments where lecturers review each other's syllabi

DO $$
DECLARE
    v_gv_nguyen_id UUID;
    v_gv_tran_id UUID;
    v_gv_le_id UUID;
    v_gv_hoang_id UUID;
    v_syllabus_tran_draft UUID;
    v_syllabus_le_draft UUID;
BEGIN
    -- Get user IDs
    SELECT id INTO v_gv_nguyen_id FROM core_service.users WHERE email = 'gv.nguyen@smd.edu.vn';
    SELECT id INTO v_gv_tran_id FROM core_service.users WHERE email = 'gv.tran@smd.edu.vn';
    SELECT id INTO v_gv_le_id FROM core_service.users WHERE email = 'gv.le@smd.edu.vn';
    SELECT id INTO v_gv_hoang_id FROM core_service.users WHERE email = 'gv.hoang@smd.edu.vn';
    
    -- Find DRAFT syllabi created by other lecturers
    -- gv.tran's DRAFT syllabus (should exist from V25)
    SELECT id INTO v_syllabus_tran_draft 
    FROM core_service.syllabus_versions 
    WHERE created_by = v_gv_tran_id 
    AND status = 'DRAFT' 
    LIMIT 1;
    
    -- gv.le's DRAFT syllabus (should exist from V25)
    SELECT id INTO v_syllabus_le_draft 
    FROM core_service.syllabus_versions 
    WHERE created_by = v_gv_le_id 
    AND status = 'DRAFT' 
    LIMIT 1;
    
    -- Assign gv.nguyen as VIEWER for gv.tran's DRAFT syllabus
    IF v_syllabus_tran_draft IS NOT NULL THEN
        INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
        VALUES (v_syllabus_tran_draft, v_gv_nguyen_id, 'VIEWER')
        ON CONFLICT DO NOTHING;
        
        -- Add a sample review comment from gv.nguyen
        INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
        VALUES (v_syllabus_tran_draft, 'GENERAL', 'Đề cương nhìn chung rất tốt. Tôi sẽ xem xét kỹ hơn và góp ý chi tiết trong vài ngày tới.', v_gv_nguyen_id)
        ON CONFLICT DO NOTHING;
    END IF;
    
    -- Assign gv.nguyen as EDITOR for gv.le's DRAFT syllabus
    IF v_syllabus_le_draft IS NOT NULL THEN
        INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
        VALUES (v_syllabus_le_draft, v_gv_nguyen_id, 'EDITOR')
        ON CONFLICT DO NOTHING;
        
        -- Add a sample edit comment from gv.nguyen
        INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
        VALUES (v_syllabus_le_draft, 'ASSESSMENT', 'Tôi đề xuất thay đổi tỷ lệ điểm giữa kỳ từ 30% lên 40% để phù hợp hơn với mục tiêu học tập.', v_gv_nguyen_id)
        ON CONFLICT DO NOTHING;
    END IF;
    
    -- Assign gv.hoang as VIEWER for gv.nguyen's DRAFT syllabus (122002)
    INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
    SELECT sv.id, v_gv_hoang_id, 'VIEWER'
    FROM core_service.syllabus_versions sv
    JOIN core_service.subjects s ON sv.subject_id = s.id
    WHERE sv.created_by = v_gv_nguyen_id 
    AND sv.status = 'DRAFT'
    AND s.code = '122002'
    ON CONFLICT DO NOTHING;
    
    -- Add comment from gv.hoang
    INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
    SELECT sv.id, 'LEARNING_OUTCOMES', 'CLO 1 và CLO 2 có vẻ hơi trùng lặp nhau. Anh xem lại để làm rõ sự khác biệt giữa chúng.', v_gv_hoang_id
    FROM core_service.syllabus_versions sv
    JOIN core_service.subjects s ON sv.subject_id = s.id
    WHERE sv.created_by = v_gv_nguyen_id 
    AND sv.status = 'DRAFT'
    AND s.code = '122002'
    ON CONFLICT DO NOTHING;
    
    RAISE NOTICE 'Collaboration assignments seeded successfully';
END $$;
