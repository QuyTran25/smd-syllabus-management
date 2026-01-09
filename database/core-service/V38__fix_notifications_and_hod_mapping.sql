/*
 * V38__fix_notifications_and_hod_mapping.sql
 * Mục tiêu: 
 * 1. Bổ sung cột cho bảng notifications
 * 2. Gán HOD với Department tương ứng
 */

SET search_path TO core_service;

-- ==========================================
-- 1. BỔ SUNG CỘT CHO BẢNG NOTIFICATIONS
-- ==========================================

-- Thêm cột related_entity_type nếu chưa có
ALTER TABLE notifications 
ADD COLUMN IF NOT EXISTS related_entity_type VARCHAR(50);

-- Thêm cột related_entity_id nếu chưa có
ALTER TABLE notifications 
ADD COLUMN IF NOT EXISTS related_entity_id UUID;

-- Thêm cột read_at nếu chưa có
ALTER TABLE notifications 
ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;

-- Tạo index cho related_entity
CREATE INDEX IF NOT EXISTS idx_notif_entity 
ON notifications(related_entity_type, related_entity_id);

-- ==========================================
-- 2. GÁN HOD VỚI DEPARTMENT TƯƠNG ỨNG
-- ==========================================

-- Cập nhật department_id cho HOD users dựa trên email pattern
-- hod.khmt@smd.edu.vn -> KHMT
-- hod.ktpm@smd.edu.vn -> KTPM
-- hod.httt@smd.edu.vn -> HTTT

UPDATE users u
SET department_id = d.id
FROM departments d
WHERE u.email LIKE 'hod.%@smd.edu.vn'
AND UPPER(SPLIT_PART(SPLIT_PART(u.email, '.', 2), '@', 1)) = d.code;

-- Cập nhật user_roles với scope_type = DEPARTMENT và scope_id = department_id
UPDATE user_roles
SET 
    scope_type = 'DEPARTMENT',
    scope_id = subq.department_id
FROM (
    SELECT ur.id as ur_id, u.department_id
    FROM user_roles ur
    JOIN users u ON ur.user_id = u.id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.code = 'HOD'
    AND u.department_id IS NOT NULL
    AND ur.scope_type IS NULL
) subq
WHERE user_roles.id = subq.ur_id;

-- ==========================================
-- 3. LOG SUMMARY
-- ==========================================
DO $$
DECLARE
    notif_cols INT;
    hod_with_dept INT;
BEGIN
    -- Đếm số cột trong notifications
    SELECT COUNT(*) INTO notif_cols 
    FROM information_schema.columns 
    WHERE table_schema = 'core_service' AND table_name = 'notifications';
    
    -- Đếm HOD đã được gán department
    SELECT COUNT(*) INTO hod_with_dept 
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.code = 'HOD' AND u.department_id IS NOT NULL;
    
    RAISE NOTICE '=== V38 Migration Summary ===';
    RAISE NOTICE 'Notifications table columns: %', notif_cols;
    RAISE NOTICE 'HOD with department assigned: %', hod_with_dept;
END $$;
