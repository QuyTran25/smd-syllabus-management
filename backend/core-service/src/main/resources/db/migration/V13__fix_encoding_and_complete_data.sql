-- V13: Fix encoding and add complete data for syllabus details
-- This migration fixes Vietnamese encoding issues and adds missing data

-- =====================================================
-- 0. Add mapping_level column to clo_plo_mappings if not exists
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'core_service' 
        AND table_name = 'clo_plo_mappings' 
        AND column_name = 'mapping_level'
    ) THEN
        ALTER TABLE core_service.clo_plo_mappings ADD COLUMN mapping_level VARCHAR(10) DEFAULT 'M';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'core_service' 
        AND table_name = 'clo_plo_mappings' 
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE core_service.clo_plo_mappings ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- =====================================================
-- 1. Update Subject information (loại học phần, thành phần, khoa, thời gian)
-- Sử dụng text để tránh lỗi ENUM, JPA sẽ tự động convert
-- =====================================================

-- Update subjects with proper Vietnamese data and time allocation
UPDATE core_service.subjects s SET
    default_theory_hours = s.default_credits * 15,
    default_practice_hours = CASE 
        WHEN s.code LIKE '11%' THEN 0 
        ELSE s.default_credits * 15 
    END,
    default_self_study_hours = s.default_credits * 30,
    description = CASE s.current_name_vi
        WHEN 'Nhập môn lập trình' THEN 'Môn học cung cấp kiến thức cơ bản về lập trình, bao gồm các khái niệm về biến, kiểu dữ liệu, cấu trúc điều khiển, hàm và mảng. Sinh viên được thực hành với ngôn ngữ Python hoặc C.'
        WHEN 'Cấu trúc dữ liệu và giải thuật' THEN 'Môn học trình bày các cấu trúc dữ liệu cơ bản (mảng, danh sách liên kết, cây, đồ thị) và các giải thuật sắp xếp, tìm kiếm. Sinh viên học cách phân tích độ phức tạp thuật toán.'
        WHEN 'Lập trình hướng đối tượng' THEN 'Môn học giới thiệu các nguyên lý lập trình hướng đối tượng: đóng gói, kế thừa, đa hình, trừu tượng. Thực hành với Java hoặc C++.'
        WHEN 'Cơ sở dữ liệu' THEN 'Môn học cung cấp kiến thức về mô hình dữ liệu quan hệ, ngôn ngữ SQL, thiết kế cơ sở dữ liệu chuẩn hóa và các kỹ thuật tối ưu truy vấn.'
        WHEN 'Hệ điều hành' THEN 'Môn học trình bày các khái niệm cơ bản về hệ điều hành: quản lý tiến trình, bộ nhớ, hệ thống file, đồng bộ hóa và deadlock.'
        WHEN 'Mạng máy tính' THEN 'Môn học giới thiệu kiến trúc mạng OSI/TCP-IP, các giao thức mạng, địa chỉ IP, routing và các dịch vụ mạng cơ bản.'
        WHEN 'Kiến trúc máy tính' THEN 'Môn học trình bày cấu trúc và nguyên lý hoạt động của máy tính: CPU, bộ nhớ, bus, I/O và ngôn ngữ Assembly.'
        WHEN 'Công nghệ phần mềm' THEN 'Môn học giới thiệu quy trình phát triển phần mềm, các phương pháp Agile/Scrum, quản lý yêu cầu và kiểm thử phần mềm.'
        WHEN 'An toàn và bảo mật thông tin' THEN 'Môn học cung cấp kiến thức về mật mã học, xác thực, ủy quyền, các lỗ hổng bảo mật web và cách phòng chống.'
        WHEN 'Trí tuệ nhân tạo' THEN 'Môn học giới thiệu các kỹ thuật AI cơ bản: tìm kiếm, logic, học máy, xử lý ngôn ngữ tự nhiên và thị giác máy tính.'
        WHEN 'Phân tích thiết kế hệ thống' THEN 'Môn học hướng dẫn phương pháp phân tích và thiết kế hệ thống thông tin: UML, use case, class diagram, sequence diagram.'
        WHEN 'Lập trình Web' THEN 'Môn học cung cấp kiến thức phát triển ứng dụng web: HTML, CSS, JavaScript, framework frontend và backend.'
        ELSE 'Môn học ' || s.current_name_vi || ' cung cấp kiến thức chuyên sâu và kỹ năng thực hành cần thiết trong lĩnh vực CNTT.'
    END
WHERE s.description IS NULL OR s.description = '';

-- =====================================================
-- 2. Delete old CLOs and recreate with proper Vietnamese encoding
-- =====================================================

