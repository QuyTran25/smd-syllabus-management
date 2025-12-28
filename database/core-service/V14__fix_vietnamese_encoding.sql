-- V14: Fix Vietnamese encoding for all tables
-- Run with: docker exec -i smd-postgres psql -U smd_user -d smd_database < V14__fix_vietnamese_encoding.sql

SET client_encoding = 'UTF8';

-- ============================================
-- 1. Fix subjects table
-- ============================================
UPDATE core_service.subjects SET
    current_name_vi = 'Tin học cơ bản',
    description = 'Kỹ năng tin học văn phòng.'
WHERE code = '124012';

UPDATE core_service.subjects SET
    current_name_vi = 'Đại số',
    description = 'Ma trận, Định thức, Hệ phương trình tuyến tính.'
WHERE code = '001201';

UPDATE core_service.subjects SET
    current_name_vi = 'Giải tích 1',
    description = 'Giới hạn, Đạo hàm, Tích phân.'
WHERE code = '001202';

UPDATE core_service.subjects SET
    current_name_vi = 'Toán chuyên đề 1',
    description = 'Xác suất thống kê, kiểm định giả thiết.'
WHERE code = '001205';

UPDATE core_service.subjects SET
    current_name_vi = 'Nhập môn ngành CNTT',
    description = 'Định hướng nghề nghiệp, nhập môn Python.'
WHERE code = '122042';

UPDATE core_service.subjects SET
    current_name_vi = 'Cơ sở dữ liệu',
    description = 'SQL, Đại số quan hệ, Thiết kế CSDL.'
WHERE code = '120001';

UPDATE core_service.subjects SET
    current_name_vi = 'Kiến trúc máy tính',
    description = 'Kiến trúc máy tính, bộ nhớ, CPU.'
WHERE code = '122002';

UPDATE core_service.subjects SET
    current_name_vi = 'Hệ điều hành',
    description = 'Process, Thread, Deadlock, Memory Management.'
WHERE code = '125001';

UPDATE core_service.subjects SET
    current_name_vi = 'Lập trình hướng đối tượng',
    description = 'OOP, Kế thừa, Đa hình, Đóng gói.'
WHERE code = '122001';

UPDATE core_service.subjects SET
    current_name_vi = 'Cấu trúc dữ liệu và giải thuật',
    description = 'Mảng, Danh sách liên kết, Cây, Đồ thị, Sắp xếp, Tìm kiếm.'
WHERE code = '122000';

UPDATE core_service.subjects SET
    current_name_vi = 'Mạng máy tính',
    description = 'Giao thức mạng, TCP/IP, Routing.'
WHERE code = '124001';

UPDATE core_service.subjects SET
    current_name_vi = 'Công nghệ phần mềm',
    description = 'Quy trình phát triển phần mềm, Agile, Scrum.'
WHERE code = '123001';

UPDATE core_service.subjects SET
    current_name_vi = 'An toàn thông tin',
    description = 'Mã hóa, Bảo mật, Tấn công và phòng thủ.'
WHERE code = '124002';

UPDATE core_service.subjects SET
    current_name_vi = 'Trí tuệ nhân tạo',
    description = 'Machine Learning, Neural Network, AI cơ bản.'
WHERE code = '123002';

UPDATE core_service.subjects SET
    current_name_vi = 'Phát triển ứng dụng Web',
    description = 'HTML, CSS, JavaScript, React, Node.js.'
WHERE code = '123003';

UPDATE core_service.subjects SET
    current_name_vi = 'Phát triển ứng dụng di động',
    description = 'Android, iOS, React Native, Flutter.'
WHERE code = '123004';

UPDATE core_service.subjects SET
    current_name_vi = 'Quản trị dự án CNTT',
    description = 'Quản lý dự án, PMBOK, Agile, Scrum Master.'
WHERE code = '123005';

UPDATE core_service.subjects SET
    current_name_vi = 'Thực tập tốt nghiệp',
    description = 'Thực tập tại doanh nghiệp.'
WHERE code = '126000';

UPDATE core_service.subjects SET
    current_name_vi = 'Luận văn tốt nghiệp',
    description = 'Nghiên cứu và thực hiện đề tài tốt nghiệp.'
WHERE code = '126001';

-- Update default hours for subjects
UPDATE core_service.subjects SET
    default_theory_hours = 30,
    default_practice_hours = 30,
    default_self_study_hours = 60
WHERE default_credits = 3;

UPDATE core_service.subjects SET
    default_theory_hours = 45,
    default_practice_hours = 30,
    default_self_study_hours = 75
WHERE default_credits = 4;

UPDATE core_service.subjects SET
    default_theory_hours = 15,
    default_practice_hours = 15,
    default_self_study_hours = 30
WHERE default_credits = 2;

-- ============================================
-- 2. Fix CLOs table - Vietnamese descriptions
-- ============================================

-- First, let's update CLO descriptions with proper Vietnamese
-- Get syllabus IDs and their subject names to create proper CLO descriptions

-- Delete old CLOs and recreate with proper Vietnamese
DELETE FROM core_service.clo_plo_mappings;
DELETE FROM core_service.assessment_clo_mappings;
DELETE FROM core_service.clos;

