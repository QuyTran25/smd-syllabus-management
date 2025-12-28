/*
 * V12__seed_syllabus_details.sql
 * Bổ sung dữ liệu chi tiết cho các đề cương:
 * - CLOs (Chuẩn đầu ra môn học)
 * - Assessment Schemes (Phương pháp đánh giá)
 * - CLO-PLO Mappings (Ánh xạ CLO-PLO)
 * - Content JSONB (Mục tiêu, mô tả, nội dung chi tiết)
 */

BEGIN;

SET search_path TO core_service;

DO $$BEGIN RAISE NOTICE 'Starting V12 Migration: Seeding Syllabus Details...'; END$$;

-- ==========================================
-- 1. UPDATE CONTENT JSONB FOR ALL SYLLABUS_VERSIONS
-- ==========================================

-- Cập nhật content với dữ liệu chi tiết cho tất cả syllabus
UPDATE syllabus_versions sv
SET content = jsonb_build_object(
    'description', 'Môn học ' || sv.snap_subject_name_vi || ' cung cấp cho sinh viên những kiến thức nền tảng và kỹ năng thực hành cần thiết. Học phần này là một phần quan trọng trong chương trình đào tạo ngành Công nghệ thông tin, giúp sinh viên xây dựng nền tảng vững chắc để học các môn chuyên ngành tiếp theo.',
    'objectives', jsonb_build_array(
        'Hiểu và áp dụng các khái niệm cơ bản của ' || sv.snap_subject_name_vi,
        'Phân tích và giải quyết các bài toán liên quan đến ' || sv.snap_subject_name_vi,
        'Thiết kế và triển khai các giải pháp kỹ thuật phù hợp',
        'Làm việc nhóm hiệu quả trong các dự án thực tế',
        'Phát triển kỹ năng tự học và nghiên cứu độc lập'
    ),
    'prerequisites', jsonb_build_array(
        jsonb_build_object('code', 'CS101', 'name', 'Nhập môn lập trình', 'type', 'required'),
        jsonb_build_object('code', 'CS102', 'name', 'Cấu trúc dữ liệu', 'type', 'recommended')
    ),
    'courseOutline', jsonb_build_array(
        jsonb_build_object(
            'week', 1,
            'topic', 'Giới thiệu môn học và tổng quan',
            'content', 'Giới thiệu mục tiêu, nội dung, phương pháp học tập và đánh giá',
            'activities', 'Thuyết trình, thảo luận nhóm',
            'readings', 'Chương 1 - Giáo trình chính'
        ),
        jsonb_build_object(
            'week', 2,
            'topic', 'Các khái niệm cơ bản',
            'content', 'Trình bày các khái niệm nền tảng và thuật ngữ chuyên ngành',
            'activities', 'Bài giảng, bài tập thực hành',
            'readings', 'Chương 2 - Giáo trình chính'
        ),
        jsonb_build_object(
            'week', 3,
            'topic', 'Phương pháp và kỹ thuật cơ bản',
            'content', 'Giới thiệu các phương pháp và kỹ thuật giải quyết vấn đề',
            'activities', 'Workshop, case study',
            'readings', 'Chương 3, 4 - Giáo trình chính'
        ),
        jsonb_build_object(
            'week', 4,
            'topic', 'Thực hành và ứng dụng',
            'content', 'Áp dụng kiến thức vào các bài toán thực tế',
            'activities', 'Lab practice, project nhóm',
            'readings', 'Tài liệu hướng dẫn thực hành'
        ),
        jsonb_build_object(
            'week', 5,
            'topic', 'Kiểm tra giữa kỳ',
            'content', 'Đánh giá kiến thức đã học từ tuần 1-4',
            'activities', 'Thi viết, bài tập lớn',
            'readings', 'Ôn tập toàn bộ nội dung'
        ),
        jsonb_build_object(
            'week', 6,
            'topic', 'Chủ đề nâng cao 1',
            'content', 'Đi sâu vào các kỹ thuật và phương pháp nâng cao',
            'activities', 'Bài giảng chuyên sâu',
            'readings', 'Chương 5, 6 - Giáo trình chính'
        ),
        jsonb_build_object(
            'week', 7,
            'topic', 'Chủ đề nâng cao 2',
            'content', 'Tiếp tục các nội dung nâng cao và ứng dụng thực tế',
            'activities', 'Seminar, thảo luận',
            'readings', 'Chương 7 - Giáo trình chính'
        ),
        jsonb_build_object(
            'week', 8,
            'topic', 'Tổng kết và ôn tập',
            'content', 'Hệ thống hóa kiến thức và chuẩn bị thi cuối kỳ',
            'activities', 'Ôn tập, Q&A',
            'readings', 'Toàn bộ giáo trình'
        )
    ),
    'textbooks', jsonb_build_array(
        jsonb_build_object(
            'title', 'Giáo trình ' || sv.snap_subject_name_vi,
            'authors', 'Nguyễn Văn A, Trần Thị B',
            'publisher', 'NXB Đại học Quốc gia',
            'year', 2023,
            'type', 'required'
        ),
        jsonb_build_object(
            'title', 'Advanced ' || sv.snap_subject_code || ' Concepts',
            'authors', 'John Smith, Jane Doe',
            'publisher', 'Pearson Education',
            'year', 2022,
            'type', 'reference'
        )
    ),
    'teachingMethods', jsonb_build_array(
        'Thuyết trình kết hợp slides và video minh họa',
        'Thảo luận nhóm và case study',
        'Thực hành tại phòng lab',
        'Làm đồ án nhóm',
        'Tự học với tài liệu hướng dẫn'
    ),
    'gradingPolicy', jsonb_build_object(
        'attendance', 10,
        'assignments', 20,
        'midterm', 30,
        'final', 40,
        'bonus', 'Có điểm cộng cho sinh viên tích cực tham gia'
    )
);

