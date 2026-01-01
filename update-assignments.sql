UPDATE core_service.teaching_assignments 
SET main_lecturer_id = '6c2c659f-bceb-4d45-ad08-e357e8607a1f',
    assigned_by = (SELECT id FROM core_service.users WHERE email = 'tbm@smd.edu.vn' LIMIT 1)
WHERE id = (SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 1 OFFSET 0);

UPDATE core_service.teaching_assignments 
SET main_lecturer_id = '32d486d8-3329-40c9-8570-7d9446a8f3c1',
    assigned_by = (SELECT id FROM core_service.users WHERE email = 'tbm@smd.edu.vn' LIMIT 1)
WHERE id = (SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 1 OFFSET 1);

UPDATE core_service.teaching_assignments 
SET main_lecturer_id = '7a6a1ff9-0853-4e0b-bf73-69afd427789a',
    assigned_by = (SELECT id FROM core_service.users WHERE email = 'tbm@smd.edu.vn' LIMIT 1)
WHERE id = (SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 1 OFFSET 2);

UPDATE core_service.teaching_assignments 
SET status = 'in-progress',
    comments = 'GV Hoàng Văn Đức đang soạn phần mục tiêu môn học. GV cộng tác Trần Thị Lan đã review và góp ý về phần tài liệu tham khảo.'
WHERE id = (SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 1 OFFSET 0);

UPDATE core_service.teaching_assignments 
SET status = 'in-progress',
    comments = 'GV Lê Hoàng Nam đã hoàn thành phần nội dung chương trình. Đang chờ GV Võ Thị Ngọc review phần đánh giá.'
WHERE id = (SELECT id FROM core_service.teaching_assignments ORDER BY id LIMIT 1 OFFSET 1);

SELECT ta.id, s.code, u.full_name, ta.status::text, COUNT(tac.id) as collab_count, 
       SUBSTRING(ta.comments, 1, 50) as comment_preview
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
JOIN core_service.users u ON ta.main_lecturer_id = u.id
LEFT JOIN core_service.teaching_assignment_collaborators tac ON ta.id = tac.assignment_id
GROUP BY ta.id, s.code, u.full_name, ta.status, ta.comments
ORDER BY ta.status DESC, s.code
LIMIT 10;