-- First delete assessment_clo_mappings that reference these CLOs
DELETE FROM core_service.assessment_clo_mappings
WHERE clo_id IN (SELECT id FROM core_service.clos);

-- Delete CLO-PLO mappings
DELETE FROM core_service.clo_plo_mappings
WHERE clo_id IN (SELECT id FROM core_service.clos);

-- Delete old CLOs
DELETE FROM core_service.clos;

-- Insert CLOs with proper Vietnamese for each syllabus version
DO $$
DECLARE
    syllabus_rec RECORD;
    subject_name TEXT;
    clo_id UUID;
BEGIN
    FOR syllabus_rec IN 
        SELECT sv.id as syllabus_id, s.current_name_vi as subject_name
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        subject_name := syllabus_rec.subject_name;
        
        -- CLO1 - Remember/Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO1',
            'Trình bày được các khái niệm cơ bản, định nghĩa và nguyên lý nền tảng của ' || subject_name,
            'Understand',
            20.00,
            NOW(), NOW()
        );
        
        -- CLO2 - Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO2',
            'Giải thích được cách thức hoạt động và mối quan hệ giữa các thành phần trong ' || subject_name,
            'Understand',
            15.00,
            NOW(), NOW()
        );
        
        -- CLO3 - Apply
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO3',
            'Áp dụng kiến thức ' || subject_name || ' để giải quyết các bài toán thực tế',
            'Apply',
            25.00,
            NOW(), NOW()
        );
        
        -- CLO4 - Analyze
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO4',
            'Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực ' || subject_name,
            'Analyze',
            20.00,
            NOW(), NOW()
        );
        
        -- CLO5 - Create/Evaluate
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO5',
            'Thiết kế và xây dựng được giải pháp hoàn chỉnh ứng dụng kiến thức ' || subject_name,
            'Create',
            20.00,
            NOW(), NOW()
        );
    END LOOP;
END $$;

-- =====================================================
-- 3. Create CLO-PLO Mappings
-- =====================================================

-- First ensure PLOs exist
INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO1', 'Có khả năng áp dụng kiến thức toán học, khoa học và kỹ thuật', 'Knowledge', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO1' AND curriculum_id = c.id);

INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO2', 'Có khả năng phân tích và giải quyết vấn đề trong lĩnh vực CNTT', 'Skills', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO2' AND curriculum_id = c.id);

INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO3', 'Có khả năng thiết kế hệ thống hoặc quy trình đáp ứng yêu cầu', 'Skills', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO3' AND curriculum_id = c.id);

INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO4', 'Có khả năng làm việc nhóm hiệu quả', 'Attitude', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO4' AND curriculum_id = c.id);

INSERT INTO core_service.plos (id, curriculum_id, code, description, category, created_at, updated_at)
SELECT gen_random_uuid(), c.id, 'PLO5', 'Có khả năng giao tiếp hiệu quả bằng văn bản và thuyết trình', 'Attitude', NOW(), NOW()
FROM core_service.curriculums c
WHERE NOT EXISTS (SELECT 1 FROM core_service.plos WHERE code = 'PLO5' AND curriculum_id = c.id);

-- Create CLO-PLO mappings for each syllabus
DO $$
DECLARE
    clo_rec RECORD;
    plo_rec RECORD;
    mapping_level TEXT;
BEGIN
    FOR clo_rec IN 
        SELECT c.id as clo_id, c.code as clo_code, c.syllabus_version_id, 
               s.curriculum_id
        FROM core_service.clos c
        JOIN core_service.syllabus_versions sv ON c.syllabus_version_id = sv.id
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        -- Map each CLO to relevant PLOs
        FOR plo_rec IN 
            SELECT id as plo_id, code as plo_code
            FROM core_service.plos
            WHERE curriculum_id = clo_rec.curriculum_id
        LOOP
            -- Determine mapping level based on CLO-PLO relationship
            mapping_level := CASE 
                -- CLO1 (Understand) -> PLO1 (Knowledge) = High
                WHEN clo_rec.clo_code = 'CLO1' AND plo_rec.plo_code = 'PLO1' THEN 'H'
                -- CLO2 (Understand) -> PLO1 (Knowledge) = Medium
                WHEN clo_rec.clo_code = 'CLO2' AND plo_rec.plo_code = 'PLO1' THEN 'M'
                -- CLO3 (Apply) -> PLO2 (Problem solving) = High
                WHEN clo_rec.clo_code = 'CLO3' AND plo_rec.plo_code = 'PLO2' THEN 'H'
                -- CLO3 (Apply) -> PLO3 (Design) = Medium
                WHEN clo_rec.clo_code = 'CLO3' AND plo_rec.plo_code = 'PLO3' THEN 'M'
                -- CLO4 (Analyze) -> PLO2 (Problem solving) = High
                WHEN clo_rec.clo_code = 'CLO4' AND plo_rec.plo_code = 'PLO2' THEN 'H'
                -- CLO5 (Create) -> PLO3 (Design) = High
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code = 'PLO3' THEN 'H'
                -- CLO5 (Create) -> PLO4 (Teamwork) = Medium
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code = 'PLO4' THEN 'M'
                -- CLO5 (Create) -> PLO5 (Communication) = Low
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code = 'PLO5' THEN 'L'
                ELSE NULL
            END;
            
            IF mapping_level IS NOT NULL THEN
                INSERT INTO core_service.clo_plo_mappings (id, clo_id, plo_id, mapping_level, created_at, updated_at)
                VALUES (gen_random_uuid(), clo_rec.clo_id, plo_rec.plo_id, mapping_level, NOW(), NOW())
                ON CONFLICT DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- 4. Recreate Assessment Schemes with complete info
