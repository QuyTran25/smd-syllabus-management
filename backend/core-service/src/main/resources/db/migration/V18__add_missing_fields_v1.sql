-- Thêm cột student_tasks vào bảng syllabus_versions
ALTER TABLE core_service.syllabus_versions 
ADD COLUMN IF NOT EXISTS student_tasks TEXT;

-- Thêm cột primary_role vào bảng users
ALTER TABLE core_service.users 
ADD COLUMN IF NOT EXISTS primary_role VARCHAR(50);
