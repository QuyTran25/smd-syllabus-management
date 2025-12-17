/*
 * V6__collaboration_and_feedback.sql
 * Mục tiêu: Cộng tác, review và phản hồi sau khi syllabus được xuất bản
 * Updated: Đã tích hợp Enum và Audit columns
 */

SET search_path TO core_service;

-- =====================================================
-- 0. ENUMS
-- =====================================================
CREATE TYPE collaborator_role AS ENUM ('OWNER', 'EDITOR', 'VIEWER');

-- =====================================================
-- 1. SYLLABUS COLLABORATORS
-- =====================================================
CREATE TABLE syllabus_collaborators (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    
    role collaborator_role NOT NULL, -- [DONE] Dùng Enum
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_syllabus_collaborator UNIQUE (syllabus_version_id, user_id)
);

CREATE INDEX idx_collaborators_syllabus ON syllabus_collaborators(syllabus_version_id);
CREATE INDEX idx_collaborators_user ON syllabus_collaborators(user_id);

-- =====================================================
-- 2. REVIEW COMMENTS
-- =====================================================
CREATE TABLE review_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    
    section VARCHAR(50), -- VD: "CLO", "OUTLINE"
    content TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    
    parent_id UUID REFERENCES review_comments(id) ON DELETE CASCADE, -- Thread support
    
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_syllabus ON review_comments(syllabus_version_id);
CREATE INDEX idx_comments_parent ON review_comments(parent_id);

-- =====================================================
-- 3. SYLLABUS ERROR REPORTS
-- =====================================================
CREATE TABLE syllabus_error_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id),
    user_id UUID NOT NULL REFERENCES users(id), -- Người báo lỗi
    
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    
    -- [BỔ SUNG] Audit để biết khi nào lỗi được fix
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_error_reports_syllabus ON syllabus_error_reports(syllabus_version_id);
CREATE INDEX idx_error_reports_status ON syllabus_error_reports(status);

-- Trigger cập nhật updated_at cho error report
CREATE TRIGGER update_error_report_time 
BEFORE UPDATE ON syllabus_error_reports 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();