/*
 * V2__vector_indexes.sql
 * Mục tiêu: Tạo Index HNSW cho tìm kiếm Semantic Search tốc độ cao
 * Lưu ý: Chạy file này sau khi đã seed một lượng dữ liệu nhất định nếu có thể
 */

SET search_path TO ai_service;

-- HNSW Index (Hierarchical Navigable Small World)
-- vector_cosine_ops: Dùng Cosine Similarity (Phổ biến nhất cho Text Embedding)
-- m=16, ef_construction=64: Cấu hình cân bằng giữa tốc độ build và tốc độ search cho < 1M vectors

CREATE INDEX idx_embedding_hnsw 
ON vector_embeddings 
USING hnsw (embedding vector_cosine_ops) 
WITH (m = 16, ef_construction = 64);

-- [OPTIONAL] Nếu muốn dùng pg_trgm cho tìm kiếm mờ (Fuzzy Search) trên nội dung gốc
-- CREATE INDEX idx_vectors_content_trgm ON vector_embeddings USING GIN (content_chunk gin_trgm_ops);