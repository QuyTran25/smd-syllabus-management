-- ==========================================
-- COMPLETE DATABASE FIX SCRIPT
-- Fix encoding + Assign roles + Update password
-- ==========================================

BEGIN;

SET search_path TO core_service;

-- ==========================================
-- 1. FIX ENCODING - Users
-- ==========================================
UPDATE users SET full_name = 'Lê Hoàng Nam' WHERE email = 'gv.le@smd.edu.vn';
UPDATE users SET full_name = 'Nguyễn Minh Tuấn' WHERE email = 'gv.nguyen@smd.edu.vn';
UPDATE users SET full_name = 'Phạm Thị Hoa' WHERE email = 'gv.pham@smd.edu.vn';
UPDATE users SET full_name = 'Trần Thị Lan' WHERE email = 'gv.tran@smd.edu.vn';
UPDATE users SET full_name = 'Võ Thị Ngọc' WHERE email = 'gv.vo@smd.edu.vn';

-- ==========================================
-- 2. FIX ENCODING - Departments
-- ==========================================
UPDATE departments SET name = 'Kỹ thuật Phần mềm' WHERE code = 'KTPM';
UPDATE departments SET name = 'Khoa học Máy tính' WHERE code = 'KHMT';
UPDATE departments SET name = 'Hệ thống Thông tin' WHERE code = 'HTTT';

-- ==========================================
-- 3. UPDATE PASSWORD for principal@smd.edu.vn
-- Using BCrypt hash of "123456": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- ==========================================
UPDATE users 
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'principal@smd.edu.vn';

-- Also update other users to use password "123456" for easier testing
UPDATE users 
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email IN (
    'admin@smd.edu.vn',
    'aa1@smd.edu.vn', 'aa2@smd.edu.vn',
    'hod.khmt@smd.edu.vn', 'hod.ktpm@smd.edu.vn', 'hod.httt@smd.edu.vn',
    'gv.nguyen@smd.edu.vn', 'gv.tran@smd.edu.vn', 'gv.le@smd.edu.vn',
    'gv.pham@smd.edu.vn', 'gv.hoang@smd.edu.vn', 'gv.vo@smd.edu.vn'
);

-- ==========================================
-- 4. ASSIGN ROLES TO USERS
-- ==========================================

-- Delete existing user_roles first (in case there are any)
DELETE FROM user_roles;

-- Assign roles using CTE
WITH 
    user_data AS (
        SELECT id, email FROM users 
        WHERE email IN (
            'admin@smd.edu.vn', 'principal@smd.edu.vn', 
            'aa1@smd.edu.vn', 'aa2@smd.edu.vn',
            'hod.khmt@smd.edu.vn', 'hod.ktpm@smd.edu.vn', 'hod.httt@smd.edu.vn',
            'gv.nguyen@smd.edu.vn', 'gv.tran@smd.edu.vn', 'gv.le@smd.edu.vn',
            'gv.pham@smd.edu.vn', 'gv.hoang@smd.edu.vn', 'gv.vo@smd.edu.vn'
        )
    ),
    role_data AS (
        SELECT id, code FROM roles 
        WHERE code IN ('ADMIN', 'PRINCIPAL', 'AA', 'HOD', 'LECTURER')
    )
INSERT INTO user_roles (user_id, role_id, scope_type, scope_id)
SELECT 
    u.id, 
    r.id,
    'GLOBAL',
    NULL
FROM user_data u
CROSS JOIN role_data r
WHERE 
    (u.email = 'admin@smd.edu.vn' AND r.code = 'ADMIN') OR
    (u.email = 'principal@smd.edu.vn' AND r.code = 'PRINCIPAL') OR
    (u.email LIKE 'aa%@smd.edu.vn' AND r.code = 'AA') OR
    (u.email LIKE 'hod%@smd.edu.vn' AND r.code = 'HOD') OR
    (u.email LIKE 'gv%@smd.edu.vn' AND r.code = 'LECTURER');

-- ==========================================
-- 5. VERIFY RESULTS
-- ==========================================

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'DATABASE FIX COMPLETED';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Users with fixed encoding: %', (SELECT COUNT(*) FROM users WHERE full_name !~ '[?]');
    RAISE NOTICE 'Departments with fixed encoding: %', (SELECT COUNT(*) FROM departments WHERE name !~ '[?]');
    RAISE NOTICE 'Users with roles assigned: %', (SELECT COUNT(DISTINCT user_id) FROM user_roles);
    RAISE NOTICE 'Total user_roles records: %', (SELECT COUNT(*) FROM user_roles);
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Password updated for all users to: 123456';
    RAISE NOTICE '========================================';
END $$;

-- Show sample data
SELECT 'USERS WITH ROLES:' as info;
SELECT u.email, u.full_name, r.code as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
ORDER BY r.code, u.email;

COMMIT;
