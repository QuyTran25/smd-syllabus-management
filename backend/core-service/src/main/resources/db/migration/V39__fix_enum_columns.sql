-- V39: Fix enum columns to use VARCHAR instead of PostgreSQL ENUM
-- This allows JPA EnumType.STRING to work correctly

-- 1. Drop view that depends on component column
DROP VIEW IF EXISTS core_service.v_syllabus_full;

-- 2. Fix component column in subjects table
ALTER TABLE core_service.subjects 
    ALTER COLUMN component TYPE VARCHAR(20) USING component::VARCHAR;

-- 3. Fix subject_type column in subjects table  
ALTER TABLE core_service.subjects 
    ALTER COLUMN subject_type TYPE VARCHAR(20) USING subject_type::VARCHAR;

-- 4. Set default values
ALTER TABLE core_service.subjects 
    ALTER COLUMN component SET DEFAULT 'BOTH';

ALTER TABLE core_service.subjects 
    ALTER COLUMN subject_type SET DEFAULT 'REQUIRED';

-- 5. Recreate the view
CREATE VIEW core_service.v_syllabus_full AS
SELECT sv.id,
    sv.subject_id,
    sv.academic_term_id,
    sv.version_no,
    sv.status,
    sv.previous_version_id,
    sv.review_deadline,
    sv.snap_subject_code,
    sv.snap_subject_name_vi,
    sv.snap_subject_name_en,
    sv.snap_credit_count,
    sv.keywords,
    sv.content,
    sv.approved_by,
    sv.created_by,
    sv.updated_by,
    sv.published_at,
    sv.is_deleted,
    sv.created_at,
    sv.updated_at,
    sv.effective_date,
    sv.unpublished_at,
    sv.unpublished_by,
    sv.unpublish_reason,
    sv.is_edit_enabled,
    sv.edit_enabled_by,
    sv.edit_enabled_at,
    sv.workflow_id,
    sv.current_approval_step,
    sv.theory_hours,
    sv.practice_hours,
    sv.self_study_hours,
    sv.grading_scale_id,
    sv.student_duties,
    sv.submitted_at,
    sv.hod_approved_at,
    sv.hod_approved_by,
    sv.aa_approved_at,
    sv.aa_approved_by,
    sv.principal_approved_at,
    sv.principal_approved_by,
    sv.description,
    sv.objectives,
    sv.student_tasks,
    sv.component_type,
    sv.course_type,
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
WHERE sv.is_deleted = false;

-- Summary
DO $$
BEGIN
    RAISE NOTICE '=== V39 Migration Summary ===';
    RAISE NOTICE 'Converted subject columns to VARCHAR for JPA compatibility';
    RAISE NOTICE 'Recreated v_syllabus_full view';
END $$;
