/*
 * V36__add_academic_year_to_terms.sql
 * Mục tiêu: Thêm cột academic_year vào bảng academic_terms
 * Đảm bảo chỉ 1 học kỳ được active tại một thời điểm
 */

SET search_path TO core_service;

-- ==========================================
-- 1. THÊM CỘT ACADEMIC_YEAR
-- ==========================================
ALTER TABLE academic_terms 
ADD COLUMN IF NOT EXISTS academic_year VARCHAR(20);

-- Tự động tính academic_year từ start_date cho data có sẵn
UPDATE academic_terms 
SET academic_year = CASE 
    WHEN EXTRACT(MONTH FROM start_date) >= 8 THEN 
        EXTRACT(YEAR FROM start_date)::TEXT || '-' || (EXTRACT(YEAR FROM start_date) + 1)::TEXT
    ELSE 
        (EXTRACT(YEAR FROM start_date) - 1)::TEXT || '-' || EXTRACT(YEAR FROM start_date)::TEXT
END
WHERE academic_year IS NULL;

-- ==========================================
-- 2. TRIGGER ĐẢM BẢO CHỈ 1 HỌC KỲ ACTIVE
-- ==========================================
CREATE OR REPLACE FUNCTION core_service.ensure_single_active_term()
RETURNS TRIGGER AS $$
BEGIN
    -- Nếu học kỳ mới được set is_active = TRUE
    IF NEW.is_active = TRUE THEN
        -- Vô hiệu hóa tất cả các học kỳ khác (chỉ định rõ schema)
        UPDATE core_service.academic_terms 
        SET is_active = FALSE 
        WHERE id != NEW.id AND is_active = TRUE;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger
DROP TRIGGER IF EXISTS trigger_single_active_term ON academic_terms;
CREATE TRIGGER trigger_single_active_term
    BEFORE INSERT OR UPDATE OF is_active ON academic_terms
    FOR EACH ROW
    WHEN (NEW.is_active = TRUE)
    EXECUTE FUNCTION ensure_single_active_term();

-- ==========================================
-- 3. COMMENT GIẢI THÍCH
-- ==========================================
COMMENT ON COLUMN academic_terms.academic_year IS 'Năm học (VD: 2024-2025). Tự động tính từ start_date nếu không nhập';
COMMENT ON TRIGGER trigger_single_active_term ON academic_terms IS 'Đảm bảo chỉ có 1 học kỳ được kích hoạt tại một thời điểm';
