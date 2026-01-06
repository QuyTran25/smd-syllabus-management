-- ==========================================
-- V33__fix_password_hash.sql
-- Reset admin password to correct BCrypt hash (round 10)
-- Password: 123456
-- Hash: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
-- ==========================================

UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'
WHERE email IN (
    'admin@smd.edu.vn', 
    'principal@smd.edu.vn', 
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

-- Update student password as well (if exists)
UPDATE core_service.users 
SET password_hash = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'
WHERE email LIKE 'student%@smd.edu.vn';

SELECT 'Password hash fixed!' AS status;
