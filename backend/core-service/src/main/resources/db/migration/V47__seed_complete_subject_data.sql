/*
 * V47__seed_complete_subject_data.sql
 * Seed dữ liệu đầy đủ cho từng môn học: PLO riêng, CLO riêng, Mapping riêng
 * Mỗi subject sẽ có tập PLO và CLO khác nhau dựa trên bản chất môn học
 */

SET search_path TO core_service;

DO $$BEGIN RAISE NOTICE '=== Bắt đầu V47: Seeding Dữ liệu Môn Học Đầy Đủ ==='; END$$;

-- ============================================
-- Step 1: Xóa dữ liệu cũ
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 1: Xóa dữ liệu cũ...'; END$$;
DELETE FROM clo_plo_mappings;
DELETE FROM clo_pi_mappings;
DELETE FROM plos;
DELETE FROM clos;

-- ============================================
-- Step 2: Seed PLO theo từng môn học cụ thể
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 2: Seeding Mục tiêu đào tạo (PLOs) theo từng môn học...'; END$$;

-- PLO cho Toán rời rạc (122002)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT, 
    CASE n
        WHEN 1 THEN 'Nắm vững các khái niệm cơ bản về lý thuyết tập hợp, logic toán học'
        WHEN 2 THEN 'Hiểu sâu sắc về đồ thị, cây, và các cấu trúc rời rạc khác'
        WHEN 3 THEN 'Áp dụng các kỹ thuật đếm và quy nạp toán học vào bài toán thực tế'
        WHEN 4 THEN 'Phân tích và tối ưu hóa các thuật toán liên quan đến cấu trúc rời rạc'
        WHEN 5 THEN 'Phát triển khả năng tư duy logic chặt chẽ và chứng minh toán học'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '122002')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- PLO cho Lập trình hướng đối tượng (122003)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Hiểu rõ các nguyên lý OOP: tính đóng gói, kế thừa, đa hình'
        WHEN 2 THEN 'Thiết kế và phát triển các hệ thống phần mềm có cấu trúc rõ ràng'
        WHEN 3 THEN 'Vận dụng các mẫu thiết kế (Design Patterns) trong lập trình OOP'
        WHEN 4 THEN 'Tái sử dụng code hiệu quả và quản lý phụ thuộc giữa các lớp'
        WHEN 5 THEN 'Phát triển kỹ năng debug và tối ưu hóa hiệu năng ứng dụng'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '122003')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- PLO cho Cơ sở dữ liệu (121000)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nắm vững mô hình quan hệ và các khái niệm cơ bản của CSDL'
        WHEN 2 THEN 'Thiết kế sơ đồ ER và chuẩn hóa cơ sở dữ liệu'
        WHEN 3 THEN 'Viết câu truy vấn SQL phức tạp và thao tác với dữ liệu hiệu quả'
        WHEN 4 THEN 'Tối ưu hóa hiệu năng truy vấn và quản lý indexes'
        WHEN 5 THEN 'Đảm bảo tính toàn vẹn dữ liệu, bảo mật và khôi phục sau sự cố'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '121000')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- PLO cho Mạng máy tính (123002)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Hiểu các mô hình OSI, TCP/IP và giao thức mạng cơ bản'
        WHEN 2 THEN 'Triển khai và cấu hình các thiết bị mạng: router, switch, firewall'
        WHEN 3 THEN 'Phân tích lưu lượng mạng và xác định vấn đề kết nối'
        WHEN 4 THEN 'Thiết kế kiến trúc mạng an toàn và hiệu suất cao'
        WHEN 5 THEN 'Phát triển kỹ năng quản lý và bảo trì hệ thống mạng'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '123002')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- PLO cho Lập trình Web (121031)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nắm vững công nghệ HTML, CSS, JavaScript và framework frontend'
        WHEN 2 THEN 'Thiết kế giao diện người dùng responsive và trực quan'
        WHEN 3 THEN 'Phát triển web ứng dụng tương tác với backend API'
        WHEN 4 THEN 'Tối ưu hóa tốc độ tải và trải nghiệm người dùng'
        WHEN 5 THEN 'Đảm bảo bảo mật web application và xử lý dữ liệu an toàn'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '121031')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- PLO cho Đại số (001201)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nắm vững các phép toán đại số và giải phương trình'
        WHEN 2 THEN 'Hiểu sâu về không gian vectơ, ma trận và hệ phương trình tuyến tính'
        WHEN 3 THEN 'Vận dụng các kỹ thuật đại số vào các bài toán thực tế'
        WHEN 4 THEN 'Phân tích cấu trúc đại số và chứng minh định lý'
        WHEN 5 THEN 'Phát triển tư duy trừu tượng và khả năng chứng minh toán học'
    END,
    NOW(), NOW(),
    (SELECT id FROM subjects WHERE code = '001201')
FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums;

-- Seed dữ liệu cho các môn học còn lại (Generic)
INSERT INTO plos (id, code, description, created_at, updated_at, subject_id)
SELECT gen_random_uuid(), 'PLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nắm vững kiến thức cơ bản của ' || s.current_name_vi
        WHEN 2 THEN 'Hiểu nguyên lý hoạt động và ứng dụng thực tế của ' || s.current_name_vi
        WHEN 3 THEN 'Vận dụng kiến thức để giải quyết các bài toán chuyên ngành'
        WHEN 4 THEN 'Phân tích, đánh giá và cải tiến các phương pháp hiện có'
        WHEN 5 THEN 'Phát triển kỹ năng tự học và làm việc độc lập, cùng tập thể'
    END,
    NOW(), NOW(),
    s.id
FROM subjects s,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE NOT EXISTS (SELECT 1 FROM plos WHERE subject_id = s.id)
  AND s.code NOT IN ('122002', '122003', '121000', '123002', '121031', '001201');

-- ============================================
-- Step 3: Seed CLO theo từng môn học cụ thể
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 3: Seeding Kết quả học tập (CLOs) theo từng phiên bản chương trình...'; END$$;

-- CLO cho môn Toán rời rạc (122002) - Tập trung vào Logic & Toán học ứng dụng
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Liệt kê các khái niệm tập hợp, logic mệnh đề, và các phép toán cơ bản'
        WHEN 2 THEN 'Giải thích các cấu trúc rời rạc: đồ thị, cây và các tính chất của chúng'
        WHEN 3 THEN 'Áp dụng các kỹ thuật đếm, quy nạp toán học để giải bài toán cụ thể'
        WHEN 4 THEN 'Đánh giá hiệu quả của các thuật toán đồ thị khác nhau'
        WHEN 5 THEN 'Thiết kế những thuật toán mới cho các vấn đề tối ưu trên đồ thị'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'EVALUATE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '122002'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO cho môn Lập trình hướng đối tượng (122003) - Tập trung vào Design & Coding
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nhớ lại các từ khóa, cú pháp OOP và quy ước đặt tên trong Java/C++'
        WHEN 2 THEN 'Diễn giải các khái niệm tính đóng gói, kế thừa, và đa hình'
        WHEN 3 THEN 'Viết code OOP đơn giản sử dụng lớp, đối tượng, và phương thức'
        WHEN 4 THEN 'Phân tích và so sánh các thiết kế OOP khác nhau'
        WHEN 5 THEN 'Xây dựng ứng dụng OOP phức tạp sử dụng Design Patterns'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '122003'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO cho môn Cơ sở dữ liệu (121000) - Tập trung vào Thiết kế & SQL
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Định nghĩa các khái niệm: quan hệ, thuộc tính, khóa chính/ngoài'
        WHEN 2 THEN 'Hiểu các dạng chuẩn hóa CSDL (1NF, 2NF, 3NF, BCNF)'
        WHEN 3 THEN 'Xây dựng sơ đồ ER từ yêu cầu và thiết kế CSDL'
        WHEN 4 THEN 'So sánh và lựa chọn cách thiết kế CSDL tối ưu cho bài toán'
        WHEN 5 THEN 'Tạo hệ thống CSDL phức tạp với trigger, stored procedure, views'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '121000'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO cho môn Mạng máy tính (123002) - Tập trung vào Giao thức & Cấu hình
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Liệt kê các lớp OSI, giao thức TCP/IP và cấu trúc gói dữ liệu'
        WHEN 2 THEN 'Giải thích cách hoạt động của các giao thức DNS, DHCP, HTTP'
        WHEN 3 THEN 'Cấu hình địa chỉ IP, subnet mask, định tuyến cơ bản'
        WHEN 4 THEN 'Phân tích lưu lượng mạng và chẩn đoán sự cố kết nối'
        WHEN 5 THEN 'Thiết kế mô hình mạng an toàn cho doanh nghiệp'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '123002'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO cho môn Lập trình Web (121031) - Tập trung vào Frontend & Backend
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Liệt kê các thẻ HTML, thuộc tính CSS và các hàm JavaScript cơ bản'
        WHEN 2 THEN 'Giải thích mô hình DOM, CSS Box Model, và Event Handling'
        WHEN 3 THEN 'Xây dựng giao diện web responsive sử dụng HTML5, CSS3'
        WHEN 4 THEN 'Đánh giá và tối ưu hóa tốc độ tải, hiệu suất web'
        WHEN 5 THEN 'Tạo ứng dụng web tương tác với API, xử lý dữ liệu phức tạp'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '121031'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO cho môn Đại số (001201) - Tập trung vào Lý thuyết & Tính toán
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nhớ lại định nghĩa, ký hiệu của các phép toán đại số cơ bản'
        WHEN 2 THEN 'Hiểu các tính chất của ma trận, định thức, không gian vectơ'
        WHEN 3 THEN 'Giải hệ phương trình tuyến tính sử dụng Gauss, Cramer'
        WHEN 4 THEN 'So sánh các phương pháp giải và chọn cách tối ưu nhất'
        WHEN 5 THEN 'Chứng minh các định lý, định luật đại số từ tiên đề'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'EVALUATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE s.code = '001201'
  AND NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id);

