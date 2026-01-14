/*
 * V48__seed_diverse_subject_data.sql
 * Seed dữ liệu đa dạng cho từng môn học
 * Fix: Integer out of range bằng cách ép kiểu BIGINT cho hashtext
 */

SET search_path TO core_service;

DO $$BEGIN RAISE NOTICE '=== Bắt đầu V48: Seeding Dữ liệu Đa Dạng (Fix Overflow) ==='; END$$;

-- ============================================
-- Step 1: Xóa dữ liệu cũ
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 1: Xóa dữ liệu cũ...'; END$$;
DELETE FROM clo_plo_mappings;
DELETE FROM clo_pi_mappings;
DELETE FROM clos;
DELETE FROM plos;

-- ============================================
-- Step 2: Seed PLO đa dạng cho từng subject
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 2: Seeding PLO đa dạng cho từng môn học...'; END$$;

WITH subject_plo_data AS (
    SELECT 
        s.id AS subject_id,
        s.code AS subject_code,
        n.plo_num,
        CASE 
            WHEN s.code LIKE '001%' THEN -- Nhóm Toán học
                CASE n.plo_num
                    WHEN 1 THEN 'Nắm vững các khái niệm cơ bản về đại số tuyến tính và giải tích'
                    WHEN 2 THEN 'Áp dụng phương pháp toán học để giải quyết bài toán thực tế'
                    WHEN 3 THEN 'Phân tích các hàm số và hệ phương trình phức tạp'
                    WHEN 4 THEN 'Đánh giá tính chính xác của các mô hình toán học'
                    WHEN 5 THEN 'Sáng tạo các giải pháp toán học mới cho vấn đề chuyên ngành'
                END
            WHEN s.code LIKE '005%' THEN -- Nhóm Chính trị
                CASE n.plo_num
                    WHEN 1 THEN 'Hiểu rõ tư tưởng Hồ Chí Minh và đường lối Đảng Cộng sản Việt Nam'
                    WHEN 2 THEN 'Áp dụng nguyên tắc chính trị vào phân tích sự kiện xã hội'
                    WHEN 3 THEN 'Phân tích vai trò của chủ nghĩa Marx-Lenin trong xã hội hiện đại'
                    WHEN 4 THEN 'Đánh giá hiệu quả của các chính sách kinh tế - xã hội'
                    WHEN 5 THEN 'Xây dựng quan điểm cá nhân về phát triển bền vững quốc gia'
                END
            WHEN s.code LIKE '010%' OR s.code LIKE '011%' THEN -- Nhóm Toán, Vật lý
                CASE n.plo_num
                    WHEN 1 THEN 'Nắm vững định lý và công thức cốt lõi của toán/vật lý'
                    WHEN 2 THEN 'Thực hành tính toán và thí nghiệm cơ bản'
                    WHEN 3 THEN 'Ứng dụng kiến thức để mô hình hóa hiện tượng tự nhiên'
                    WHEN 4 THEN 'Phân tích dữ liệu thí nghiệm và rút ra kết luận'
                    WHEN 5 THEN 'Thiết kế dự án nghiên cứu toán/vật lý sáng tạo'
                END
            WHEN s.code LIKE '121%' THEN -- Nhóm Lập trình
                CASE n.plo_num
                    WHEN 1 THEN 'Hiểu cấu trúc dữ liệu và thuật toán lập trình'
                    WHEN 2 THEN 'Phát triển ứng dụng web/mobile với các framework hiện đại'
                    WHEN 3 THEN 'Tối ưu hóa code và xử lý lỗi lập trình'
                    WHEN 4 THEN 'Đánh giá chất lượng phần mềm theo tiêu chuẩn'
                    WHEN 5 THEN 'Thiết kế hệ thống lập trình phức tạp và tích hợp'
                END
            WHEN s.code LIKE '122%' THEN -- Nhóm Hệ thống
                CASE n.plo_num
                    WHEN 1 THEN 'Nắm vững kiến trúc hệ thống và mạng máy tính'
                    WHEN 2 THEN 'Xây dựng và bảo trì hệ thống phần mềm'
                    WHEN 3 THEN 'Phân tích yêu cầu và thiết kế phần mềm'
                    WHEN 4 THEN 'Kiểm thử và đánh giá hiệu suất hệ thống'
                    WHEN 5 THEN 'Sáng tạo giải pháp kỹ thuật cho vấn đề thực tế'
                END
            WHEN s.code LIKE '123%' THEN -- Nhóm An ninh, Mạng
                CASE n.plo_num
                    WHEN 1 THEN 'Hiểu các giao thức mạng và an ninh thông tin'
                    WHEN 2 THEN 'Cấu hình và bảo mật hệ thống mạng'
                    WHEN 3 THEN 'Phân tích lỗ hổng và tấn công mạng'
                    WHEN 4 THEN 'Đánh giá rủi ro và lập kế hoạch bảo vệ'
                    WHEN 5 THEN 'Phát triển công cụ an ninh mạng mới'
                END
            ELSE -- Generic
                CASE n.plo_num
                    WHEN 1 THEN 'Nắm vững kiến thức cơ bản về ' || s.current_name_vi
                    WHEN 2 THEN 'Áp dụng kỹ năng thực hành trong lĩnh vực ' || s.current_name_vi
                    WHEN 3 THEN 'Phân tích vấn đề và giải quyết trong bối cảnh thực tế'
                    WHEN 4 THEN 'Đánh giá hiệu quả và cải thiện phương pháp'
                    WHEN 5 THEN 'Sáng tạo ý tưởng mới liên quan đến ' || s.current_name_vi
                END
        END AS plo_desc,
        CASE n.plo_num 
            WHEN 1 THEN 'Knowledge'::plo_category
            WHEN 2 THEN 'Knowledge'::plo_category
            WHEN 3 THEN 'Skills'::plo_category
            WHEN 4 THEN 'Competence'::plo_category
            WHEN 5 THEN 'Competence'::plo_category
        END AS category_type
    FROM subjects s
    CROSS JOIN (SELECT 1 AS plo_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) n
)
INSERT INTO plos (id, code, description, category, subject_id, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'PLO' || plo_num::TEXT,
    plo_desc,
    category_type,
    subject_id,
    NOW(),
    NOW()
FROM subject_plo_data;

-- ============================================
-- Step 3: Seed CLO đa dạng cho từng SyllabusVersion
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 3: Seeding CLO đa dạng cho từng phiên bản chương trình...'; END$$;

WITH subject_clo_data AS (
    SELECT 
        sv.id AS sv_id,
        s.code AS subject_code,
        n.clo_num,
        CASE 
            WHEN s.code LIKE '001%' THEN -- Toán học
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ và liệt kê các định nghĩa toán học cơ bản'
                    WHEN 2 THEN 'Hiểu và giải thích các định lý toán học'
                    WHEN 3 THEN 'Áp dụng công thức để giải bài toán cụ thể'
                    WHEN 4 THEN 'Phân tích và chứng minh các vấn đề toán học'
                    WHEN 5 THEN 'Đánh giá tính đúng đắn của giải pháp toán học'
                    WHEN 6 THEN 'Sáng tạo mô hình toán học mới cho vấn đề thực tế'
                END
            WHEN s.code LIKE '005%' THEN -- Chính trị
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ các mốc lịch sử chính trị quan trọng'
                    WHEN 2 THEN 'Hiểu tư tưởng và đường lối chính trị'
                    WHEN 3 THEN 'Áp dụng lý thuyết chính trị vào phân tích sự kiện'
                    WHEN 4 THEN 'Phân tích tác động của chính sách xã hội'
                    WHEN 5 THEN 'Đánh giá hiệu quả của hệ thống chính trị'
                    WHEN 6 THEN 'Xây dựng quan điểm cá nhân về phát triển quốc gia'
                END
            WHEN s.code LIKE '010%' OR s.code LIKE '011%' THEN -- Toán, Vật lý
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ công thức và định luật cốt lõi'
                    WHEN 2 THEN 'Hiểu nguyên lý và ứng dụng thực tế'
                    WHEN 3 THEN 'Thực hành tính toán và thí nghiệm'
                    WHEN 4 THEN 'Phân tích dữ liệu và kết quả'
                    WHEN 5 THEN 'Đánh giá tính khả thi của mô hình'
                    WHEN 6 THEN 'Thiết kế dự án nghiên cứu mới'
                END
            WHEN s.code LIKE '121%' THEN -- Lập trình
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ cú pháp và lệnh lập trình cơ bản'
                    WHEN 2 THEN 'Hiểu cấu trúc dữ liệu và thuật toán'
                    WHEN 3 THEN 'Viết code để giải quyết vấn đề đơn giản'
                    WHEN 4 THEN 'Phân tích và tối ưu hóa code'
                    WHEN 5 THEN 'Đánh giá chất lượng phần mềm'
                    WHEN 6 THEN 'Sáng tạo ứng dụng lập trình phức tạp'
                END
            WHEN s.code LIKE '122%' THEN -- Hệ thống
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ kiến trúc hệ thống cơ bản'
                    WHEN 2 THEN 'Hiểu cách vận hành hệ thống'
                    WHEN 3 THEN 'Xây dựng và cấu hình hệ thống'
                    WHEN 4 THEN 'Phân tích lỗi và bảo trì'
                    WHEN 5 THEN 'Đánh giá hiệu suất hệ thống'
                    WHEN 6 THEN 'Thiết kế hệ thống mới'
                END
            WHEN s.code LIKE '123%' THEN -- An ninh, Mạng
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ giao thức và công cụ an ninh'
                    WHEN 2 THEN 'Hiểu lỗ hổng và rủi ro mạng'
                    WHEN 3 THEN 'Cấu hình bảo mật hệ thống'
                    WHEN 4 THEN 'Phân tích tấn công và phòng thủ'
                    WHEN 5 THEN 'Đánh giá mức độ an toàn'
                    WHEN 6 THEN 'Phát triển công cụ an ninh mới'
                END
            ELSE -- Generic
                CASE n.clo_num
                    WHEN 1 THEN 'Nhớ kiến thức cơ bản của môn học'
                    WHEN 2 THEN 'Hiểu nguyên tắc và ứng dụng'
                    WHEN 3 THEN 'Áp dụng vào thực hành'
                    WHEN 4 THEN 'Phân tích vấn đề sâu sắc'
                    WHEN 5 THEN 'Đánh giá và cải thiện'
                    WHEN 6 THEN 'Sáng tạo giải pháp mới'
                END
        END AS clo_desc,
        CASE n.clo_num 
            WHEN 1 THEN 'REMEMBER'
            WHEN 2 THEN 'UNDERSTAND'
            WHEN 3 THEN 'APPLY'
            WHEN 4 THEN 'ANALYZE'
            WHEN 5 THEN 'EVALUATE'
            ELSE 'CREATE'
        END AS bloom_level_val,
        CASE n.clo_num 
            WHEN 1 THEN 10
            WHEN 2 THEN 15
            WHEN 3 THEN 20
            WHEN 4 THEN 20
            WHEN 5 THEN 20
            ELSE 15
        END AS weight_val
    FROM syllabus_versions sv
    JOIN subjects s ON sv.subject_id = s.id
    CROSS JOIN (SELECT 1 AS clo_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) n
)
INSERT INTO clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    sv_id,
    'CLO' || clo_num::TEXT,
    clo_desc,
    bloom_level_val,
    weight_val,
    NOW(),
    NOW()
FROM subject_clo_data;

-- ============================================
-- Step 4: Seed CLO-PLO Mappings (Tạo ma trận khác biệt cho từng môn)
-- ============================================
DO $$BEGIN RAISE NOTICE 'Bước 4: Tạo ma trận CLO-PLO ngẫu nhiên theo từng môn...'; END$$;

INSERT INTO clo_plo_mappings (id, clo_id, plo_id, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    c.id,
    p.id,
    NOW(),
    NOW()
FROM clos c
JOIN syllabus_versions sv ON sv.id = c.syllabus_version_id
JOIN subjects s ON sv.subject_id = s.id
JOIN plos p ON p.subject_id = s.id
WHERE 
    CASE 
        -- Nhóm 1: Toán (Map đường chéo)
        WHEN s.code LIKE '001%' OR s.code LIKE '010%' OR s.code LIKE '011%' THEN 
            CAST(SUBSTRING(c.code FROM 4) AS INT) = CAST(SUBSTRING(p.code FROM 4) AS INT)
            OR CAST(SUBSTRING(c.code FROM 4) AS INT) = CAST(SUBSTRING(p.code FROM 4) AS INT) + 1

        -- Nhóm 2: Chính trị (Map diện rộng)
        WHEN s.code LIKE '005%' OR s.code LIKE '122%' THEN 
            CAST(SUBSTRING(p.code FROM 4) AS INT) <= CAST(SUBSTRING(c.code FROM 4) AS INT) + 1
            AND (CAST(SUBSTRING(c.code FROM 4) AS INT) + CAST(SUBSTRING(p.code FROM 4) AS INT)) % 2 = 0 

        -- Nhóm 3: Lập trình - FIX OVERFLOW Ở ĐÂY
        -- Ép kiểu hashtext(...) thành BIGINT để tránh tràn số Integer
        WHEN s.code LIKE '121%' THEN 
            (hashtext(s.id::text)::BIGINT + CAST(SUBSTRING(c.code FROM 4) AS INT) * CAST(SUBSTRING(p.code FROM 4) AS INT)) % 3 = 0

        -- Nhóm 4: Mạng - FIX OVERFLOW Ở ĐÂY
        WHEN s.code LIKE '123%' THEN 
             CAST(SUBSTRING(p.code FROM 4) AS INT) >= 3 
             AND (hashtext(c.code)::BIGINT + hashtext(p.code)::BIGINT) % 2 = 0

        -- Nhóm 5: Generic - FIX OVERFLOW Ở ĐÂY
        ELSE 
            (hashtext(concat(c.id, p.id))::BIGINT % 100) < 30 
    END;

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
    
    RAISE NOTICE '=== Kết quả Migration V48 ===';
    RAISE NOTICE 'Tổng số môn học: %', v_total_subjects;
    RAISE NOTICE 'Tổng số PLOs: % (TB %/môn)', v_total_plos, v_avg_plo_per_subject;
    RAISE NOTICE 'Tổng số CLOs: % (TB %/môn)', v_total_clos, v_avg_clo_per_subject;
    RAISE NOTICE 'Tổng số Mapping: %', v_total_mappings;
    RAISE NOTICE '=== Hoàn thành ===';
END$$;

COMMIT;