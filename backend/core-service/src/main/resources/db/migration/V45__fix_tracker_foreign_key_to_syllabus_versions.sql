/* V41__fix_tracker_foreign_key_to_syllabus_versions.sql */
BEGIN;

SET search_path TO core_service;

-- Drop the old foreign key constraint that references subjects
ALTER TABLE student_syllabus_tracker 
DROP CONSTRAINT fk_tracker_subject;

-- Add new foreign key constraint that references syllabus_versions
ALTER TABLE student_syllabus_tracker 
ADD CONSTRAINT fk_tracker_syllabus_version 
FOREIGN KEY (syllabus_id) REFERENCES syllabus_versions(id) ON DELETE CASCADE;

-- Update the column comment to reflect the change
COMMENT ON COLUMN student_syllabus_tracker.syllabus_id IS 'ID of the syllabus version that the student is tracking';

COMMIT;
