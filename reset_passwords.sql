-- Reset passwords for all demo accounts
-- password: 123456
-- BCrypt hash: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW

UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' 
WHERE email = 'principal@smd.edu.vn';

UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' 
WHERE email = 'admin@smd.edu.vn';

UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' 
WHERE email = 'student@smd.edu.vn';

SELECT email, full_name, password_hash FROM core_service.users 
WHERE email IN ('principal@smd.edu.vn', 'student@smd.edu.vn', 'admin@smd.edu.vn');
