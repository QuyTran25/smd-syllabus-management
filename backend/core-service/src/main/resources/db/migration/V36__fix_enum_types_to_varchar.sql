-- V36: Fix PostgreSQL ENUM types to VARCHAR for Hibernate compatibility
-- Vấn đề: Hibernate không mapping đúng với PostgreSQL custom ENUM types
-- Giải pháp: Chuyển sang VARCHAR với CHECK constraint

-- Step 0: Drop view that depends on these columns
DROP VIEW IF EXISTS core_service.v_syllabus_full CASCADE;

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

-- Step 6: Recreate the view
CREATE VIEW core_service.v_syllabus_full AS
SELECT 
    sv.*,
    s.code AS subject_code, 
    s.current_name_vi AS subject_name_vi,
    s.subject_type,
    s.component,
    s.default_theory_hours AS subject_theory_hours,
    s.default_practice_hours AS subject_practice_hours,
    s.default_self_study_hours AS subject_self_study_hours,
    d.name AS department_name, 
    d.code AS department_code,
    f.name AS faculty_name,
    f.code AS faculty_code,
    at.code AS term_code,
    at.name AS term_name,
    at.academic_year
FROM core_service.syllabus_versions sv
JOIN core_service.subjects s ON sv.subject_id = s.id
JOIN core_service.departments d ON s.department_id = d.id
JOIN core_service.faculties f ON d.faculty_id = f.id
LEFT JOIN core_service.academic_terms at ON sv.academic_term_id = at.id
WHERE sv.is_deleted = FALSE;