-- =====================================================

-- Delete old assessment schemes
DELETE FROM core_service.assessment_schemes;

-- Create assessments for each syllabus
DO $$
DECLARE
    syllabus_rec RECORD;
    assess_id UUID;
    clo_rec RECORD;
BEGIN
    FOR syllabus_rec IN SELECT id FROM core_service.syllabus_versions LOOP
        -- Assessment 1: Attendance & Participation (10%)
        assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at)
        VALUES (assess_id, syllabus_rec.id, 'Chuyên cần và thái độ học tập', 10.00, NOW(), NOW());
        
        -- Link to CLO1, CLO2
        FOR clo_rec IN SELECT id FROM core_service.clos WHERE syllabus_version_id = syllabus_rec.id AND code IN ('CLO1', 'CLO2') LOOP
            INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
            VALUES (gen_random_uuid(), assess_id, clo_rec.id, NOW(), NOW());
        END LOOP;
        
        -- Assessment 2: Assignments & Lab (20%)
        assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at)
        VALUES (assess_id, syllabus_rec.id, 'Bài tập và thực hành', 20.00, NOW(), NOW());
        
        -- Link to CLO2, CLO3
        FOR clo_rec IN SELECT id FROM core_service.clos WHERE syllabus_version_id = syllabus_rec.id AND code IN ('CLO2', 'CLO3') LOOP
            INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
            VALUES (gen_random_uuid(), assess_id, clo_rec.id, NOW(), NOW());
        END LOOP;
        
        -- Assessment 3: Midterm (30%)
        assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at)
        VALUES (assess_id, syllabus_rec.id, 'Kiểm tra giữa kỳ', 30.00, NOW(), NOW());
        
        -- Link to CLO1, CLO2, CLO3, CLO4
        FOR clo_rec IN SELECT id FROM core_service.clos WHERE syllabus_version_id = syllabus_rec.id AND code IN ('CLO1', 'CLO2', 'CLO3', 'CLO4') LOOP
            INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
            VALUES (gen_random_uuid(), assess_id, clo_rec.id, NOW(), NOW());
        END LOOP;
        
        -- Assessment 4: Final Exam (40%)
        assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at)
        VALUES (assess_id, syllabus_rec.id, 'Thi cuối kỳ', 40.00, NOW(), NOW());
        
        -- Link to all CLOs
        FOR clo_rec IN SELECT id FROM core_service.clos WHERE syllabus_version_id = syllabus_rec.id LOOP
            INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
            VALUES (gen_random_uuid(), assess_id, clo_rec.id, NOW(), NOW());
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- 5. Update syllabus_versions content with proper Vietnamese
-- =====================================================

