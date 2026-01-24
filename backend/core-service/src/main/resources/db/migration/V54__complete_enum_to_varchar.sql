-- V54: Complete ENUM to VARCHAR conversion
-- Fix all remaining ENUM columns for full Hibernate compatibility

-- Drop views that depend on columns being modified
DROP VIEW IF EXISTS core_service.v_syllabus_full CASCADE;

-- 1. Users table
ALTER TABLE core_service.users 
    ALTER COLUMN auth_provider TYPE VARCHAR(20) USING auth_provider::text,
    ALTER COLUMN gender TYPE VARCHAR(20) USING gender::text,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- 2. Approval history
ALTER TABLE core_service.approval_history 
    ALTER COLUMN action TYPE VARCHAR(50) USING action::text,
    ALTER COLUMN actor_role TYPE VARCHAR(50) USING actor_role::text;

-- 3. CLO-PI mappings
ALTER TABLE core_service.clo_pi_mappings 
    ALTER COLUMN level TYPE VARCHAR(20) USING level::text;

-- 4. PLOs
ALTER TABLE core_service.plos 
    ALTER COLUMN category TYPE VARCHAR(50) USING category::text;

-- 5. Revision sessions
ALTER TABLE core_service.revision_sessions 
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- 6. Subject relationships
ALTER TABLE core_service.subject_relationships 
    ALTER COLUMN type TYPE VARCHAR(50) USING type::text;

-- 7. Syllabus collaborators
ALTER TABLE core_service.syllabus_collaborators 
    ALTER COLUMN role TYPE VARCHAR(50) USING role::text;

-- 8. Syllabus version history
ALTER TABLE core_service.syllabus_version_history 
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- 9. Syllabus versions
ALTER TABLE core_service.syllabus_versions 
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- 10. Teaching assignments
ALTER TABLE core_service.teaching_assignments 
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- Recreate view v_syllabus_full
CREATE OR REPLACE VIEW core_service.v_syllabus_full AS
SELECT 
    sv.*,
    s.subject_code,
    s.subject_name_vi,
    s.subject_name_en,
    s.credit_count,
    at.term_code,
    at.year
FROM core_service.syllabus_versions sv
LEFT JOIN core_service.subjects s ON sv.subject_id = s.id
LEFT JOIN core_service.academic_terms at ON sv.academic_term_id = at.id
WHERE sv.is_deleted = false;
