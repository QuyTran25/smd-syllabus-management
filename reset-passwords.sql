-- Reset all passwords using pgcrypto
-- Enable pgcrypto extension for password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Update password for principal (password: 123456)
-- Using BCrypt with default cost factor
UPDATE core_service.users 
SET password_hash = crypt('123456', gen_salt('bf'))
WHERE email = 'principal@smd.edu.vn';

-- Update password for admin (password: Admin@123456)
UPDATE core_service.users 
SET password_hash = crypt('Admin@123456', gen_salt('bf'))
WHERE email = 'admin@smd.edu.vn';

-- Verify passwords are set
SELECT 
    email,
    full_name,
    LENGTH(password_hash) as hash_length,
    SUBSTRING(password_hash, 1, 7) as hash_prefix,
    is_active,
    status
FROM core_service.users 
WHERE email IN ('principal@smd.edu.vn', 'admin@smd.edu.vn')
ORDER BY email;
