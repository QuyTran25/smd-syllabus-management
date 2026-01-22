/*
 * V51__fix_notification_urls.sql
 * Mục tiêu: Fix actionUrl trong notifications cũ
 * - Thay /hod/syllabi/ → /admin/syllabi/
 * - Thay /principal/syllabi/ → /admin/syllabi/
 */

SET search_path TO core_service;

-- 1. Update HOD notification URLs
UPDATE notifications 
SET payload = jsonb_set(
    payload, 
    '{actionUrl}', 
    to_jsonb(replace(payload->>'actionUrl', '/hod/syllabi/', '/admin/syllabi/'))
)
WHERE payload->>'actionUrl' LIKE '/hod/syllabi/%';

-- 2. Update Principal notification URLs
UPDATE notifications 
SET payload = jsonb_set(
    payload, 
    '{actionUrl}', 
    to_jsonb(replace(payload->>'actionUrl', '/principal/syllabi/', '/admin/syllabi/'))
)
WHERE payload->>'actionUrl' LIKE '/principal/syllabi/%';

-- 3. Log results
DO $$ 
DECLARE
    hod_count INTEGER;
    principal_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO hod_count 
    FROM notifications 
    WHERE payload->>'actionUrl' LIKE '/admin/syllabi/%' 
    AND type IN ('SYLLABUS_REVIEW', 'SYLLABUS_REJECTED_NOTIFICATION', 'SYLLABUS_PROGRESS');
    
    SELECT COUNT(*) INTO principal_count 
    FROM notifications 
    WHERE payload->>'actionUrl' LIKE '/admin/syllabi/%' 
    AND type = 'SYLLABUS_PRINCIPAL_REVIEW';
    
    RAISE NOTICE '✅ Migration V51 completed';
    RAISE NOTICE 'Updated HOD notifications: %', hod_count;
    RAISE NOTICE 'Updated Principal notifications: %', principal_count;
END $$;
