/* V52__fix_duplicate_tracker_index.sql */
/* Fix conflict: V37 và V41 đều tạo index idx_tracker_student */
/* Migration này đảm bảo index tồn tại duy nhất */

BEGIN;

SET search_path TO core_service;

-- Drop index nếu tồn tại (để V41 có thể chạy lại mà không lỗi)
DROP INDEX IF EXISTS idx_tracker_student;

-- Tạo lại index với IF NOT EXISTS để đảm bảo idempotent
CREATE INDEX IF NOT EXISTS idx_tracker_student ON student_syllabus_tracker(student_id);

-- Log completion
DO $$
BEGIN
    RAISE NOTICE '✅ V52: Fixed duplicate index issue between V37 and V41';
END $$;

COMMIT;
