/*
 * V16__add_realistic_data.sql
 * Thêm dữ liệu thực tế: objectives, CLOs, PLOs, CLO-PLO mappings
 */

BEGIN;

SET search_path TO core_service;
SET client_encoding = 'UTF8';

-- =====================================================
-- 1. Update Objectives cho các môn học cụ thể
-- =====================================================

-- Môn Trí tuệ nhân tạo (122042)
UPDATE syllabus_versions sv SET objectives = 
'Nắm vững các khái niệm cơ bản về trí tuệ nhân tạo, machine learning và deep learning.
Áp dụng các thuật toán tìm kiếm, học máy để giải quyết bài toán thực tế.
Phân tích và đánh giá hiệu suất các mô hình AI qua các metrics chuẩn.
Thiết kế và triển khai ứng dụng AI sử dụng Python và các thư viện phổ biến.
Làm việc nhóm để phát triển dự án AI từ thu thập dữ liệu đến deployment.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '122042';

-- Môn Software Testing (124001)
UPDATE syllabus_versions sv SET objectives = 
'Nắm vững các phương pháp kiểm thử: unit testing, integration testing, system testing.
Thiết kế test cases, test plans và test reports theo chuẩn IEEE 829.
Sử dụng các công cụ automation testing như Selenium, JUnit, Jest.
Phân tích và báo cáo lỗi chi tiết theo quy trình Agile/Scrum.
Áp dụng Test-Driven Development (TDD) và Behavior-Driven Development (BDD).'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '124001';

-- Môn Software Architecture (124002)
UPDATE syllabus_versions sv SET objectives = 
'Hiểu các nguyên lý kiến trúc phần mềm: modularity, scalability, maintainability.
Thiết kế kiến trúc cho hệ thống quy mô lớn sử dụng design patterns.
Áp dụng microservices, event-driven architecture, và serverless.
Đánh giá và lựa chọn kiến trúc phù hợp dựa trên yêu cầu phi chức năng.
Sử dụng UML, C4 Model để mô tả và document kiến trúc hệ thống.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '124002';

-- Môn Lập trình hướng đối tượng (122002)
UPDATE syllabus_versions sv SET objectives = 
'Nắm vững 4 tính chất OOP: Encapsulation, Inheritance, Polymorphism, Abstraction.
Thiết kế class diagrams và áp dụng SOLID principles trong thực tế.
Sử dụng design patterns: Factory, Singleton, Observer, Strategy.
Xây dựng ứng dụng Java/C# với GUI và kết nối database.
Debug, refactoring và optimize code OOP theo clean code principles.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '122002';

-- Môn Web Programming (122003)
UPDATE syllabus_versions sv SET objectives = 
'Nắm vững HTML5, CSS3, JavaScript ES6+ và responsive design.
Phát triển web application với React/Vue và RESTful API.
Xây dựng backend với Node.js/Express và xác thực JWT.
Làm việc với database NoSQL (MongoDB) và SQL (PostgreSQL).
Deploy ứng dụng web lên cloud platform (AWS, Azure, Heroku).'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '122003';

-- Môn Network Security (125001)
UPDATE syllabus_versions sv SET objectives = 
'Hiểu các mối đe dọa bảo mật mạng: DDoS, MITM, phishing, malware.
Cấu hình firewall, IDS/IPS và VPN để bảo vệ hệ thống.
Áp dụng cryptography: symmetric, asymmetric, hashing, digital signature.
Thực hiện penetration testing và vulnerability assessment.
Tuân thủ các chuẩn bảo mật: ISO 27001, NIST, OWASP Top 10.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '125001';

-- Môn Data Structures and Algorithms (121008)
UPDATE syllabus_versions sv SET objectives = 
'Nắm vững các cấu trúc dữ liệu: array, linked list, stack, queue, tree, graph.
Phân tích độ phức tạp thuật toán với Big O notation.
Áp dụng thuật toán: sorting, searching, dynamic programming, greedy.
Giải quyết bài toán tối uu sử dụng cấu trúc dữ liệu phù hợp.
Implement các thuật toán bằng C/C++/Java với hiệu suất cao.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '121008';

-- Môn Database Management Systems (121031)
UPDATE syllabus_versions sv SET objectives = 
'Thiết kế database schema với ERD và normalization (1NF-3NF, BCNF).
Sử dụng SQL: DDL, DML, DCL, TCL và stored procedures.
Tối ưu query với indexing, query optimization và execution plan.
Quản lý transactions với ACID properties và concurrency control.
Backup, recovery và replication cho database production.'
FROM subjects s 
WHERE sv.subject_id = s.id AND s.code = '121031';

