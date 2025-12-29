/*
 * V13__fix_encoding_and_complete_data.sql
 * Khôi phục toàn bộ dữ liệu mẫu chi tiết và sửa lỗi hiển thị tiếng Việt
 * Tích hợp logic mapping CLO-PLO và Assessment từ Team
 */

SET search_path TO core_service, public;

-- =====================================================
-- 1. CẬP NHẬT THÔNG TIN CHI TIẾT CHO TOÀN BỘ MÔN HỌC
-- =====================================================
DO $$
BEGIN
    -- 1.1. Cập nhật các trường thông tin cơ bản
    UPDATE core_service.subjects SET
        subject_type = 'REQUIRED',
        component = 'BOTH',
        description = CASE current_name_vi
            WHEN 'Nhập môn lập trình' THEN 'Môn học cung cấp kiến thức cơ bản về lập trình, bao gồm các khái niệm về biến, kiểu dữ liệu, cấu trúc điều khiển, hàm và mảng.'
            WHEN 'Cấu trúc dữ liệu và giải thuật' THEN 'Môn học trình bày các cấu trúc dữ liệu cơ bản (mảng, danh sách liên kết, cây, đồ thị) và các giải thuật sắp xếp, tìm kiếm.'
            WHEN 'Lập trình hướng đối tượng' THEN 'Môn học giới thiệu các nguyên lý lập trình hướng đối tượng: đóng gói, kế thừa, đa hình, trừu tượng thông qua Java/C++.'
            WHEN 'Cơ sở dữ liệu' THEN 'Môn học cung cấp kiến thức về mô hình dữ liệu quan hệ, ngôn ngữ SQL, thiết kế cơ sở dữ liệu và tối ưu truy vấn.'
            ELSE 'Môn học ' || current_name_vi || ' cung cấp kiến thức chuyên sâu và kỹ năng thực hành cần thiết trong lĩnh vực CNTT.'
        END
    WHERE description IS NULL OR description = '';

    -- 1.2. Cập nhật số giờ học theo tên cột thực tế (An toàn cho cả 2 phiên bản schema)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_theory_hours') THEN
        UPDATE core_service.subjects SET default_theory_hours = default_credits * 15;
        UPDATE core_service.subjects SET default_practice_hours = default_credits * 15;
        UPDATE core_service.subjects SET default_self_study_hours = default_credits * 30;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'theory_hours') THEN
        UPDATE core_service.subjects SET theory_hours = default_credits * 15;
        UPDATE core_service.subjects SET practice_hours = default_credits * 15;
        UPDATE core_service.subjects SET self_study_hours = default_credits * 30;
    END IF;
END $$;

-- =====================================================
-- 2. LÀM SẠCH VÀ TẠO LẠI CLO CHI TIẾT
-- =====================================================
DELETE FROM core_service.assessment_clo_mappings;
DELETE FROM core_service.clo_plo_mappings;
DELETE FROM core_service.clos;

DO $$
DECLARE
    syllabus_rec RECORD;
BEGIN
    FOR syllabus_rec IN 
        SELECT sv.id, s.current_name_vi 
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO1', 'Trình bày và giải thích được các khái niệm cơ bản của ' || syllabus_rec.current_name_vi, 'Understand', 20.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO2', 'Vận dụng kiến thức ' || syllabus_rec.current_name_vi || ' vào giải quyết bài toán thực tế.', 'Apply', 30.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO3', 'Phân tích và đánh giá các giải pháp kỹ thuật trong ' || syllabus_rec.current_name_vi, 'Analyze', 30.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO4', 'Kỹ năng làm việc nhóm và thiết kế giải pháp hoàn chỉnh.', 'Create', 20.00);
    END LOOP;
END $$;

-- =====================================================
-- 3. TẠO PLO VÀ MAPPING CLO-PLO
-- =====================================================
INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO1', 'Áp dụng kiến thức toán học, khoa học và kỹ thuật', 'Knowledge', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO1' AND curriculum_id = c.id);

INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO2', 'Phân tích và giải quyết vấn đề CNTT', 'Skills', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO2' AND curriculum_id = c.id);

-- Mapping tự động (Logic của Team)
INSERT INTO core_service.clo_plo_mappings (id, clo_id, plo_id, mapping_level)
SELECT gen_random_uuid(), c.id, p.id, 'H'
FROM core_service.clos c, core_service.plos p
WHERE (c.code = 'CLO1' AND p.code = 'PLO1') OR (c.code = 'CLO3' AND p.code = 'PLO2')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 4. TẠO CƠ CHẾ ĐÁNH GIÁ (ASSESSMENT SCHEMES)
-- =====================================================
DELETE FROM core_service.assessment_schemes;

DO $$
DECLARE
    syllabus_rec RECORD;
    assess_id UUID;
BEGIN
    FOR syllabus_rec IN SELECT id FROM core_service.syllabus_versions LOOP
        -- Quá trình (40%)
        assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent)
        VALUES (assess_id, syllabus_rec.id, 'Đánh giá quá trình (Bài tập/Lab)', 40.00);
        
        -- Cuối kỳ (60%)
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'Thi cuối kỳ', 60.00);
    END LOOP;
END $$;

-- =====================================================
-- 5. CẬP NHẬT NỘI DUNG JSONB CHO SYLLABUS
-- =====================================================
UPDATE core_service.syllabus_versions sv SET content = jsonb_build_object(
    'description', s.description,
    'objectives', jsonb_build_array(
        'Nắm vững lý thuyết cốt lõi và xu hướng công nghệ.',
        'Thành thạo công cụ và ngôn ngữ liên quan.',
        'Tư duy phản biện và giải quyết vấn đề.',
        'Đạo đức nghề nghiệp và trách nhiệm xã hội.'
    ),
    'teachingMethods', jsonb_build_array(
        'Giảng dạy lý thuyết và thảo luận.',
        'Thực hành Lab trực tiếp.',
        'Học tập qua dự án (Project-based learning).'
    ),
    'courseOutline', jsonb_build_array(
        jsonb_build_object('week', 1, 'topic', 'Giới thiệu tổng quan', 'activities', 'Thuyết trình'),
        jsonb_build_object('week', 2, 'topic', 'Cơ sở lý thuyết', 'activities', 'Bài giảng'),
        jsonb_build_object('week', 4, 'topic', 'Thực hành kỹ thuật', 'activities', 'Lab')
    )
)
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- Summary
DO $$ BEGIN 
    RAISE NOTICE 'V13 Migration: Toàn bộ dữ liệu mẫu Tiếng Việt đã được đồng bộ.'; 
END $$;