UPDATE core_service.syllabus_versions sv SET content = jsonb_build_object(
    'description', s.description,
    'objectives', jsonb_build_array(
        'Trình bày được các khái niệm cơ bản và nguyên lý nền tảng của môn học',
        'Áp dụng kiến thức lý thuyết vào việc giải quyết các bài toán thực tế',
        'Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực liên quan',
        'Thiết kế và triển khai các giải pháp phù hợp với yêu cầu',
        'Làm việc nhóm hiệu quả và trình bày kết quả một cách chuyên nghiệp'
    ),
    'prerequisites', jsonb_build_array(
        jsonb_build_object('code', 'CS101', 'name', 'Nhập môn lập trình', 'type', 'required'),
        jsonb_build_object('code', 'CS102', 'name', 'Cấu trúc dữ liệu', 'type', 'recommended')
    ),
    'textbooks', jsonb_build_array(
        jsonb_build_object(
            'title', 'Giáo trình ' || s.current_name_vi,
            'authors', 'Nguyễn Văn A, Trần Thị B',
            'publisher', 'NXB Đại học Quốc gia',
            'year', 2023,
            'type', 'required'
        ),
        jsonb_build_object(
            'title', 'Bài tập và thực hành ' || s.current_name_vi,
            'authors', 'Lê Văn C',
            'publisher', 'NXB Khoa học Kỹ thuật',
            'year', 2022,
            'type', 'reference'
        )
    ),
    'teachingMethods', jsonb_build_array(
        'Thuyết trình kết hợp slides và video minh họa',
        'Thảo luận nhóm và nghiên cứu tình huống',
        'Thực hành tại phòng lab với bài tập có hướng dẫn',
        'Làm đồ án nhóm với báo cáo và thuyết trình',
        'Tự học với tài liệu và video được cung cấp'
    ),
    'gradingPolicy', jsonb_build_object(
        'attendance', 10,
        'assignments', 20,
        'midterm', 30,
        'final', 40,
        'bonus', 'Cộng điểm cho sinh viên tích cực tham gia thảo luận và có đóng góp xuất sắc'
    ),
    'courseOutline', jsonb_build_array(
        jsonb_build_object(
            'week', 1, 
            'topic', 'Giới thiệu môn học và tổng quan',
            'content', 'Giới thiệu mục tiêu, nội dung, phương pháp học tập và đánh giá',
            'readings', 'Chương 1 - Giáo trình chính',
            'activities', 'Thuyết trình, thảo luận nhóm'
        ),
        jsonb_build_object(
            'week', 2, 
            'topic', 'Các khái niệm cơ bản',
            'content', 'Trình bày các khái niệm nền tảng và thuật ngữ chuyên ngành',
            'readings', 'Chương 2 - Giáo trình chính',
            'activities', 'Bài giảng, bài tập thực hành'
        ),
        jsonb_build_object(
            'week', 3, 
            'topic', 'Phương pháp và kỹ thuật cơ bản',
            'content', 'Giới thiệu các phương pháp và kỹ thuật giải quyết vấn đề',
            'readings', 'Chương 3, 4 - Giáo trình chính',
            'activities', 'Workshop, case study'
        ),
        jsonb_build_object(
            'week', 4, 
            'topic', 'Thực hành và ứng dụng',
            'content', 'Áp dụng kiến thức vào các bài toán thực tế',
            'readings', 'Tài liệu hướng dẫn thực hành',
            'activities', 'Lab practice, project nhóm'
        ),
        jsonb_build_object(
            'week', 5, 
            'topic', 'Kiểm tra giữa kỳ',
            'content', 'Đánh giá kiến thức đã học từ tuần 1-4',
            'readings', 'Ôn tập toàn bộ nội dung',
            'activities', 'Thi viết, bài tập lớn'
        ),
        jsonb_build_object(
            'week', 6, 
            'topic', 'Chủ đề nâng cao 1',
            'content', 'Đi sâu vào các kỹ thuật và phương pháp nâng cao',
            'readings', 'Chương 5, 6 - Giáo trình chính',
            'activities', 'Bài giảng chuyên sâu'
        ),
        jsonb_build_object(
            'week', 7, 
            'topic', 'Chủ đề nâng cao 2',
            'content', 'Tiếp tục các nội dung nâng cao và ứng dụng thực tế',
            'readings', 'Chương 7 - Giáo trình chính',
            'activities', 'Seminar, thảo luận'
        ),
        jsonb_build_object(
            'week', 8, 
            'topic', 'Tổng kết và ôn tập',
            'content', 'Hệ thống hóa kiến thức và chuẩn bị thi cuối kỳ',
            'readings', 'Toàn bộ giáo trình',
            'activities', 'Ôn tập, Q&A'
        )
    )
)
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- =====================================================
-- Summary
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '=== V13 Migration Summary ===';
    RAISE NOTICE 'Subjects updated: %', (SELECT COUNT(*) FROM core_service.subjects WHERE description IS NOT NULL);
    RAISE NOTICE 'CLOs created: %', (SELECT COUNT(*) FROM core_service.clos);
    RAISE NOTICE 'PLOs created: %', (SELECT COUNT(*) FROM core_service.plos);
    RAISE NOTICE 'CLO-PLO mappings: %', (SELECT COUNT(*) FROM core_service.clo_plo_mappings);
    RAISE NOTICE 'Assessment schemes: %', (SELECT COUNT(*) FROM core_service.assessment_schemes);
    RAISE NOTICE 'Assessment-CLO mappings: %', (SELECT COUNT(*) FROM core_service.assessment_clo_mappings);
END $$;