-- ==========================================
-- 2. INSERT CLOs FOR EACH SYLLABUS
-- ==========================================

-- Tạo CLOs cho tất cả syllabus_versions
INSERT INTO clos (syllabus_version_id, code, description, bloom_level, weight, created_by)
SELECT 
    sv.id,
    'CLO' || clo_num,
    CASE clo_num
        WHEN 1 THEN 'Hiểu và giải thích được các khái niệm cơ bản của ' || sv.snap_subject_name_vi
        WHEN 2 THEN 'Phân tích và đánh giá các vấn đề liên quan đến ' || sv.snap_subject_name_vi
        WHEN 3 THEN 'Áp dụng kiến thức để giải quyết bài toán thực tế trong lĩnh vực ' || sv.snap_subject_name_vi
        WHEN 4 THEN 'Thiết kế và triển khai giải pháp kỹ thuật cho các yêu cầu cụ thể'
        WHEN 5 THEN 'Làm việc nhóm hiệu quả và trình bày kết quả một cách chuyên nghiệp'
    END,
    CASE clo_num
        WHEN 1 THEN 'Understand'
        WHEN 2 THEN 'Analyze'
        WHEN 3 THEN 'Apply'
        WHEN 4 THEN 'Create'
        WHEN 5 THEN 'Evaluate'
    END,
    CASE clo_num
        WHEN 1 THEN 25.00
        WHEN 2 THEN 20.00
        WHEN 3 THEN 25.00
        WHEN 4 THEN 20.00
        WHEN 5 THEN 10.00
    END,
    sv.created_by
FROM syllabus_versions sv
CROSS JOIN generate_series(1, 5) AS clo_num
ON CONFLICT (syllabus_version_id, code) DO NOTHING;

-- ==========================================
-- 3. INSERT ASSESSMENT SCHEMES
-- ==========================================

-- Tạo Assessment Schemes cho tất cả syllabus_versions
INSERT INTO assessment_schemes (syllabus_version_id, name, weight_percent, created_by)
SELECT 
    sv.id,
    assessment_name,
    assessment_weight,
    sv.created_by
FROM syllabus_versions sv
CROSS JOIN (VALUES 
    ('Điểm danh và tham gia', 10.00),
    ('Bài tập và thực hành', 20.00),
    ('Kiểm tra giữa kỳ', 30.00),
    ('Thi cuối kỳ', 40.00)
) AS assessments(assessment_name, assessment_weight)
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. INSERT CLO-PLO MAPPINGS
-- ==========================================

-- Tạo ánh xạ CLO-PLO (giả định có PLOs trong database)
-- Chỉ insert nếu có PLOs tồn tại

INSERT INTO clo_plo_mappings (clo_id, plo_id, weight, created_by)
SELECT 
    c.id as clo_id,
    p.id as plo_id,
    CASE 
        WHEN c.code IN ('CLO1', 'CLO2') THEN 0.3
        WHEN c.code = 'CLO3' THEN 0.4
        ELSE 0.2
    END as weight,
    c.created_by
FROM clos c
CROSS JOIN LATERAL (
    -- Lấy 2-3 PLOs ngẫu nhiên cho mỗi CLO
    SELECT id FROM plos 
    ORDER BY RANDOM() 
    LIMIT 2
) p
WHERE EXISTS (SELECT 1 FROM plos LIMIT 1)
ON CONFLICT DO NOTHING;

-- ==========================================
-- 5. INSERT ASSESSMENT-CLO MAPPINGS
-- ==========================================

INSERT INTO assessment_clo_mappings (assessment_scheme_id, clo_id, contribution_percent)
SELECT 
    ass.id as assessment_scheme_id,
    c.id as clo_id,
    CASE 
        WHEN ass.name LIKE '%cuối kỳ%' THEN 40.00
        WHEN ass.name LIKE '%giữa kỳ%' THEN 30.00
        ELSE 15.00
    END as contribution_percent
FROM assessment_schemes ass
JOIN clos c ON c.syllabus_version_id = ass.syllabus_version_id
ON CONFLICT DO NOTHING;

-- ==========================================
-- 6. LOG SUMMARY
-- ==========================================

DO $$
DECLARE
    clo_count INT;
    assessment_count INT;
    mapping_count INT;
BEGIN
    SELECT COUNT(*) INTO clo_count FROM clos;
    SELECT COUNT(*) INTO assessment_count FROM assessment_schemes;
    SELECT COUNT(*) INTO mapping_count FROM clo_plo_mappings;
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'V12 Migration completed!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'CLOs created: %', clo_count;
    RAISE NOTICE 'Assessment schemes created: %', assessment_count;
    RAISE NOTICE 'CLO-PLO mappings created: %', mapping_count;
    RAISE NOTICE '================================';
END $$;

COMMIT;
