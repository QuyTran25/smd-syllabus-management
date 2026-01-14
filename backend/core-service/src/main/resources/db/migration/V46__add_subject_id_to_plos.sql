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

-- ✅ FIX: Link PLO với TẤT CẢ subjects
-- Strategy: Mỗi subject sẽ được link với một PLO từ tập PLO chung
-- Lặp qua từng subject và assign PLO dựa trên modulo để phân bổ đều

CREATE OR REPLACE FUNCTION assign_plos_to_subjects() RETURNS void AS $$
DECLARE
    v_subject_id UUID;
    v_subject_row record;
    v_plo_id UUID;
    v_plo_code VARCHAR;
    v_subject_idx INT := 0;
    v_plo_count INT;
    v_plo_array UUID[];
BEGIN
    -- Lấy danh sách tất cả PLO
    SELECT ARRAY_AGG(id) INTO v_plo_array FROM plos WHERE subject_id IS NULL;
    v_plo_count := ARRAY_LENGTH(v_plo_array, 1);
    
    RAISE NOTICE 'Total PLOs to assign: %', v_plo_count;
    
    IF v_plo_count > 0 THEN
        -- Lặp qua từng subject và assign PLO
        FOR v_subject_row IN SELECT id, code FROM subjects ORDER BY created_at ASC
        LOOP
            v_subject_idx := v_subject_idx + 1;
            v_plo_id := v_plo_array[(v_subject_idx % v_plo_count) + 1];
            
            -- Update PLO để reference tới subject này
            UPDATE plos SET subject_id = v_subject_row.id 
            WHERE id = v_plo_id AND subject_id IS NULL;
            
            IF v_subject_idx % 10 = 0 THEN
                RAISE NOTICE 'Assigned PLOs to % subjects so far...', v_subject_idx;
            END IF;
        END LOOP;
        
        RAISE NOTICE 'Successfully assigned PLOs to % subjects', v_subject_idx;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Gọi function để assign PLO
SELECT assign_plos_to_subjects();

-- Cleanup function
DROP FUNCTION IF EXISTS assign_plos_to_subjects();

-- Log kết quả cuối cùng
DO $$
DECLARE 
    plo_count INTEGER;
    plo_with_subject INTEGER;
    subject_with_plo INTEGER;
BEGIN
    SELECT COUNT(*) INTO plo_count FROM plos;
    SELECT COUNT(*) INTO plo_with_subject FROM plos WHERE subject_id IS NOT NULL;
    SELECT COUNT(DISTINCT p.subject_id) INTO subject_with_plo FROM plos p WHERE p.subject_id IS NOT NULL;
    
    RAISE NOTICE '=== V46 Migration Results ===';
    RAISE NOTICE 'Total PLOs: %', plo_count;
    RAISE NOTICE 'PLOs with subject_id: %', plo_with_subject;
    RAISE NOTICE 'Subjects with PLOs: %', subject_with_plo;
    RAISE NOTICE 'V46 Migration completed successfully';
END$$;

COMMIT;

