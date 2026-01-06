-- V23: Fix assignment status data (KEEP ENUM TYPE)
-- Note: Column is already correct type (assignment_status enum) from V8
-- This migration only updates existing data values if needed

BEGIN;

-- Update assignments with syllabus drafts to 'in-progress'
UPDATE core_service.teaching_assignments 
SET status = 'in-progress'
WHERE syllabus_version_id IN (
    SELECT sv.id 
    FROM core_service.syllabus_versions sv 
    WHERE sv.status = 'DRAFT'
)
AND status = 'pending';

-- Verify status distribution
SELECT 
    ta.status,
    COUNT(*) as count
FROM core_service.teaching_assignments ta
GROUP BY ta.status
ORDER BY ta.status;

COMMIT;
