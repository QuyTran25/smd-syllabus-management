/*
 * V7__notifications_and_audit.sql
 * Mục tiêu: Cấu hình hệ thống, Thông báo và Nhật ký truy vết
 * Cập nhật: System Settings dùng JSONB, Notification hỗ trợ payload
 */

SET search_path TO core_service;

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
-- 2. NOTIFICATIONS (Thông báo)
-- ==========================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id), -- Người nhận
    
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    
    -- Loại thông báo để FE hiển thị icon (SYSTEM, APPROVAL, DEADLINE...)
    type VARCHAR(50) DEFAULT 'SYSTEM', 
    
    -- Payload chứa link/ID để click vào xem chi tiết (VD: { "syllabus_id": "..." })
    payload JSONB,    
    
    is_read BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index để query list thông báo nhanh
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_read ON notifications(user_id, is_read);

-- ==========================================
-- 3. AUDIT LOGS (Truy vết hệ thống)
-- ==========================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    entity_name VARCHAR(50), -- VD: SyllabusVersion, User, CLO
    entity_id UUID,          -- ID của đối tượng bị tác động
    
    action VARCHAR(50),      -- VD: CREATE, UPDATE_CONTENT, APPROVE, REJECT
    
    actor_id UUID,           -- Người thực hiện (Không FK cứng để giữ log khi user bị xóa)
    
    -- Lưu vết thay đổi (Optional, dùng cho các tác vụ quan trọng)
    old_value JSONB,
    new_value JSONB,
    
    ip_address VARCHAR(50),
    user_agent TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index cho tra cứu lịch sử
CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_time ON audit_logs(created_at);

-- Trigger cập nhật thời gian settings
CREATE TRIGGER update_settings_time BEFORE UPDATE ON system_settings FOR EACH ROW EXECUTE FUNCTION update_timestamp();