/*
 * V16__seed_student_account.sql
 * Tạo tài khoản sinh viên cho testing
 * Email: student@smd.edu.vn
 * Password: password
 */

BEGIN;

SET search_path TO core_service;

DO $$BEGIN RAISE NOTICE 'Starting V16 Migration: Seeding Student Account...'; END$$;

-- ==========================================
-- 1. INSERT ROLE STUDENT
-- ==========================================
INSERT INTO roles (code, name, description, is_system) VALUES 
('STUDENT', 'Student', 'Sinh viên', FALSE)
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 2. INSERT STUDENT USER
-- ==========================================
-- Password: "password"
-- BCrypt Hash: $2a$10$kQoXVMDcfVLLYj2P/T/FsOEtXnH32GDmyOeQXXStjA9gJiuh3GzH.

INSERT INTO users (
    email, password_hash, full_name, phone, gender, status, auth_provider, is_active
) VALUES 
('student@smd.edu.vn', '$2a$10$kQoXVMDcfVLLYj2P/T/FsOEtXnH32GDmyOeQXXStjA9gJiuh3GzH.', 
 'Sinh Viên Test', '0901234599', 'MALE', 'ACTIVE', 'LOCAL', true)
ON CONFLICT (email) DO NOTHING;

-- ==========================================
-- 3. ASSIGN ROLE STUDENT
-- ==========================================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'student@smd.edu.vn' AND r.code = 'STUDENT'
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. CREATE STUDENT PROFILE
-- ==========================================
INSERT INTO student_profiles (user_id, student_code, enrollment_year, curriculum_id)
SELECT u.id, 'SVTEST001', 2024, 
       (SELECT id FROM curriculums ORDER BY created_at DESC LIMIT 1)
FROM users u
WHERE u.email = 'student@smd.edu.vn'
ON CONFLICT (user_id) DO NOTHING;

-- ==========================================
-- 5. LOG SUMMARY
-- ==========================================
DO $$
DECLARE
    user_exists BOOLEAN;
    role_assigned BOOLEAN;
    profile_created BOOLEAN;
BEGIN
    -- Check user
    SELECT EXISTS(SELECT 1 FROM users WHERE email = 'student@smd.edu.vn') INTO user_exists;
    
    -- Check role
    SELECT EXISTS(
        SELECT 1 FROM users u 
        JOIN user_roles ur ON u.id = ur.user_id 
        JOIN roles r ON ur.role_id = r.id 
        WHERE u.email = 'student@smd.edu.vn' AND r.code = 'STUDENT'
    ) INTO role_assigned;
    
    -- Check profile
    SELECT EXISTS(
        SELECT 1 FROM student_profiles sp
        JOIN users u ON sp.user_id = u.id
        WHERE u.email = 'student@smd.edu.vn'
    ) INTO profile_created;
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'V16 Migration completed successfully!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Student Account Status:';
    RAISE NOTICE '  Email: student@smd.edu.vn';
    RAISE NOTICE '  Password: password';
    RAISE NOTICE '  User created: %', user_exists;
    RAISE NOTICE '  Role assigned: %', role_assigned;
    RAISE NOTICE '  Profile created: %', profile_created;
    RAISE NOTICE '================================';
END $$;

COMMIT;