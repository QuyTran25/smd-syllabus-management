-- V24: Fix UTF-8 encoding for comments in teaching_assignments
-- This fixes the garbled Vietnamese text in comments

BEGIN;

-- Update comments for in-progress assignments with proper UTF-8 text
UPDATE core_service.teaching_assignments 
SET comments = 'GV Hoàng Văn Đức đang soạn phần mục tiêu môn học. GV cộng tác Trần Thị Lan đã review và góp ý về phần tài liệu tham khảo.'
WHERE main_lecturer_id = (SELECT id FROM core_service.users WHERE full_name = 'Hoàng Văn Đức')
  AND status = 'in-progress';

UPDATE core_service.teaching_assignments 
SET comments = 'GV Lê Hoàng Nam đã hoàn thành phần nội dung chương trình. Đang chờ GV Vũ Thị Ngọc review phần đánh giá.'
WHERE main_lecturer_id = (SELECT id FROM core_service.users WHERE full_name = 'Lê Hoàng Nam')
  AND status = 'in-progress';

-- Update pending assignments
UPDATE core_service.teaching_assignments 
SET comments = 'Chờ giáo viên bắt đầu soạn đề cương'
WHERE status = 'pending' AND (comments IS NULL OR comments = '');

-- Verify
SELECT 
    s.code,
    u.full_name,
    ta.status,
    LEFT(ta.comments, 50) as comment_preview
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
JOIN core_service.users u ON ta.main_lecturer_id = u.id
ORDER BY ta.status DESC;

COMMIT;
