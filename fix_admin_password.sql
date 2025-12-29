-- Fix ALL user passwords to "123456"
-- NEW BCrypt hash for "123456": $2a$10$D16EJhIwRbodUTTDLFyI2ea4r47bCOewthIx2nQWM0jEzN2eB29jm

UPDATE core_service.users 
SET password_hash = '$2a$10$D16EJhIwRbodUTTDLFyI2ea4r47bCOewthIx2nQWM0jEzN2eB29jm';

-- Verify
SELECT email, LEFT(password_hash, 40) as hash_prefix, status FROM core_service.users ORDER BY email;
