-- V23: Fix PostgreSQL enum to match Java enum names
-- This fixes the mismatch between DB enum values and Java enum names

BEGIN;

-- Drop the column and recreate as VARCHAR
ALTER TABLE core_service.teaching_assignments 
DROP COLUMN status;

ALTER TABLE core_service.teaching_assignments 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending';

-- Update with correct values from before
UPDATE core_service.teaching_assignments 
SET status = 'in-progress'
WHERE id IN (
    SELECT id FROM core_service.teaching_assignments ta
    WHERE ta.syllabus_version_id IN (
        SELECT sv.id FROM core_service.syllabus_versions sv WHERE sv.status = 'DRAFT'
    )
);

-- Verify
SELECT s.code, u.full_name, ta.status
FROM core_service.teaching_assignments ta
JOIN core_service.subjects s ON ta.subject_id = s.id
JOIN core_service.users u ON ta.main_lecturer_id = u.id
ORDER BY ta.status DESC
LIMIT 10;

COMMIT;
