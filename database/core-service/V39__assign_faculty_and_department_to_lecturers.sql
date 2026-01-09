-- V39: Assign faculty_id and department_id to lecturer users
-- Purpose: Fix empty lecturer dropdown by associating lecturers with their faculty and department

-- Update all lecturers to belong to "Khoa Công nghệ Thông tin" (IT Faculty) and "Bộ môn Khoa học Máy tính" (CS Department)
UPDATE users
SET 
    faculty_id = (SELECT id FROM faculties WHERE code = 'CNTT' LIMIT 1),
    department_id = (SELECT id FROM departments WHERE code = 'KHMT' LIMIT 1)
WHERE email IN (
    'gv.nguyen@smd.edu.vn',
    'gv.tran@smd.edu.vn',
    'gv.le@smd.edu.vn',
    'gv.pham@smd.edu.vn',
    'gv.hoang@smd.edu.vn',
    'gv.vo@smd.edu.vn'
);

-- Verify the update was successful
DO $$
DECLARE
    lecturer_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO lecturer_count
    FROM users
    WHERE email LIKE 'gv.%@smd.edu.vn'
    AND faculty_id IS NOT NULL
    AND department_id IS NOT NULL;
    
    IF lecturer_count < 6 THEN
        RAISE EXCEPTION 'Expected 6 lecturers to be updated but only % were updated', lecturer_count;
    END IF;
    
    RAISE NOTICE 'Successfully assigned faculty and department to % lecturers', lecturer_count;
END $$;
