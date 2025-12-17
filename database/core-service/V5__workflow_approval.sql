/*
 * V5__workflow_approval.sql
 * Mục tiêu: Cấu hình quy trình duyệt & Lịch sử duyệt
 * Updated: Thêm Indexes cho History
 */

SET search_path TO core_service;

CREATE TYPE decision_type AS ENUM ('APPROVED', 'REJECTED');

-- 1. Approval Workflows
CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    steps JSONB NOT NULL, -- [{"role": "HOD", "order": 1}, ...]
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Approval History
CREATE TABLE approval_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id),
    actor_id UUID NOT NULL REFERENCES users(id),
    action decision_type NOT NULL,
    comment TEXT,
    batch_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- [FIX] Indexes cho History (Dashboard query rất nhiều)
CREATE INDEX idx_approval_syllabus ON approval_history(syllabus_version_id);
CREATE INDEX idx_approval_actor ON approval_history(actor_id);
CREATE INDEX idx_approval_time ON approval_history(created_at);