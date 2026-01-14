-- V40: Assign faculty_id and department_id to all relevant users
-- Purpose: Fix empty lecturer dropdown and "You do not belong to any faculty" errors

-- Update all lecturers to belong to "Khoa Công nghệ Thông tin" (IT Faculty) and departments
UPDATE core_service.users
SET 
    faculty_id = (SELECT id FROM core_service.faculties WHERE code = 'FIT' LIMIT 1),
    department_id = (SELECT id FROM core_service.departments WHERE code = 'KHMT' LIMIT 1)
WHERE email IN (
    'gv.nguyen@smd.edu.vn',
    'gv.tran@smd.edu.vn',
    'gv.le@smd.edu.vn',
    'gv.pham@smd.edu.vn',
    'gv.hoang@smd.edu.vn',
    'gv.vo@smd.edu.vn'
);

-- Update HODs with their departments
UPDATE core_service.users
SET faculty_id = (SELECT id FROM core_service.faculties WHERE code = 'FIT' LIMIT 1)
WHERE email IN ('hod.khmt@smd.edu.vn', 'hod.ktpm@smd.edu.vn', 'hod.httt@smd.edu.vn');

-- Update AA and Principal with faculty
UPDATE core_service.users
SET faculty_id = (SELECT id FROM core_service.faculties WHERE code = 'FIT' LIMIT 1)
WHERE email IN ('aa@smd.edu.vn', 'principal@smd.edu.vn');

-- Verify the update (only if users exist)
DO $$
DECLARE
    lecturer_count INTEGER;
    hod_count INTEGER;
    admin_count INTEGER;
BEGIN
    -- Count lecturers with faculty/department
    SELECT COUNT(*) INTO lecturer_count
    FROM core_service.users
    WHERE email LIKE 'gv.%@smd.edu.vn'
    AND faculty_id IS NOT NULL
    AND department_id IS NOT NULL;
    
    -- Count HODs with faculty
    SELECT COUNT(*) INTO hod_count
    FROM core_service.users
    WHERE email LIKE 'hod.%@smd.edu.vn'
    AND faculty_id IS NOT NULL;
    
    -- Count AA/Principal with faculty
    SELECT COUNT(*) INTO admin_count
    FROM core_service.users
    WHERE email IN ('aa@smd.edu.vn', 'principal@smd.edu.vn')
    AND faculty_id IS NOT NULL;
    
    -- Log results
    IF lecturer_count > 0 OR hod_count > 0 OR admin_count > 0 THEN
        RAISE NOTICE 'Successfully assigned faculty: % lecturers, % HODs, % admins', 
            lecturer_count, hod_count, admin_count;
    ELSE
        RAISE NOTICE 'No users found to update (data not yet seeded)';
    END IF;
END $$;
