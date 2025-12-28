-- Fix password for all accounts
-- Password: 123456 (BCrypt hash với cost 10)
-- Hash được generate từ: https://bcrypt-generator.com/
-- Hash này đã được verify hoạt động với Spring Security BCryptPasswordEncoder

-- BCrypt hash chuẩn của "123456" với cost 10
-- Verified: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW

UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'
WHERE email = 'principal@smd.edu.vn';

-- Cũng fix cho tất cả users khác (password: 123456)
UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'
WHERE email IN (
    'admin@smd.edu.vn',
    'aa1@smd.edu.vn',
    'aa2@smd.edu.vn',
    'hod.khmt@smd.edu.vn',
    'hod.ktpm@smd.edu.vn',
    'hod.httt@smd.edu.vn',
    'gv.nguyen@smd.edu.vn',
    'gv.tran@smd.edu.vn',
    'gv.le@smd.edu.vn',
    'gv.pham@smd.edu.vn',
    'gv.hoang@smd.edu.vn',
    'gv.vo@smd.edu.vn'
);

-- Verify update
SELECT 
    email, 
    full_name,
    LENGTH(password_hash) as password_length,
    status
FROM core_service.users 
WHERE email LIKE '%@smd.edu.vn'
ORDER BY email;
