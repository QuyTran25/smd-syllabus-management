-- V34__reset_bcrypt_hashes.sql
-- Reset password hashes to ensure BCrypt works correctly
-- ALL USERS PASSWORD: 123456
-- BCrypt hash generated: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
-- This is the correct hash that matches the password "123456"

BEGIN;

SET search_path TO core_service;

-- Update all test users with the correct password hash
UPDATE users
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
    'gv.vo@smd.edu.vn',
    'lecturer.khmt.1@smd.edu.vn',
    'lecturer.khmt.2@smd.edu.vn',
    'student.khmt.1@smd.edu.vn',
    'student.khmt.2@smd.edu.vn'
);

DO $$BEGIN RAISE NOTICE 'V34 Migration Completed: Updated password hashes for all users'; END$$;

COMMIT;