-- Các môn còn lại dùng objectives mặc định
UPDATE syllabus_versions sv SET objectives = 
'Trình bày được các khái niệm cơ bản và nguyên lý nền tảng của môn học.
Áp dụng kiến thức lý thuyết để giải quyết các bài toán và tình huống thực tế.
Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực chuyên môn.
Thiết kế và triển khai các giải pháp đáp ứng yêu cầu kỹ thuật và nghiệp vụ.
Làm việc nhóm hiệu quả, giao tiếp chuyên nghiệp và trình bày kết quả rõ ràng.'
FROM subjects s 
WHERE sv.subject_id = s.id 
  AND s.code NOT IN ('122042', '124001', '124002', '122002', '122003', '125001', '121008', '121031')
  AND sv.objectives IS NULL;

-- =====================================================
-- 2. Create PLOs (Program Learning Outcomes) theo chuẩn CDIO
-- =====================================================

-- Xóa PLOs cũ nếu có
DELETE FROM plos WHERE curriculum_id IN (SELECT id FROM curriculums);

-- Tạo 12 PLOs theo chuẩn CDIO cho mỗi curriculum
DO $$
DECLARE
    curr_rec RECORD;
BEGIN
    FOR curr_rec IN SELECT id FROM curriculums LOOP
        -- PLO1: Kiến thức nền tảng
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO1',
            'Áp dụng kiến thức toán học, khoa học tự nhiên và kỹ thuật vào giải quyết các vấn đề CNTT',
            'Knowledge',
            NOW(), NOW()
        );
        
        -- PLO2: Kiến thức chuyên môn sâu
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO2',
            'Vận dụng kiến thức chuyên sâu về lập trình, cấu trúc dữ liệu, cơ sở dữ liệu và mạng máy tính',
            'Knowledge',
            NOW(), NOW()
        );
        
        -- PLO3: Phân tích vấn đề
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO3',
            'Phân tích yêu cầu, thiết kế giải pháp và giải quyết vấn đề kỹ thuật phức tạp',
            'Skills',
            NOW(), NOW()
        );
        
        -- PLO4: Thiết kế hệ thống
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO4',
            'Thiết kế hệ thống phần mềm đáp ứng yêu cầu kỹ thuật, nghiệp vụ và bảo mật',
            'Skills',
            NOW(), NOW()
        );
        
        -- PLO5: Sử dụng công cụ
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO5',
            'Sử dụng thành thạo các công cụ, công nghệ và framework hiện đại trong phát triển phần mềm',
            'Skills',
            NOW(), NOW()
        );
        
        -- PLO6: Làm việc nhóm
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO6',
            'Làm việc hiệu quả trong nhóm đa ngành với vai trò thành viên hoặc leader',
            'Attitude',
            NOW(), NOW()
        );
        
        -- PLO7: Giao tiếp
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO7',
            'Giao tiếp hiệu quả bằng văn bản, thuyết trình và tài liệu kỹ thuật',
            'Attitude',
            NOW(), NOW()
        );
        
        -- PLO8: Tự học
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO8',
            'Tự học và cập nhật kiến thức công nghệ mới thông qua tài liệu và cộng đồng',
            'Attitude',
            NOW(), NOW()
        );
        
        -- PLO9: Đạo đức nghề nghiệp
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO9',
            'Thực hành đạo đức nghề nghiệp, trách nhiệm xã hội và tuân thủ pháp luật',
            'Attitude',
            NOW(), NOW()
        );
        
        -- PLO10: Quản lý dự án
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO10',
            'Quản lý thời gian, nguồn lực và rủi ro trong các dự án phần mềm',
            'Skills',
            NOW(), NOW()
        );
        
        -- PLO11: Nghiên cứu
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO11',
            'Nghiên cứu, đánh giá và áp dụng các công nghệ, phương pháp mới vào thực tiễn',
            'Knowledge',
            NOW(), NOW()
        );
        
        -- PLO12: Tư duy hệ thống
        INSERT INTO plos (id, curriculum_id, code, description, category, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            curr_rec.id,
            'PLO12',
            'Tư duy hệ thống, nhìn nhận vấn đề toàn diện và đưa ra quyết định hợp lý',
            'Knowledge',
            NOW(), NOW()
        );
    END LOOP;
END $$;

-- =====================================================
-- 3. Create CLOs cho từng syllabus (5 CLOs/syllabus)
-- =====================================================

-- Xóa CLOs cũ nếu có
DELETE FROM clos WHERE syllabus_version_id IN (SELECT id FROM syllabus_versions);

-- Tạo 5 CLOs cho mỗi syllabus theo Bloom's Taxonomy
DO $$
DECLARE
    syllabus_rec RECORD;
    subject_name TEXT;
