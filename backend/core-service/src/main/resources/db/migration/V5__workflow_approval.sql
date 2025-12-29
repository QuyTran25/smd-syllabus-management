/*
 * V5__workflow_approval.sql
 * Mục tiêu: Cấu hình quy trình duyệt & Lịch sử duyệt
 * Updated: Thêm Indexes cho History
 */

-- Chuyển ngữ cảnh làm việc vào schema dự án và public để dùng UUID
SET search_path TO core_service, public;

-- Khai báo kiểu dữ liệu cho quyết định phê duyệt
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'decision_type') THEN
        CREATE TYPE decision_type AS ENUM ('APPROVED', 'REJECTED');
    END IF;
END $$;

-- ==========================================
-- 1. Approval Workflows (Cấu hình quy trình)
-- ==========================================
CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    
    -- Các bước duyệt được lưu dưới dạng JSONB để tùy biến thứ tự dễ dàng
    -- Ví dụ: [{"role": "HOD", "order": 1}, {"role": "DEAN", "order": 2}]
    steps JSONB NOT NULL, 
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_workflows_time BEFORE UPDATE ON approval_workflows FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 2. Approval History (Lịch sử phê duyệt)
-- ==========================================
CREATE TABLE approval_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    
    -- Người thực hiện (Trưởng bộ môn, Giáo vụ, v.v.)
    actor_id UUID NOT NULL REFERENCES users(id),
    
    -- Hành động: Đồng ý hoặc Từ chối
    action decision_type NOT NULL,
    
    -- Ý kiến phản hồi (bắt buộc nếu từ chối)
    comment TEXT,
    
    -- Dùng để nhóm các hành động trong cùng một đợt duyệt (nếu cần)
    batch_id UUID, 
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- INDEXES (Tối ưu hóa cho Dashboard & Tracking)
-- ==========================================

-- Tối ưu việc truy tìm lịch sử của một bản đề cương cụ thể
CREATE INDEX idx_approval_syllabus ON approval_history(syllabus_version_id);

-- Tối ưu việc thống kê khối lượng công việc của người duyệt
CREATE INDEX idx_approval_actor ON approval_history(actor_id);

-- Tối ưu việc sắp xếp theo thời gian (Audit Log)
CREATE INDEX idx_approval_time ON approval_history(created_at);