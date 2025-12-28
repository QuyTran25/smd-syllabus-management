-- Fix admin password to Admin@123456
UPDATE core_service.users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'admin@smd.edu.vn';

SELECT email, LENGTH(password_hash) as password_length, is_active, status 
FROM core_service.users 
WHERE email = 'admin@smd.edu.vn';