BEGIN
    FOR syllabus_rec IN 
        SELECT sv.id as syllabus_id, s.current_name_vi, s.code
        FROM syllabus_versions sv
        JOIN subjects s ON sv.subject_id = s.id
    LOOP
        subject_name := syllabus_rec.current_name_vi;
        
        -- CLO1 - Remember/Understand
        INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO1',
            'Trình bày được các khái niệm cơ bản, định nghĩa và nguyên lý nền tảng của ' || subject_name,
            'Understand',
            15.00,
            NOW(), NOW()
        );
        
        -- CLO2 - Understand/Apply
        INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO2',
            'Giải thích được cách thức hoạt động và mối quan hệ giữa các thành phần trong ' || subject_name,
            'Understand',
            20.00,
            NOW(), NOW()
        );
        
        -- CLO3 - Apply
        INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO3',
            'Áp dụng kiến thức ' || subject_name || ' để giải quyết các bài toán và tình huống thực tế',
            'Apply',
            25.00,
            NOW(), NOW()
        );
        
        -- CLO4 - Analyze
        INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO4',
            'Phân tích và đánh giá các giải pháp kỹ thuật, so sánh ưu nhược điểm trong lĩnh vực ' || subject_name,
            'Analyze',
            20.00,
            NOW(), NOW()
        );
        
        -- CLO5 - Create/Evaluate
        INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
        VALUES (
            gen_random_uuid(), 
            syllabus_rec.syllabus_id, 
            'CLO5',
            'Thiết kế và xây dựng giải pháp hoàn chỉnh, đánh giá hiệu quả ứng dụng kiến thức ' || subject_name,
            'Create',
            20.00,
            NOW(), NOW()
        );
    END LOOP;
END $$;

-- =====================================================
-- 4. Create CLO-PLO Mappings
-- =====================================================

-- Xóa mappings cũ
DELETE FROM clo_plo_mappings;

-- Tạo mappings với logic chi tiết
DO $$
DECLARE
    clo_rec RECORD;
    plo_rec RECORD;
    mapping_level TEXT;
BEGIN
    FOR clo_rec IN 
        SELECT c.id as clo_id, c.code as clo_code, c.bloom_level, c.syllabus_version_id, 
               s.curriculum_id
        FROM clos c
        JOIN syllabus_versions sv ON c.syllabus_version_id = sv.id
        JOIN subjects s ON sv.subject_id = s.id
    LOOP
        FOR plo_rec IN 
            SELECT id as plo_id, code as plo_code
            FROM plos
            WHERE curriculum_id = clo_rec.curriculum_id
        LOOP
            -- Logic mapping dựa trên CLO code và PLO code
            mapping_level := CASE 
                -- CLO1 (Understand) maps strongly to knowledge PLOs
                WHEN clo_rec.clo_code = 'CLO1' AND plo_rec.plo_code IN ('PLO1', 'PLO2') THEN 'H'
                WHEN clo_rec.clo_code = 'CLO1' AND plo_rec.plo_code = 'PLO8' THEN 'M'
                
                -- CLO2 (Understand/Apply) maps to knowledge and some skills
                WHEN clo_rec.clo_code = 'CLO2' AND plo_rec.plo_code IN ('PLO2', 'PLO3') THEN 'H'
                WHEN clo_rec.clo_code = 'CLO2' AND plo_rec.plo_code IN ('PLO1', 'PLO5') THEN 'M'
                
                -- CLO3 (Apply) maps strongly to skills PLOs
                WHEN clo_rec.clo_code = 'CLO3' AND plo_rec.plo_code IN ('PLO3', 'PLO4', 'PLO5') THEN 'H'
                WHEN clo_rec.clo_code = 'CLO3' AND plo_rec.plo_code IN ('PLO2', 'PLO10') THEN 'M'
                
                -- CLO4 (Analyze) maps to analysis and research
                WHEN clo_rec.clo_code = 'CLO4' AND plo_rec.plo_code IN ('PLO3', 'PLO11', 'PLO12') THEN 'H'
                WHEN clo_rec.clo_code = 'CLO4' AND plo_rec.plo_code IN ('PLO4', 'PLO8') THEN 'M'
                
                -- CLO5 (Create) maps to design, teamwork, communication
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code IN ('PLO4', 'PLO5') THEN 'H'
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code IN ('PLO6', 'PLO7', 'PLO10') THEN 'M'
                WHEN clo_rec.clo_code = 'CLO5' AND plo_rec.plo_code = 'PLO9' THEN 'L'
                
                ELSE NULL
            END;
            
            IF mapping_level IS NOT NULL THEN
                INSERT INTO clo_plo_mappings (id, clo_id, plo_id, mapping_level, created_at, updated_at)
                VALUES (gen_random_uuid(), clo_rec.clo_id, plo_rec.plo_id, mapping_level, NOW(), NOW())
                ON CONFLICT DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- Summary
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '=== V16 Migration Summary ===';
    RAISE NOTICE 'Syllabi with objectives: %', (SELECT COUNT(*) FROM syllabus_versions WHERE objectives IS NOT NULL);
    RAISE NOTICE 'PLOs created: %', (SELECT COUNT(*) FROM plos);
    RAISE NOTICE 'CLOs created: %', (SELECT COUNT(*) FROM clos);
    RAISE NOTICE 'CLO-PLO mappings: %', (SELECT COUNT(*) FROM clo_plo_mappings);
END $$;

COMMIT;
