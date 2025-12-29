/*
 * V16__fix_vietnamese_encoding.sql
 * FIX CUỐI CÙNG: Tự động tạo cột nếu thiếu và sửa lỗi Tiếng Việt
 */

SET client_encoding = 'UTF8';
SET search_path TO core_service, public;

-- ============================================
-- 1. ĐẢM BẢO CÁC CỘT GIỜ HỌC PHẢI TỒN TẠI
-- ============================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_theory_hours') THEN
        ALTER TABLE core_service.subjects ADD COLUMN default_theory_hours INT DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_practice_hours') THEN
        ALTER TABLE core_service.subjects ADD COLUMN default_practice_hours INT DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_self_study_hours') THEN
        ALTER TABLE core_service.subjects ADD COLUMN default_self_study_hours INT DEFAULT 0;
    END IF;
END $$;

-- ============================================
-- 2. CẬP NHẬT DỮ LIỆU MÔN HỌC (TIẾNG VIỆT)
-- ============================================
UPDATE core_service.subjects SET current_name_vi = 'Cơ sở dữ liệu', description = 'SQL, Thiết kế CSDL.' WHERE code = '121000';
UPDATE core_service.subjects SET current_name_vi = 'Cấu trúc dữ liệu và giải thuật' WHERE code = '124002';
-- (Bạn có thể thêm các dòng UPDATE môn học khác vào đây nếu muốn fix thêm tên)

-- Cập nhật giờ học (Lúc này chắc chắn cột đã tồn tại nhờ bước 1)
UPDATE core_service.subjects SET 
    default_theory_hours = default_credits * 15,
    default_practice_hours = default_credits * 15,
    default_self_study_hours = default_credits * 30;

-- ============================================
-- 3. TẠO LẠI CLOs (Giữ dữ liệu mẫu - gen_random_uuid)
-- ============================================
DELETE FROM core_service.clo_plo_mappings;
DELETE FROM core_service.assessment_clo_mappings;
DELETE FROM core_service.clos;

DO $$
DECLARE
    syllabus_rec RECORD;
BEGIN
    FOR syllabus_rec IN 
        SELECT sv.id as syllabus_id, s.current_name_vi as subject_name
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES 
            (gen_random_uuid(), syllabus_rec.syllabus_id, 'CLO1', 'Trình bày kiến thức cơ bản của ' || syllabus_rec.subject_name, 'Understand', 25.00),
            (gen_random_uuid(), syllabus_rec.syllabus_id, 'CLO2', 'Vận dụng kiến thức ' || syllabus_rec.subject_name || ' vào thực tế', 'Apply', 75.00);
    END LOOP;
END $$;

-- ============================================
-- 4. CẬP NHẬT NỘI DUNG ĐỀ CƯƠNG (JSONB)
-- ============================================
UPDATE core_service.syllabus_versions sv
SET content = jsonb_build_object(
    'description', s.description,
    'objectives', '["Nắm vững lý thuyết", "Thực hành tốt"]'::jsonb,
    'teachingMethods', '["Lý thuyết", "Thực hành"]'::jsonb
)
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- ============================================
-- 5. ĐỒNG BỘ GIỜ HỌC TRONG SYLLABUS_VERSIONS
-- ============================================
UPDATE core_service.syllabus_versions sv
SET 
    theory_hours = s.default_theory_hours,
    practice_hours = s.default_practice_hours,
    self_study_hours = s.default_self_study_hours
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- Log hoàn tất
DO $$ BEGIN RAISE NOTICE 'V16 Migration Success: Columns ensured and Vietnamese fixed.'; END $$;
