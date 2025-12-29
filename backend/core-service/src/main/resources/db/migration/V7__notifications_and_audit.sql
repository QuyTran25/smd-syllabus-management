/*
 * V7__notifications_and_audit.sql
 * Mục tiêu: Cấu hình hệ thống, Thông báo và Nhật ký truy vết
 * Cập nhật: System Settings dùng JSONB, Notification hỗ trợ payload
 */

-- Chuyển ngữ cảnh làm việc vào schema dự án và public để dùng UUID
SET search_path TO core_service, public;

-- ==========================================
-- 1. SYSTEM SETTINGS (Cấu hình động)
-- ==========================================
CREATE TABLE system_settings (
    key VARCHAR(100) PRIMARY KEY,
    
    -- JSONB giúp lưu cấu trúc phức tạp (VD: Quy tắc số lượng CLO tối thiểu, Banner config)
    value JSONB NOT NULL, 
    
    description TEXT,
    
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(id)
);

-- ==========================================
-- 2. NOTIFICATIONS (Thông báo người dùng)
-- ==========================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- Người nhận
    
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    
    -- Loại thông báo (VD: SYSTEM, APPROVAL_REQUEST, REJECTED, DEADLINE)
    type VARCHAR(50) DEFAULT 'SYSTEM', 
    
    -- Payload chứa metadata để FE điều hướng (VD: { "syllabus_id": "UUID", "action": "VIEW" })
    payload JSONB,    
    
    is_read BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index để lấy danh sách thông báo chưa đọc của user cực nhanh
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_read ON notifications(user_id, is_read);

-- ==========================================
-- 3. AUDIT LOGS (Nhật ký truy vết)
-- ==========================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    entity_name VARCHAR(50), -- VD: 'SyllabusVersion', 'User', 'Subject'
    entity_id UUID,          -- ID của bản ghi bị tác động
    
    action VARCHAR(50),      -- VD: 'CREATE', 'UPDATE_CONTENT', 'APPROVE', 'LOGIN'
    
    -- ID người thực hiện. Không dùng Foreign Key cứng để bảo toàn log 
    -- ngay cả khi tài khoản người dùng đó đã bị xóa khỏi hệ thống.
    actor_id UUID,           
    
    -- Lưu vết dữ liệu trước và sau khi thay đổi (Dùng cho so sánh phiên bản)
    old_value JSONB,
    new_value JSONB,
    
    ip_address VARCHAR(50),
    user_agent TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- INDEXES & TRIGGERS (Hiệu năng & Đồng bộ)
-- ==========================================

-- Tối ưu hóa việc tra cứu: "Ai đã sửa bản đề cương này?" hoặc "User này đã làm gì?"
CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_time ON audit_logs(created_at);

-- Tự động cập nhật thời gian chỉnh sửa cho bảng cấu hình
CREATE TRIGGER update_settings_time 
BEFORE UPDATE ON system_settings 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();