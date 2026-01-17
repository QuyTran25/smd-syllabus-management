-- Apply foreign key fix for student_syllabus_tracker
BEGIN;

-- Add the new foreign key constraint
ALTER TABLE core_service.student_syllabus_tracker 
ADD CONSTRAINT fk_tracker_syllabus_version 
FOREIGN KEY (syllabus_id) REFERENCES core_service.syllabus_versions(id) ON DELETE CASCADE;

COMMIT;
