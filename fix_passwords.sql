-- Reset tất cả password về "password" 
-- BCrypt hash for "password": $2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIVEmgOOwW

UPDATE core_service.users 
SET password_hash = '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIVEmgOOwW';

-- Verify
SELECT email, full_name, '✓ password = "password"' as note 
FROM core_service.users 
WHERE email LIKE '%@smd.edu.vn'
ORDER BY email;
