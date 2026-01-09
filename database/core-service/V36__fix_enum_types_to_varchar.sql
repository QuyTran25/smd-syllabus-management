-- V36: Fix PostgreSQL ENUM types to VARCHAR for Hibernate compatibility
-- Vấn đề: Hibernate không mapping đúng với PostgreSQL custom ENUM types
-- Giải pháp: Chuyển sang VARCHAR với CHECK constraint

-- Step 1: Add temporary columns
ALTER TABLE core_service.syllabus_versions 
    ADD COLUMN component_type_new VARCHAR(20),
    ADD COLUMN course_type_new VARCHAR(20);

-- Step 2: Copy data to new columns
UPDATE core_service.syllabus_versions 
SET component_type_new = component_type::text,
    course_type_new = course_type::text;

-- Step 3: Drop old columns
ALTER TABLE core_service.syllabus_versions 
    DROP COLUMN component_type,
    DROP COLUMN course_type;

-- Step 4: Rename new columns
ALTER TABLE core_service.syllabus_versions 
    RENAME COLUMN component_type_new TO component_type;
ALTER TABLE core_service.syllabus_versions 
    RENAME COLUMN course_type_new TO course_type;

-- Step 5: Add CHECK constraints
ALTER TABLE core_service.syllabus_versions 
    ADD CONSTRAINT chk_component_type 
    CHECK (component_type IN ('major', 'foundation', 'general', 'thesis'));

ALTER TABLE core_service.syllabus_versions 
    ADD CONSTRAINT chk_course_type 
    CHECK (course_type IN ('required', 'elective', 'optional'));

-- Set default values
ALTER TABLE core_service.syllabus_versions 
    ALTER COLUMN component_type SET DEFAULT 'major';

ALTER TABLE core_service.syllabus_versions 
    ALTER COLUMN course_type SET DEFAULT 'required';
