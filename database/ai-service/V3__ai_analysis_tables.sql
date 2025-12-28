/*
 * V3__ai_analysis_tables.sql
 * Mục tiêu: Lưu trữ kết quả phân tích và đề xuất từ AI
 * - syllabus_ai_analysis: Kết quả tóm tắt, phân tích
 * - syllabus_ai_recommendation: Đề xuất cải thiện đề cương
 */

SET search_path TO ai_service;

-- ==========================================
-- 1. ENUMS
-- ==========================================
CREATE TYPE analysis_type AS ENUM (
    'SUMMARY',              -- Tóm tắt đề cương
    'BLOOM_ANALYSIS',       -- Phân tích Bloom's Taxonomy
    'DUPLICATE_CHECK',      -- Kiểm tra trùng lặp
    'PLO_ALIGNMENT',        -- Phân tích độ phù hợp CLO-PLO
    'PREREQUISITE_ANALYSIS',-- Phân tích môn tiên quyết
    'CONTENT_QUALITY',      -- Đánh giá chất lượng nội dung
    'VERSION_DIFF'          -- So sánh phiên bản
);

CREATE TYPE recommendation_type AS ENUM (
    'CLO_SUGGESTION',       -- Gợi ý CLO
    'PLO_MAPPING',          -- Gợi ý mapping CLO-PLO
    'CONTENT_IMPROVEMENT',  -- Cải thiện nội dung
    'ASSESSMENT_SUGGESTION',-- Gợi ý phương pháp đánh giá
    'MATERIAL_SUGGESTION',  -- Gợi ý tài liệu
    'PREREQUISITE_SUGGESTION' -- Gợi ý môn tiên quyết
);

CREATE TYPE recommendation_status AS ENUM (
    'PENDING',      -- Chờ xem xét
    'ACCEPTED',     -- Đã chấp nhận
    'REJECTED',     -- Đã từ chối
    'APPLIED'       -- Đã áp dụng vào đề cương
);

-- ==========================================
-- 2. SYLLABUS AI ANALYSIS (Kết quả phân tích)
-- ==========================================
CREATE TABLE syllabus_ai_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Reference (không FK cứng vì cross-service)
    syllabus_version_id UUID NOT NULL,
    
    -- Loại phân tích
    analysis_type analysis_type NOT NULL,
    
    -- Kết quả phân tích (JSON linh hoạt)
    result JSONB NOT NULL,
    /*
     * Ví dụ result cho SUMMARY:
     * {
     *   "summary_vi": "Môn học giới thiệu về...",
     *   "summary_en": "This course introduces...",
     *   "key_topics": ["OOP", "Design Patterns", "SOLID"],
     *   "target_audience": "Sinh viên năm 2-3",
     *   "difficulty_level": "Intermediate"
     * }
     *
     * Ví dụ result cho BLOOM_ANALYSIS:
     * {
     *   "clo_analysis": [
     *     {"clo_code": "CLO1", "detected_level": 3, "suggested_level": 4, "keywords": ["apply", "implement"]}
     *   ],
     *   "overall_balance": {"remember": 10, "understand": 20, "apply": 30, "analyze": 25, "evaluate": 10, "create": 5}
     * }
     */
    
    -- Metadata
    model_used VARCHAR(100),        -- Model AI đã dùng (gpt-4, gemini-pro, ...)
    confidence_score DECIMAL(5,4),  -- Độ tin cậy (0.0000 - 1.0000)
    processing_time_ms INT,         -- Thời gian xử lý (ms)
    
    -- Job reference
    job_id UUID REFERENCES ai_jobs(id) ON DELETE SET NULL,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ai_analysis_syllabus ON syllabus_ai_analysis(syllabus_version_id);
CREATE INDEX idx_ai_analysis_type ON syllabus_ai_analysis(analysis_type);
CREATE INDEX idx_ai_analysis_job ON syllabus_ai_analysis(job_id);
CREATE INDEX idx_ai_analysis_created ON syllabus_ai_analysis(created_at);
CREATE INDEX idx_ai_analysis_result_gin ON syllabus_ai_analysis USING GIN (result jsonb_path_ops);

