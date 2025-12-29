/*
 * V13__fix_encoding_and_complete_data.sql
 * Khôi phục toàn bộ dữ liệu mẫu chi tiết và sửa lỗi hiển thị tiếng Việt
 * FIX: Cập nhật từng cột độc lập để tránh lỗi "column does not exist"
 */

SET search_path TO core_service, public;

-- =====================================================
-- 1. CẬP NHẬT THÔNG TIN CHI TIẾT CHO TOÀN BỘ MÔN HỌC
-- =====================================================
DO $$
BEGIN
    -- 1.1. Cập nhật các trường thông tin cơ bản luôn tồn tại
    UPDATE core_service.subjects SET
        subject_type = 'REQUIRED',
        component = 'BOTH',
        description = CASE code
            WHEN '124001' THEN 'Môn học cung cấp kiến thức nền tảng về lập trình C/C++, con trỏ, quản lý bộ nhớ và các kỹ thuật đệ quy phức tạp.'
            WHEN '124002' THEN 'Trình bày các cấu trúc dữ liệu cơ bản như mảng, danh sách liên kết, cây, đồ thị và các giải thuật sắp xếp, tìm kiếm tối ưu.'
            WHEN '122003' THEN 'Giới thiệu các nguyên lý lập trình hướng đối tượng: đóng gói, kế thừa, đa hình thông qua ngôn ngữ Java hoặc C++.'
            WHEN '121000' THEN 'Cung cấp kiến thức về mô hình dữ liệu quan hệ, ngôn ngữ SQL chuẩn, thiết kế DB và các kỹ thuật tối ưu hóa truy vấn.'
            WHEN '125001' THEN 'Trình bày cơ chế quản lý tiến trình, bộ nhớ ảo, hệ thống tệp tin và các vấn đề về đồng bộ hóa trong hệ điều hành.'
            WHEN '123002' THEN 'Giới thiệu kiến trúc tầng OSI, TCP/IP, các giao thức định tuyến và dịch vụ mạng phổ biến hiện nay.'
            WHEN '122005' THEN 'Cung cấp quy trình phát triển phần mềm chuyên nghiệp, mô hình Agile/Scrum và kỹ năng làm việc nhóm trong dự án.'
            WHEN '121031' THEN 'Hướng dẫn phát triển ứng dụng Web toàn diện với HTML5, CSS3, JavaScript và các Framework hiện đại như React/NodeJS.'
            ELSE 'Môn học ' || current_name_vi || ' cung cấp kiến thức chuyên sâu và kỹ năng thực hành cần thiết.'
        END
    WHERE description IS NULL OR description = '';

    -- 1.2. Cập nhật số giờ học theo tên cột thực tế (Sửa lỗi dứt điểm tại đây)
    
    -- Cột Theory
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_theory_hours') THEN
        UPDATE core_service.subjects SET default_theory_hours = default_credits * 15;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'theory_hours') THEN
        UPDATE core_service.subjects SET theory_hours = default_credits * 15;
    END IF;

    -- Cột Practice
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_practice_hours') THEN
        UPDATE core_service.subjects SET default_practice_hours = default_credits * 15;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'practice_hours') THEN
        UPDATE core_service.subjects SET practice_hours = default_credits * 15;
    END IF;

    -- Cột Self-study
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'default_self_study_hours') THEN
        UPDATE core_service.subjects SET default_self_study_hours = default_credits * 30;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'core_service' AND table_name = 'subjects' AND column_name = 'self_study_hours') THEN
        UPDATE core_service.subjects SET self_study_hours = default_credits * 30;
    END IF;
END $$;

-- =====================================================
-- 2. TẠO LẠI CLO VỚI NỘI DUNG TIẾNG VIỆT CHI TIẾT
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
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO2', 'Vận dụng kiến thức ' || syllabus_rec.current_name_vi || ' vào thực tế.', 'Apply', 30.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO3', 'Phân tích và đánh giá giải pháp trong ' || syllabus_rec.current_name_vi, 'Analyze', 30.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (gen_random_uuid(), syllabus_rec.id, 'CLO4', 'Kỹ năng làm việc nhóm và báo cáo đồ án.', 'Create', 20.00);
    END LOOP;
END $$;

-- =====================================================
-- 3. CẬP NHẬT CONTENT JSONB (Nội dung chi tiết)
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

-- Log kết quả
DO $$ BEGIN RAISE NOTICE 'V13 Migration: Dữ liệu mẫu đã được khôi phục thành công.'; END $$;