/*
 * V3__syllabus_versioning.sql
 * Mục tiêu: Quản lý Học kỳ & Phiên bản Đề cương
 * Updated: Version Lineage, Keywords Search, GIN Indexes
 */

SET search_path TO core_service;

-- [FINAL] Status cho Workflow + Lưu trữ
CREATE TYPE syllabus_status AS ENUM (
    'DRAFT',
    'PENDING_HOD',           -- Chờ Trưởng bộ môn
    'PENDING_AA',            -- Chờ Phòng đào tạo
    'PENDING_PRINCIPAL',     -- Chờ Hiệu trưởng
    'APPROVED',              -- Đã phê duyệt (chờ publish)
    'PUBLISHED',             -- Đã ban hành
    'REJECTED',              -- Từ chối (quay về Draft)
    'REVISION_IN_PROGRESS',  -- Đang sửa đổi theo yêu cầu
    'INACTIVE',              -- Ngưng sử dụng
    'ARCHIVED'               -- [NEW] Lưu trữ (version cũ sau khi có bản mới)
);

-- ==========================================
-- 1. ACADEMIC TERMS (Học kỳ)
-- ==========================================
CREATE TABLE academic_terms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE, -- FA24, SP25
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

CREATE TRIGGER update_terms_time BEFORE UPDATE ON academic_terms FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 2. SYLLABUS VERSIONS
-- ==========================================
CREATE TABLE syllabus_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    academic_term_id UUID REFERENCES academic_terms(id), 
    
    version_no VARCHAR(20) NOT NULL,
    status syllabus_status DEFAULT 'DRAFT',
    
    -- [NEW] Version Lineage: Theo dõi lịch sử từng phiên bản
    -- Giúp Compare V1 vs V2, hiện Revision History
    previous_version_id UUID REFERENCES syllabus_versions(id) ON DELETE SET NULL,
    
    -- [NEW] Review Deadline: Hạn chót phê duyệt
    review_deadline TIMESTAMP,
    
    -- Snapshots (khóa giá trị tại thời điểm tạo)
    snap_subject_code VARCHAR(20) NOT NULL,
    snap_subject_name_vi VARCHAR(255) NOT NULL,
    snap_subject_name_en VARCHAR(255),
    snap_credit_count INT NOT NULL,
    
    -- [NEW] Keywords: Hỗ trợ tìm kiếm nhanh
    -- Ví dụ: ['machine learning', 'neural network', 'python']
    keywords TEXT[] DEFAULT '{}',
    
    -- Detailed content (JSONB cho flexibility)
    content JSONB DEFAULT '{}',
    
    -- Audit & Soft Delete
    approved_by UUID REFERENCES users(id),
    created_by UUID NOT NULL REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    published_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- INDEXES (Performance Critical)
-- ==========================================

-- Unique constraint: Chỉ 1 bản PUBLISHED cho mỗi môn mỗi kỳ
CREATE UNIQUE INDEX uq_subject_published 
ON syllabus_versions(subject_id, academic_term_id) 
WHERE status = 'PUBLISHED' AND is_deleted = FALSE;

-- B-Tree Indexes (Foreign Keys & Lookups)
CREATE INDEX idx_syllabus_subject ON syllabus_versions(subject_id);
CREATE INDEX idx_syllabus_term ON syllabus_versions(academic_term_id);
CREATE INDEX idx_syllabus_previous ON syllabus_versions(previous_version_id);
CREATE INDEX idx_syllabus_status ON syllabus_versions(status);

-- [NEW] GIN Indexes: CRITICAL cho Full-text Search
-- Không có GIN → Search content chậm 100x
CREATE INDEX idx_syllabus_content_gin ON syllabus_versions USING GIN (content jsonb_path_ops);
CREATE INDEX idx_syllabus_keywords_gin ON syllabus_versions USING GIN (keywords);

-- ==========================================
-- TRIGGER
-- ==========================================
CREATE TRIGGER update_syllabus_time BEFORE UPDATE ON syllabus_versions FOR EACH ROW EXECUTE FUNCTION update_timestamp();