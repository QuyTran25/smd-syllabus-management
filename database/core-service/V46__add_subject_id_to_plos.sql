/*
 * V46__add_subject_id_to_plos.sql
 * Thêm cột subject_id vào bảng plos để liên kết PLO với Subject
 * Fix lỗi: column p1_0.subject_id does not exist
 */

SET search_path TO core_service;

-- Log start
DO $$BEGIN RAISE NOTICE 'Starting V46 Migration: Adding subject_id to plos table...'; END$$;

-- Thêm cột subject_id vào bảng plos (nếu chưa có)
ALTER TABLE plos 
ADD COLUMN IF NOT EXISTS subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE;

-- Tạo index để tăng performance khi query
CREATE INDEX IF NOT EXISTS idx_plos_subject ON plos(subject_id);

-- Xóa constraint NOT NULL của curriculum_id vì giờ dùng subject_id
ALTER TABLE plos 
ALTER COLUMN curriculum_id DROP NOT NULL;

-- ✅ FIX: Lấy tất cả subjects chưa có PLO được liên kết
-- Nếu một curriculum có subjects, hãy liên kết PLO với subject đầu tiên của curriculum đó
UPDATE plos p
SET subject_id = (
    SELECT s.id 
    FROM subjects s 
    WHERE s.curriculum_id = p.curriculum_id 
      AND s.id IS NOT NULL
    ORDER BY s.created_at ASC
    LIMIT 1
)
WHERE p.subject_id IS NULL AND p.curriculum_id IS NOT NULL;

-- Log kết quả UPDATE
DO $$
DECLARE 
    updated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO updated_count FROM plos WHERE subject_id IS NOT NULL AND curriculum_id IS NOT NULL;
    RAISE NOTICE 'Updated PLOs with subject_id from curriculum: %', updated_count;
END$$;

-- Nếu vẫn có PLO không có subject_id, hãy lấy bất kỳ subject nào
UPDATE plos p
SET subject_id = (
    SELECT id FROM subjects 
    WHERE id IS NOT NULL
    ORDER BY created_at ASC
    LIMIT 1
)
WHERE p.subject_id IS NULL;

-- Log kết quả cuối cùng
DO $$
DECLARE 
    plo_count INTEGER;
    plo_with_subject INTEGER;
    plo_without_subject INTEGER;
BEGIN
    SELECT COUNT(*) INTO plo_count FROM plos;
    SELECT COUNT(*) INTO plo_with_subject FROM plos WHERE subject_id IS NOT NULL;
    SELECT COUNT(*) INTO plo_without_subject FROM plos WHERE subject_id IS NULL;
    
    RAISE NOTICE 'Total PLOs: %', plo_count;
    RAISE NOTICE 'PLOs with subject_id: %', plo_with_subject;
    RAISE NOTICE 'PLOs without subject_id: %', plo_without_subject;
    RAISE NOTICE 'V46 Migration completed successfully';
END$$;

COMMIT;

