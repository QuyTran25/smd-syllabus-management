-- V30: Fix Vietnamese encoding for remaining review comments
-- Fix 4 comments with corrupted text

DO $$
BEGIN
    -- Fix GENERAL comment
    UPDATE core_service.review_comments
    SET content = 'Anh ơi, đề cương môn AI này cần bổ sung thêm phần thực hành về Deep Learning nhé.'
    WHERE section = 'GENERAL'
    AND content LIKE '%Anh ?i, ?? c??ng%';
    
    -- Fix PROJECT_REQUIREMENTS comment
    UPDATE core_service.review_comments
    SET content = 'Em đề xuất thêm yêu cầu về báo cáo tiến độ hàng tuần để giám sát sinh viên tốt hơn.'
    WHERE section = 'PROJECT_REQUIREMENTS'
    AND content LIKE '%Em ?? xu?t%';
    
    -- Fix MATERIALS comment
    UPDATE core_service.review_comments
    SET content = 'Tài liệu tham khảo cần cập nhật thêm các nguồn mới hơn, đặc biệt là các tài liệu từ năm 2024-2025.'
    WHERE section = 'MATERIALS'
    AND content LIKE '%T?i li?u tham kh?o%';
    
    -- Fix LEARNING_OUTCOMES comment
    UPDATE core_service.review_comments
    SET content = 'CLO 1 và CLO 2 có vẻ hơi trùng lặp nhau. Anh xem lại để làm rõ sự khác biệt giữa chúng.'
    WHERE section = 'LEARNING_OUTCOMES'
    AND content LIKE '%CLO 1 v? CLO 2%';
    
    RAISE NOTICE 'Fixed Vietnamese encoding for 4 remaining review comments';
END $$;
