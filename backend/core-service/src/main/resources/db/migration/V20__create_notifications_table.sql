-- 1. Đảm bảo schema tồn tại
CREATE SCHEMA IF NOT EXISTS core_service;

-- 2. Tạo bảng với đầy đủ cột
CREATE TABLE IF NOT EXISTS core_service.notifications (
    id uuid PRIMARY KEY,
    user_id uuid,
    title varchar(255),
    message text,
    type varchar(50),
    payload jsonb,
    is_read boolean DEFAULT false,
    read_at timestamptz,
    -- HAI CỘT DƯỚI ĐÂY LÀ BẮT BUỘC ĐỂ KHÔNG LỖI INDEX
    related_entity_id uuid,
    related_entity_type varchar(50),
    created_at timestamptz DEFAULT now()
);

-- 3. Tạo các Index
CREATE INDEX IF NOT EXISTS idx_notif_user_created ON core_service.notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_related_entity ON core_service.notifications(related_entity_type, related_entity_id);