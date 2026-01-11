/*
 * V39__check_notification_integrity.sql
 * Mục tiêu: Kiểm tra và đảm bảo tính toàn vẹn của notifications
 * - Kiểm tra notification có user_id hợp lệ
 * - Kiểm tra HOD assignment đúng department
 * - Log các vấn đề để debug
 */

SET search_path TO core_service;

-- ==========================================
-- 1. KIỂM TRA SỐ LƯỢNG HOD TRONG MỖI DEPARTMENT
-- ==========================================

DO $$
DECLARE
    v_dept RECORD;
    v_hod_count INT;
BEGIN
    RAISE NOTICE '=== KIỂM TRA HOD TRONG CÁC DEPARTMENT ===';
    
    FOR v_dept IN (SELECT id, name FROM departments WHERE is_active = TRUE)
    LOOP
        -- Đếm số HOD trong department
        SELECT COUNT(DISTINCT u.id) INTO v_hod_count
        FROM users u
        JOIN user_roles ur ON u.id = ur.user_id
        JOIN roles r ON ur.role_id = r.id
        WHERE u.department_id = v_dept.id
        AND r.code = 'HOD'
        AND u.status = 'ACTIVE';
        
        IF v_hod_count = 0 THEN
            RAISE WARNING 'Department "%" (ID: %) KHÔNG CÓ HOD nào!', v_dept.name, v_dept.id;
        ELSIF v_hod_count > 1 THEN
            RAISE WARNING 'Department "%" (ID: %) có % HOD (nên chỉ có 1)!', v_dept.name, v_dept.id, v_hod_count;
        ELSE
            RAISE NOTICE 'Department "%" (ID: %) có 1 HOD - OK', v_dept.name, v_dept.id;
        END IF;
    END LOOP;
END $$;

-- ==========================================
-- 2. KIỂM TRA NOTIFICATIONS HIỆN CÓ
-- ==========================================

DO $$
DECLARE
    v_notif_count INT;
    v_assignment_notif_count INT;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== KIỂM TRA NOTIFICATIONS ===';
    
    -- Tổng số notifications
    SELECT COUNT(*) INTO v_notif_count FROM notifications;
    RAISE NOTICE 'Tổng số notifications: %', v_notif_count;
    
    -- Số notifications type ASSIGNMENT
    SELECT COUNT(*) INTO v_assignment_notif_count 
    FROM notifications 
    WHERE type = 'ASSIGNMENT';
    RAISE NOTICE 'Số notifications ASSIGNMENT: %', v_assignment_notif_count;
    
    -- Liệt kê notifications ASSIGNMENT
    IF v_assignment_notif_count > 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '--- Chi tiết ASSIGNMENT Notifications ---';
        
        FOR v_rec IN (
            SELECT 
                n.id,
                n.title,
                u.full_name as recipient_name,
                u.email as recipient_email,
                r.code as recipient_role,
                d.name as recipient_dept,
                n.related_entity_type,
                n.created_at
            FROM notifications n
            JOIN users u ON n.user_id = u.id
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            LEFT JOIN departments d ON u.department_id = d.id
            WHERE n.type = 'ASSIGNMENT'
            ORDER BY n.created_at DESC
        )
        LOOP
            RAISE NOTICE '[%] % -> % (%) [Role: %, Dept: %] - Entity: %', 
                v_rec.created_at, 
                v_rec.title, 
                v_rec.recipient_name,
                v_rec.recipient_email,
                v_rec.recipient_role,
                COALESCE(v_rec.recipient_dept, 'N/A'),
                COALESCE(v_rec.related_entity_type, 'N/A');
        END LOOP;
    END IF;
END $$;

-- ==========================================
-- 3. KIỂM TRA USER ROLES - PHÁT HIỆN USER CÓ NHIỀU ROLE
-- ==========================================

DO $$
DECLARE
    v_user RECORD;
    v_role_count INT;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== KIỂM TRA USERS CÓ NHIỀU ROLES ===';
    
    FOR v_user IN (
        SELECT 
            u.id,
            u.full_name,
            u.email,
            COUNT(ur.role_id) as role_count,
            STRING_AGG(r.code, ', ' ORDER BY r.code) as roles
        FROM users u
        JOIN user_roles ur ON u.id = ur.user_id
        JOIN roles r ON ur.role_id = r.id
        WHERE u.status = 'ACTIVE'
        GROUP BY u.id, u.full_name, u.email
        HAVING COUNT(ur.role_id) > 1
        ORDER BY role_count DESC, u.email
    )
    LOOP
        RAISE WARNING 'User "%" (%) có % roles: [%]', 
            v_user.full_name, 
            v_user.email, 
            v_user.role_count,
            v_user.roles;
    END LOOP;
END $$;

-- ==========================================
-- 4. THÊM INDEX ĐỂ TỐI ƯU QUERY NOTIFICATIONS
-- ==========================================

-- Index cho việc filter notifications theo user và type
CREATE INDEX IF NOT EXISTS idx_notifications_user_type 
ON notifications(user_id, type) 
WHERE is_read = FALSE;

-- Index cho việc query notifications theo related entity
CREATE INDEX IF NOT EXISTS idx_notifications_related_entity 
ON notifications(related_entity_type, related_entity_id);

RAISE NOTICE '';
RAISE NOTICE '=== HOÀN TẤT KIỂM TRA ===';
RAISE NOTICE 'Đã thêm indexes để tối ưu query notifications';