-- Insert CLOs for each syllabus with proper Vietnamese
DO $$
DECLARE
    syllabus RECORD;
    subject_name TEXT;
    clo_id UUID;
BEGIN
    FOR syllabus IN 
        SELECT sv.id as syllabus_id, s.current_name_vi as subject_name
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        subject_name := syllabus.subject_name;
        
        -- CLO1: Remember/Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight_percentage)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO1',
            'Trình bày được các khái niệm cơ bản, định nghĩa và nguyên lý nền tảng của ' || subject_name,
            'UNDERSTAND',
            20.00
        );
        
        -- CLO2: Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight_percentage)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO2',
            'Giải thích được cách thức hoạt động và mối quan hệ giữa các thành phần trong ' || subject_name,
            'UNDERSTAND',
            15.00
        );
        
        -- CLO3: Apply
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight_percentage)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO3',
            'Áp dụng kiến thức ' || subject_name || ' để giải quyết các bài toán thực tế',
            'APPLY',
            25.00
        );
        
        -- CLO4: Analyze
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight_percentage)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO4',
            'Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực ' || subject_name,
            'ANALYZE',
            20.00
        );
        
        -- CLO5: Create
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight_percentage)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO5',
            'Thiết kế và xây dựng được giải pháp hoàn chỉnh ứng dụng kiến thức ' || subject_name,
            'CREATE',
            20.00
        );
    END LOOP;
END $$;

-- ============================================
-- 3. Recreate CLO-PLO mappings
-- ============================================
DO $$
DECLARE
    clo RECORD;
    plo RECORD;
    mapping_level TEXT;
BEGIN
    FOR clo IN SELECT id, code FROM core_service.clos LOOP
        FOR plo IN SELECT id, code FROM core_service.program_learning_outcomes ORDER BY RANDOM() LIMIT 3 LOOP
            -- Determine mapping level based on CLO code
            IF clo.code IN ('CLO1', 'CLO2') THEN
                mapping_level := 'I'; -- Introduce
            ELSIF clo.code IN ('CLO3', 'CLO4') THEN
                mapping_level := 'R'; -- Reinforce
            ELSE
                mapping_level := 'M'; -- Master
            END IF;
            
            INSERT INTO core_service.clo_plo_mappings (id, clo_id, plo_id, mapping_level, created_at, updated_at)
            VALUES (gen_random_uuid(), clo.id, plo.id, mapping_level, NOW(), NOW())
            ON CONFLICT DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- ============================================
-- 4. Recreate Assessment-CLO mappings
-- ============================================
DO $$
DECLARE
    assessment RECORD;
    clo RECORD;
BEGIN
    FOR assessment IN 
        SELECT a.id as assessment_id, a.syllabus_version_id, a.name
        FROM core_service.assessment_schemes a
    LOOP
        -- Link each assessment to 2-3 CLOs from the same syllabus
        FOR clo IN 
            SELECT id FROM core_service.clos 
            WHERE syllabus_version_id = assessment.syllabus_version_id
            ORDER BY RANDOM() LIMIT 3
        LOOP
            INSERT INTO core_service.assessment_clo_mappings (id, assessment_id, clo_id, created_at)
            VALUES (gen_random_uuid(), assessment.assessment_id, clo.id, NOW())
            ON CONFLICT DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- ============================================
-- 5. Update syllabus_versions content with proper Vietnamese
-- ============================================
UPDATE core_service.syllabus_versions sv
SET 
    content = jsonb_set(
        jsonb_set(
            jsonb_set(
                COALESCE(content, '{}'::jsonb),
                '{objectives}',
                '"Trình bày được các khái niệm cơ bản và nguyên lý nền tảng của môn học. Áp dụng kiến thức lý thuyết vào việc giải quyết các bài toán thực tế. Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực liên quan. Thiết kế và triển khai các giải pháp phù hợp với yêu cầu. Làm việc nhóm hiệu quả và trình bày kết quả một cách chuyên nghiệp."'
            ),
            '{teachingMethods}',
            '"Thuyết trình kết hợp slides và video minh họa. Thảo luận nhóm và nghiên cứu tình huống. Thực hành tại phòng lab với bài tập có hướng dẫn. Làm đồ án nhóm với báo cáo và thuyết trình. Tự học với tài liệu và video được cung cấp."'
        ),
        '{description}',
        to_jsonb(s.description)
    ),
    keywords = ARRAY['Lập trình', 'Phần mềm', 'Công nghệ']
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- ============================================
-- 6. Update time allocation in syllabus_versions
-- ============================================
UPDATE core_service.syllabus_versions sv
SET 
    theory_hours = s.default_theory_hours,
    practice_hours = s.default_practice_hours,
    self_study_hours = s.default_self_study_hours
FROM core_service.subjects s
WHERE sv.subject_id = s.id;

-- Verify the updates
SELECT 'Subjects updated:' as info, COUNT(*) as count FROM core_service.subjects WHERE current_name_vi NOT LIKE '%?%';
SELECT 'CLOs created:' as info, COUNT(*) as count FROM core_service.clos;
SELECT 'CLO-PLO mappings:' as info, COUNT(*) as count FROM core_service.clo_plo_mappings;
SELECT 'Assessment-CLO mappings:' as info, COUNT(*) as count FROM core_service.assessment_clo_mappings;
