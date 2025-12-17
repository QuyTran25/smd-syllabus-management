/*
 * V1__init_ai_service.sql
 * Mục tiêu: Khởi tạo Vector DB & Job Queue
 * Updated: Full Job Types (Diff, Summarize, Suggest, Validate)
 */

SET search_path TO ai_service;

-- ==========================================
-- 0. EXTENSIONS
-- ==========================================
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 1. ENUMS
-- ==========================================
CREATE TYPE job_status AS ENUM ('PENDING', 'PROCESSING', 'DONE', 'FAILED', 'CANCELLED');

-- [UPDATED] Bổ sung đầy đủ các Job theo System Analysis
CREATE TYPE job_type AS ENUM (
    -- 1. Core RAG Jobs
    'VECTORIZE',            -- Nhúng dữ liệu vào Vector DB
    
    -- 2. Generation & Suggestion
    'GENERATE_OUTLINE',     -- Gợi ý khung đề cương
    'SUGGEST_CLO',          -- [NEW] Gợi ý CLO từ mô tả môn học
    'RECOMMEND_SUBJECT',    -- Gợi ý môn học cho sinh viên
    
    -- 3. Validation & Analysis
    'ANALYZE_BLOOM',        -- Phân tích thang đo Bloom
    'CHECK_DUPLICATE',      -- Kiểm tra trùng lặp nội dung
    'VALIDATE_PLO_MAPPING', -- [NEW] Kiểm tra độ phù hợp CLO-PLO
    
    -- 4. Utility & Workflow Support
    'DIFF_VERSIONS',        -- [NEW] So sánh 2 phiên bản (Highlight changes)
    'SUMMARIZE_SYLLABUS',   -- [NEW] Tóm tắt đề cương cho sinh viên
    'MATCH_REVIEWER'        -- Tìm giảng viên phù hợp để review
);

CREATE TYPE embedding_section_type AS ENUM (
    'CLO', 'COURSE_DESCRIPTION', 'TEACHING_PLAN', 
    'MATERIAL', 'ASSESSMENT', 'GENERAL_INFO'
);

CREATE TYPE log_level AS ENUM ('INFO', 'WARN', 'ERROR', 'DEBUG');

-- ==========================================
-- 2. AI JOBS
-- ==========================================
CREATE TABLE ai_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID, 
    type job_type NOT NULL,
    status job_status DEFAULT 'PENDING',
    priority INT DEFAULT 0,
    
    worker_id VARCHAR(100),
    locked_at TIMESTAMP,

    input_payload JSONB NOT NULL,
    result JSONB,
    
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    execution_duration_ms BIGINT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_jobs_poll ON ai_jobs(status, priority DESC, created_at ASC);
CREATE INDEX idx_jobs_syllabus ON ai_jobs(syllabus_version_id);
CREATE INDEX idx_jobs_type ON ai_jobs(type);

CREATE TRIGGER update_jobs_time BEFORE UPDATE ON ai_jobs FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 3. SMART FUNCTIONS
-- ==========================================

-- Function 1: Get Next Job
CREATE OR REPLACE FUNCTION get_next_job(p_worker_id VARCHAR)
RETURNS SETOF ai_jobs AS $$
DECLARE
    v_job_id UUID;
BEGIN
    SELECT id INTO v_job_id
    FROM ai_jobs
    WHERE status = 'PENDING'
    ORDER BY priority DESC, created_at ASC
    FOR UPDATE SKIP LOCKED
    LIMIT 1;

    IF v_job_id IS NOT NULL THEN
        RETURN QUERY
        UPDATE ai_jobs
        SET status = 'PROCESSING',
            worker_id = p_worker_id,
            locked_at = NOW(),
            started_at = NOW()
        WHERE id = v_job_id
        RETURNING *;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function 2: Cleanup Zombies
CREATE OR REPLACE FUNCTION cleanup_zombie_jobs(p_minutes INT DEFAULT 30)
RETURNS INT AS $$
DECLARE
    v_count INT;
BEGIN
    WITH updated AS (
        UPDATE ai_jobs
        SET status = 'PENDING',
            worker_id = NULL,
            locked_at = NULL,
            retry_count = retry_count + 1,
            error_message = 'Zombie Detected: Job timed out > ' || p_minutes || ' minutes'
        WHERE status = 'PROCESSING'
          AND locked_at < NOW() - (p_minutes || ' minutes')::INTERVAL
          AND retry_count < max_retries
        RETURNING 1
    )
    SELECT COUNT(*) INTO v_count FROM updated;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Function 3: Cancel Job
CREATE OR REPLACE FUNCTION cancel_job(p_job_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_affected INT;
BEGIN
    UPDATE ai_jobs
    SET status = 'CANCELLED',
        finished_at = NOW(),
        error_message = 'Cancelled by user request'
    WHERE id = p_job_id
      AND status IN ('PENDING', 'PROCESSING')
    RETURNING 1 INTO v_affected;
    
    RETURN v_affected > 0;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 4. VECTOR EMBEDDINGS
-- ==========================================
CREATE TABLE vector_embeddings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    syllabus_version_id UUID NOT NULL,
    section_type embedding_section_type NOT NULL,
    
    content_chunk TEXT NOT NULL,
    
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector('simple', unaccent(content_chunk))
    ) STORED,
    
    metadata JSONB DEFAULT '{}',
    embedding vector(1536),
    dimension INT DEFAULT 1536, 
    model_name VARCHAR(100) DEFAULT 'text-embedding-3-small',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vectors_syllabus ON vector_embeddings(syllabus_version_id);
CREATE INDEX idx_vectors_search ON vector_embeddings USING GIN(search_vector);
CREATE INDEX idx_vectors_section ON vector_embeddings(section_type);

CREATE TRIGGER update_vectors_time BEFORE UPDATE ON vector_embeddings FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 5. MONITORING
-- ==========================================
CREATE TABLE ai_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID REFERENCES ai_jobs(id) ON DELETE CASCADE,
    level log_level DEFAULT 'INFO',
    message TEXT,
    details JSONB,
    model_used VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_logs_job ON ai_logs(job_id);
CREATE INDEX idx_logs_level ON ai_logs(level);

CREATE OR REPLACE VIEW v_ai_job_stats AS
SELECT 
    type,
    COUNT(*) AS total_jobs,
    COUNT(*) FILTER (WHERE status = 'DONE') AS success_count,
    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed_count,
    ROUND((COUNT(*) FILTER (WHERE status = 'FAILED')::DECIMAL / NULLIF(COUNT(*), 0)) * 100, 2) AS fail_rate_pct,
    ROUND(AVG(execution_duration_ms) FILTER (WHERE status = 'DONE'), 2) AS avg_duration_ms,
    SUM(retry_count) AS total_retries,
    MAX(created_at) AS last_job_at
FROM ai_jobs
GROUP BY type;
