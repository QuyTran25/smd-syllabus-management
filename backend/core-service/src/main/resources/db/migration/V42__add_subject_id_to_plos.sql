/*
 * V42__add_subject_id_to_plos.sql
 * Thêm cột subject_id vào bảng plos để liên kết PLO với Subject
 * Fix lỗi: column p1_0.subject_id does not exist
 */

SET search_path TO core_service;

-- Log start
DO $$BEGIN RAISE NOTICE 'Starting V42 Migration: Adding subject_id to plos table...'; END$$;

-- Thêm cột subject_id vào bảng plos
ALTER TABLE plos 
ADD COLUMN IF NOT EXISTS subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE;

-- Tạo index để tăng performance khi query
CREATE INDEX IF NOT EXISTS idx_plos_subject ON plos(subject_id);

-- Xóa constraint NOT NULL của curriculum_id vì giờ dùng subject_id
ALTER TABLE plos 
ALTER COLUMN curriculum_id DROP NOT NULL;

-- Update existing PLOs to link with subjects via curriculum
-- Lấy subject_id từ curriculum mà PLO đang thuộc về
UPDATE plos p
SET subject_id = (
    SELECT s.id 
    FROM subjects s 
    WHERE s.curriculum_id = p.curriculum_id 
    LIMIT 1
)
WHERE p.subject_id IS NULL;

-- Log kết quả
DO $$
DECLARE 
    plo_count INTEGER;
    plo_with_subject INTEGER;
BEGIN
    SELECT COUNT(*) INTO plo_count FROM plos;
    SELECT COUNT(*) INTO plo_with_subject FROM plos WHERE subject_id IS NOT NULL;
    
    RAISE NOTICE 'Total PLOs: %', plo_count;
    RAISE NOTICE 'PLOs with subject_id: %', plo_with_subject;
    RAISE NOTICE 'V42 Migration completed successfully';
END$$;

COMMIT;
