-- V29: Fix Vietnamese encoding in review_comments table
-- Replace corrupted text with properly encoded Vietnamese

DO $$
DECLARE
    v_gv_nguyen_id UUID;
BEGIN
    -- Get user ID
    SELECT id INTO v_gv_nguyen_id FROM core_service.users WHERE email = 'gv.nguyen@smd.edu.vn';
    
    -- Fix the corrupted comment with proper Vietnamese encoding
    UPDATE core_service.review_comments
    SET content = 'Tôi đề xuất thay đổi tỷ lệ điểm giữa kỳ từ 30% lên 40% để phù hợp hơn với mục tiêu học tập.'
    WHERE created_by = v_gv_nguyen_id
    AND section = 'ASSESSMENT'
    AND content LIKE '%T?i ?? xu?t%';
    
    RAISE NOTICE 'Fixed Vietnamese encoding in review comments';
END $$;
