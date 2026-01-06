-- V28: Update collaboration assignments with new DRAFT syllabi
-- Assign gv.nguyen as collaborator for newly created DRAFT syllabi

DO $$
DECLARE
    v_gv_nguyen_id UUID;
    v_gv_pham_id UUID;
    v_syllabus_tran UUID;
    v_syllabus_hoang UUID;
BEGIN
    -- Get user IDs
    SELECT id INTO v_gv_nguyen_id FROM core_service.users WHERE email = 'gv.nguyen@smd.edu.vn';
    SELECT id INTO v_gv_pham_id FROM core_service.users WHERE email = 'gv.pham@smd.edu.vn';
    
    -- Find newly created DRAFT syllabi
    SELECT sv.id INTO v_syllabus_tran 
    FROM core_service.syllabus_versions sv
    JOIN core_service.subjects s ON sv.subject_id = s.id
    JOIN core_service.users u ON sv.created_by = u.id
    WHERE u.email = 'gv.tran@smd.edu.vn' 
    AND sv.status = 'DRAFT'
    AND s.code = '121033'
    LIMIT 1;
    
    SELECT sv.id INTO v_syllabus_hoang 
    FROM core_service.syllabus_versions sv
    JOIN core_service.subjects s ON sv.subject_id = s.id
    JOIN core_service.users u ON sv.created_by = u.id
    WHERE u.email = 'gv.hoang@smd.edu.vn' 
    AND sv.status = 'DRAFT'
    AND s.code = '122043'
    LIMIT 1;
    
    -- Assign gv.nguyen as VIEWER for gv.tran's new DRAFT syllabus (121033 - Trí tuệ nhân tạo)
    IF v_syllabus_tran IS NOT NULL THEN
        INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
        VALUES (v_syllabus_tran, v_gv_nguyen_id, 'VIEWER')
        ON CONFLICT DO NOTHING;
        
        -- Add review comment
        INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
        VALUES (v_syllabus_tran, 'GENERAL', 'Anh ơi, đề cương môn AI này cần bổ sung thêm phần thực hành về Deep Learning nhé.', v_gv_nguyen_id)
        ON CONFLICT DO NOTHING;
    END IF;
    
    -- Assign gv.nguyen as EDITOR for gv.hoang's DRAFT syllabus (122043 - Thực tập chuyên ngành II)
    IF v_syllabus_hoang IS NOT NULL THEN
        INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
        VALUES (v_syllabus_hoang, v_gv_nguyen_id, 'EDITOR')
        ON CONFLICT DO NOTHING;
        
        -- Add edit comment
        INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
        VALUES (v_syllabus_hoang, 'PROJECT_REQUIREMENTS', 'Em đề xuất thêm yêu cầu về báo cáo tiến độ hàng tuần để giám sát sinh viên tốt hơn.', v_gv_nguyen_id)
        ON CONFLICT DO NOTHING;
    END IF;
    
    -- Also assign gv.pham as VIEWER for gv.nguyen's existing DRAFT (122002)
    IF v_gv_pham_id IS NOT NULL THEN
        INSERT INTO core_service.syllabus_collaborators (syllabus_version_id, user_id, role)
        SELECT sv.id, v_gv_pham_id, 'VIEWER'
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
        JOIN core_service.users u ON sv.created_by = u.id
        WHERE u.email = 'gv.nguyen@smd.edu.vn'
        AND sv.status = 'DRAFT'
        AND s.code = '122002'
        ON CONFLICT DO NOTHING;
        
        -- Add comment from gv.pham
        INSERT INTO core_service.review_comments (syllabus_version_id, section, content, created_by)
        SELECT sv.id, 'MATERIALS', 'Tài liệu tham khảo cần cập nhật thêm các nguồn mới hơn, đặc biệt là các tài liệu từ năm 2024-2025.', v_gv_pham_id
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
        JOIN core_service.users u ON sv.created_by = u.id
        WHERE u.email = 'gv.nguyen@smd.edu.vn'
        AND sv.status = 'DRAFT'
        AND s.code = '122002'
        ON CONFLICT DO NOTHING;
    END IF;
    
    RAISE NOTICE 'Updated collaboration assignments for new DRAFT syllabi';
END $$;
