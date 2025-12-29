/*
 * V6__collaboration_and_feedback.sql
 * Mục tiêu: Cộng tác, review và phản hồi sau khi syllabus được xuất bản
 * Updated: Đã tích hợp Enum và Audit columns
 */

-- Chuyển ngữ cảnh làm việc vào schema dự án và public để dùng UUID
SET search_path TO core_service, public;

-- =====================================================
-- 0. ENUMS
-- =====================================================
-- Khai báo kiểu dữ liệu cho vai trò cộng tác viên
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'collaborator_role') THEN
        CREATE TYPE collaborator_role AS ENUM ('OWNER', 'EDITOR', 'VIEWER');
    END IF;
END $$;

-- =====================================================
-- 1. SYLLABUS COLLABORATORS (Cộng tác viên)
-- =====================================================
CREATE TABLE syllabus_collaborators (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    
    -- Quyền hạn cụ thể: Chủ sở hữu, Người biên tập, hoặc Người xem
    role collaborator_role NOT NULL, 
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Đảm bảo mỗi user chỉ có một vai trò duy nhất trong một bản đề cương
    CONSTRAINT uq_syllabus_collaborator UNIQUE (syllabus_version_id, user_id)
);

CREATE INDEX idx_collaborators_syllabus ON syllabus_collaborators(syllabus_version_id);
CREATE INDEX idx_collaborators_user ON syllabus_collaborators(user_id);

-- =====================================================
-- 2. REVIEW COMMENTS (Thảo luận & Góp ý)
-- =====================================================
CREATE TABLE review_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    
    -- Chỉ định góp ý thuộc phần nào để người soạn thảo dễ định vị
    section VARCHAR(50), -- VD: "CLO", "OUTLINE", "ASSESSMENT"
    content TEXT NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE, -- Đánh dấu góp ý đã được xử lý chưa
    
    -- Hỗ trợ thảo luận theo luồng (Thread) bằng cách tham chiếu đến comment cha
    parent_id UUID REFERENCES review_comments(id) ON DELETE CASCADE, 
    
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_syllabus ON review_comments(syllabus_version_id);
CREATE INDEX idx_comments_parent ON review_comments(parent_id);

-- =====================================================
-- 3. SYLLABUS ERROR REPORTS (Báo cáo lỗi hậu ban hành)
-- =====================================================
CREATE TABLE syllabus_error_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id),
    user_id UUID NOT NULL REFERENCES users(id), -- Người phát hiện và báo lỗi
    
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, INVESTIGATING, FIXED, REJECTED
    
    -- Theo dõi thời gian để quản lý KPI xử lý phản hồi
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_error_reports_syllabus ON syllabus_error_reports(syllabus_version_id);
CREATE INDEX idx_error_reports_status ON syllabus_error_reports(status);

-- Tự động cập nhật thời gian chỉnh sửa cuối cùng
CREATE TRIGGER update_error_report_time 
BEFORE UPDATE ON syllabus_error_reports 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();