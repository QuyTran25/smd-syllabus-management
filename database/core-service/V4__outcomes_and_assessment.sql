/*
 * V4__outcomes_and_assessment.sql
 * Mục tiêu: Chuẩn đầu ra & Đánh giá
 * Updated: Thêm Index hiệu năng & Trigger Audit
 */

SET search_path TO core_service;

-- 1. PLOs
CREATE TABLE plos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    curriculum_id UUID NOT NULL REFERENCES curriculums(id),
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- 2. CLOs
CREATE TABLE clos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    bloom_level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    UNIQUE(syllabus_version_id, code)
);

-- 3. CLO-PLO Mapping
CREATE TABLE clo_plo_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    clo_id UUID NOT NULL REFERENCES clos(id) ON DELETE CASCADE,
    plo_id UUID NOT NULL REFERENCES plos(id) ON DELETE CASCADE,
    weight DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

-- 4. Assessment Schemes
CREATE TABLE assessment_schemes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL REFERENCES syllabus_versions(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES assessment_schemes(id),
    name VARCHAR(255) NOT NULL,
    weight_percent DECIMAL(5,2) NOT NULL CHECK (weight_percent >= 0 AND weight_percent <= 100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- 5. Grading Scales
CREATE TABLE grading_scales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    definition JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- [FIX] Indexes
CREATE INDEX idx_plos_curriculum ON plos(curriculum_id);
CREATE INDEX idx_clos_syllabus ON clos(syllabus_version_id);
CREATE INDEX idx_mapping_clo ON clo_plo_mappings(clo_id);
CREATE INDEX idx_mapping_plo ON clo_plo_mappings(plo_id);
CREATE INDEX idx_assessment_syllabus ON assessment_schemes(syllabus_version_id);

-- [FIX] Triggers
CREATE TRIGGER update_plos_time BEFORE UPDATE ON plos FOR EACH ROW EXECUTE FUNCTION update_timestamp();
CREATE TRIGGER update_clos_time BEFORE UPDATE ON clos FOR EACH ROW EXECUTE FUNCTION update_timestamp();
CREATE TRIGGER update_schemes_time BEFORE UPDATE ON assessment_schemes FOR EACH ROW EXECUTE FUNCTION update_timestamp();