-- CLO Generic cho các môn học còn lại
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, created_at, updated_at)
SELECT gen_random_uuid(), sv.id, 'CLO' || n::TEXT,
    CASE n
        WHEN 1 THEN 'Nhớ lại các khái niệm cơ bản và thuật ngữ của ' || s.current_name_vi
        WHEN 2 THEN 'Giải thích các nguyên lý, quy tắc và ứng dụng thực tế'
        WHEN 3 THEN 'Vận dụng kiến thức để thực hành và giải quyết các bài tập'
        WHEN 4 THEN 'Phân tích, so sánh các phương pháp khác nhau'
        WHEN 5 THEN 'Tổng hợp và tạo ra những ý tưởng mới trong lĩnh vực'
    END,
    CASE n
        WHEN 1 THEN 'REMEMBER'
        WHEN 2 THEN 'UNDERSTAND'
        WHEN 3 THEN 'APPLY'
        WHEN 4 THEN 'ANALYZE'
        WHEN 5 THEN 'CREATE'
    END,
    NOW(), NOW()
FROM syllabus_versions sv
JOIN subjects s ON sv.subject_id = s.id,
     (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) AS nums
WHERE NOT EXISTS (SELECT 1 FROM clos WHERE syllabus_version_id = sv.id)
  AND s.code NOT IN ('122002', '122003', '121000', '123002', '121031', '001201');

-- ============================================
-- Step 4: Seed CLO-PLO Mappings - Ánh xạ thực sự hợp lý theo nội dung
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 4: Tạo ánh xạ giữa CLOs và PLOs...'; END$$;

INSERT INTO clo_plo_mappings (id, clo_id, plo_id, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    c.id,
    p.id,
    NOW(),
    NOW()
FROM clos c
JOIN syllabus_versions sv ON sv.id = c.syllabus_version_id
JOIN plos p ON p.subject_id = sv.subject_id
WHERE 
    -- CLO1 => PLO1, PLO2
    -- CLO2 => PLO2, PLO3
    -- CLO3 => PLO3, PLO4
    -- etc.
    CAST(SUBSTRING(c.code FROM 4) AS INT) >= CAST(SUBSTRING(p.code FROM 4) AS INT) - 1
    AND CAST(SUBSTRING(c.code FROM 4) AS INT) <= CAST(SUBSTRING(p.code FROM 4) AS INT);

-- ============================================
-- Step 5: Log kết quả
-- ============================================
DO $$
DECLARE 
    v_total_subjects INT;
    v_total_plos INT;
    v_total_clos INT;
    v_total_mappings INT;
    v_avg_plo_per_subject DECIMAL;
    v_avg_clo_per_subject DECIMAL;
BEGIN
    SELECT COUNT(DISTINCT id) INTO v_total_subjects FROM subjects;
    SELECT COUNT(*) INTO v_total_plos FROM plos;
    SELECT COUNT(*) INTO v_total_clos FROM clos;
    SELECT COUNT(*) INTO v_total_mappings FROM clo_plo_mappings;
    
    v_avg_plo_per_subject := ROUND(v_total_plos::DECIMAL / NULLIF(v_total_subjects, 0), 1);
    v_avg_clo_per_subject := ROUND(v_total_clos::DECIMAL / NULLIF(v_total_subjects, 0), 1);
    
    RAISE NOTICE '=== Kết quả Migration V47 ===';
    RAISE NOTICE 'Tổng số môn học: %', v_total_subjects;
    RAISE NOTICE 'Tổng số PLOs (Mục tiêu đào tạo): % (trung bình % trên môn)', v_total_plos, v_avg_plo_per_subject;
    RAISE NOTICE 'Tổng số CLOs (Kết quả học tập): % (trung bình % trên môn)', v_total_clos, v_avg_clo_per_subject;
    RAISE NOTICE 'Tổng số ánh xạ CLO-PLO: %', v_total_mappings;
    RAISE NOTICE '=== Migration V47 hoàn thành thành công ===';
END$$;

COMMIT;