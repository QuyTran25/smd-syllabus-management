-- BCrypt hash for 'password123' (principal, admin)
UPDATE core_service.users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqP5TDBj4FQTVZpDhP.9m5M/JKJCy' 
WHERE email = 'principal@smd.edu.vn';

-- BCrypt hash for 'password' (student)
UPDATE core_service.users 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' 
WHERE email = 'student@smd.edu.vn';

-- BCrypt hash for 'password123' (admin)
UPDATE core_service.users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqP5TDBj4FQTVZpDhP.9m5M/JKJCy' 
WHERE email = 'admin@smd.edu.vn';

-- Verify
SELECT email, LEFT(password_hash, 30) as hash_start FROM core_service.users 
WHERE email IN ('principal@smd.edu.vn', 'student@smd.edu.vn', 'admin@smd.edu.vn');
