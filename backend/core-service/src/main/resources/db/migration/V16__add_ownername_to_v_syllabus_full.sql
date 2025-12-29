-- Add owner full name to v_syllabus_full view
SET search_path TO core_service, public;

DROP VIEW IF EXISTS v_syllabus_full;

CREATE VIEW v_syllabus_full AS
SELECT
  sv.id,
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
  sv.course_type,
  sv.component_type,
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
  s.code AS subject_code,
  s.current_name_vi AS subject_name_vi,
  s.subject_type,
  s.component,
  s.default_theory_hours AS subject_theory_hours,
  d.name AS department_name,
  f.name AS faculty_name,
  at.code AS term_code,
  at.name AS term_name,
  at.academic_year,
  u.full_name AS owner_full_name
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id
JOIN departments d ON s.department_id = d.id
JOIN faculties f ON d.faculty_id = f.id
LEFT JOIN academic_terms at ON sv.academic_term_id = at.id
LEFT JOIN users u ON sv.created_by = u.id
WHERE sv.is_deleted = false;
