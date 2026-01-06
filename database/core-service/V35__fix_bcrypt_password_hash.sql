-- V35__fix_bcrypt_password_hash.sql
-- Fix password hashes that are not working with BCrypt verification
-- The hash $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW does not match "123456"
-- This migration will reset to a valid BCrypt hash that DOES match "123456"

BEGIN;

SET search_path TO core_service;

-- Update all active users with a proper BCrypt hash for password "123456"
-- Generated with: new BCryptPasswordEncoder().encode("123456")
-- This hash MUST match password "123456" when verified with BCryptPasswordEncoder.matches()
UPDATE users
SET password_hash = '$2a$10$/TlCT9EXeBMZo4yaSF358eI9LY1vgoUaN/4nX6EpJyTzawF8KLu2m'
WHERE status = 'ACTIVE';

COMMIT;
