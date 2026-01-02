-- Quick verification for teaching assignments status
SELECT 
    s.code as "Mã môn",
    u.full_name as "Giáo viên chính",
    ta.status::text as "Trạng thái",
    COUNT(tac.id) as "Số GV cộng tác",
    CASE 
        WHEN sv.id IS NULL THEN 'Chưa có đề cương'
        WHEN sv.status = 'DRAFT' THEN 'Đang soạn (DRAFT)'
        WHEN sv.status = 'APPROVED' THEN 'Đã duyệt'
        ELSE sv.status::text
    END as "Đề cương",
    LEFT(ta.comments, 60) as "Nhận xét"
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id  
JOIN core_service.users u ON ta.main_lecturer_id = u.id
LEFT JOIN core_service.teaching_assignment_collaborators tac ON ta.id = tac.assignment_id
LEFT JOIN core_service.syllabus_versions sv ON ta.syllabus_version_id = sv.id
GROUP BY s.code, u.full_name, ta.status, sv.id, sv.status, ta.comments
ORDER BY 
    CASE ta.status::text
        WHEN 'in-progress' THEN 1
        WHEN 'submitted' THEN 2
        WHEN 'pending' THEN 3
        ELSE 4
    END,
    s.code;
