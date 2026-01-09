/* V16__create_student_syllabus_tracker.sql */
BEGIN;

SET search_path TO core_service;

-- 1. Tạo bảng theo dõi (Tracker)
CREATE TABLE IF NOT EXISTS student_syllabus_tracker (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL, -- ID sinh viên (Lấy từ Token)
    syllabus_id UUID NOT NULL, -- ID môn học (Subject ID)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Đảm bảo mỗi sinh viên chỉ track một môn 1 lần
    CONSTRAINT uk_student_syllabus UNIQUE (student_id, syllabus_id),
    
    -- Khóa ngoại trỏ về bảng subjects (đảm bảo môn học tồn tại)
    CONSTRAINT fk_tracker_subject FOREIGN KEY (syllabus_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- 2. Tạo index để query cho nhanh
CREATE INDEX idx_tracker_student ON student_syllabus_tracker(student_id);

COMMIT;