-- Trigger
CREATE TRIGGER update_ai_analysis_time 
BEFORE UPDATE ON syllabus_ai_analysis 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 3. SYLLABUS AI RECOMMENDATION (Đề xuất cải thiện)
-- ==========================================
CREATE TABLE syllabus_ai_recommendation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Reference
    syllabus_version_id UUID NOT NULL,
    
    -- Loại đề xuất
    recommendation_type recommendation_type NOT NULL,
    
    -- Nội dung đề xuất
    title VARCHAR(255) NOT NULL,            -- Tiêu đề ngắn gọn
    description TEXT NOT NULL,              -- Mô tả chi tiết
    
    -- Chi tiết đề xuất (JSON)
    suggestion_data JSONB NOT NULL,
    /*
     * Ví dụ suggestion_data cho CLO_SUGGESTION:
     * {
     *   "suggested_clos": [
     *     {
     *       "code": "CLO5",
     *       "description": "Áp dụng được các mẫu thiết kế phổ biến",
     *       "bloom_level": 3,
     *       "rationale": "Dựa trên nội dung về Design Patterns trong outline"
     *     }
     *   ]
     * }
     *
     * Ví dụ suggestion_data cho CONTENT_IMPROVEMENT:
     * {
     *   "section": "assessment",
     *   "current_content": "Thi cuối kỳ 70%",
     *   "suggested_content": "Thi cuối kỳ 50%, Đồ án 20%",
     *   "reason": "Cân bằng giữa lý thuyết và thực hành"
     * }
     */
    
    -- Trạng thái xử lý
    status recommendation_status DEFAULT 'PENDING',
    
    -- Phản hồi của người dùng
    user_feedback TEXT,                     -- Lý do chấp nhận/từ chối
    reviewed_by UUID,                       -- User ID đã review (từ core_service)
    reviewed_at TIMESTAMP,
    
    -- Metadata AI
    priority INT DEFAULT 0,                 -- Độ ưu tiên (cao hơn = quan trọng hơn)
    confidence_score DECIMAL(5,4),
    model_used VARCHAR(100),
    
    -- Job reference  
    job_id UUID REFERENCES ai_jobs(id) ON DELETE SET NULL,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ai_rec_syllabus ON syllabus_ai_recommendation(syllabus_version_id);
CREATE INDEX idx_ai_rec_type ON syllabus_ai_recommendation(recommendation_type);
CREATE INDEX idx_ai_rec_status ON syllabus_ai_recommendation(status);
CREATE INDEX idx_ai_rec_priority ON syllabus_ai_recommendation(priority DESC);
CREATE INDEX idx_ai_rec_job ON syllabus_ai_recommendation(job_id);
CREATE INDEX idx_ai_rec_suggestion_gin ON syllabus_ai_recommendation USING GIN (suggestion_data jsonb_path_ops);

-- Trigger
CREATE TRIGGER update_ai_rec_time 
BEFORE UPDATE ON syllabus_ai_recommendation 
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ==========================================
-- 4. VIEW: Thống kê AI Analysis
-- ==========================================
CREATE OR REPLACE VIEW v_ai_analysis_stats AS
SELECT 
    syllabus_version_id,
    COUNT(*) AS total_analyses,
    COUNT(*) FILTER (WHERE analysis_type = 'SUMMARY') AS summary_count,
    COUNT(*) FILTER (WHERE analysis_type = 'BLOOM_ANALYSIS') AS bloom_count,
    ROUND(AVG(confidence_score), 4) AS avg_confidence,
    MAX(created_at) AS last_analysis_at
FROM syllabus_ai_analysis
GROUP BY syllabus_version_id;

-- ==========================================
-- 5. VIEW: Thống kê AI Recommendations
-- ==========================================
CREATE OR REPLACE VIEW v_ai_recommendation_stats AS
SELECT 
    syllabus_version_id,
    COUNT(*) AS total_recommendations,
    COUNT(*) FILTER (WHERE status = 'PENDING') AS pending_count,
    COUNT(*) FILTER (WHERE status = 'ACCEPTED') AS accepted_count,
    COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected_count,
    COUNT(*) FILTER (WHERE status = 'APPLIED') AS applied_count,
    ROUND(
        COUNT(*) FILTER (WHERE status IN ('ACCEPTED', 'APPLIED'))::DECIMAL / 
        NULLIF(COUNT(*) FILTER (WHERE status != 'PENDING'), 0) * 100, 
    2) AS acceptance_rate_pct
FROM syllabus_ai_recommendation
GROUP BY syllabus_version_id;

-- ==========================================
-- LOG COMPLETION
-- ==========================================
DO $$
BEGIN 
    RAISE NOTICE 'V3 AI Migration completed: syllabus_ai_analysis & syllabus_ai_recommendation tables created.';
END $$;
