/*
 * V9__seed_full_curriculum.sql
 * Import TOÀN BỘ môn học (Full Curriculum Seed)
 * Updated: 
 * - Fix trùng Code giữa Khoa và Bộ môn (thêm prefix BM_).
 * - Thêm log debug số lượng mapping.
 * - Đánh dấu TODO cho logic prerequisite nâng cao.
 */

BEGIN;

-- Chuyển ngữ cảnh làm việc vào schema dự án và public để dùng UUID
SET search_path TO core_service, public;

-- Log start
DO $$BEGIN RAISE NOTICE 'Starting V9 Migration: Seeding Full Curriculum...'; END$$;

-- ==========================================
-- 1. TẠO CẤU TRÚC KHOA & BỘ MÔN
-- ==========================================

-- 1.1. Faculties
INSERT INTO faculties (code, name) VALUES 
('FIT', 'Khoa Công nghệ Thông tin'),
('LLCT', 'Khoa Lý luận Chính trị'),
('KHCB', 'Khoa Khoa học Cơ bản'),
('GDTC', 'Trung tâm Giáo dục Thể chất'),
('GDQP', 'Trung tâm Giáo dục Quốc phòng')
ON CONFLICT (code) DO NOTHING;

-- 1.2. Departments
-- Sửa lỗi trùng mã bằng cách thêm tiền tố BM_ hoặc TT_
WITH 
    f_fit AS (SELECT id FROM faculties WHERE code = 'FIT'),
    f_llct AS (SELECT id FROM faculties WHERE code = 'LLCT'),
    f_khcb AS (SELECT id FROM faculties WHERE code = 'KHCB'),
    f_gdtc AS (SELECT id FROM faculties WHERE code = 'GDTC'),
    f_gdqp AS (SELECT id FROM faculties WHERE code = 'GDQP')

INSERT INTO departments (code, name, faculty_id) VALUES 
('KHMT', 'Khoa học Máy tính', (SELECT id FROM f_fit)),
('KTPM', 'Kỹ thuật Phần mềm', (SELECT id FROM f_fit)),
('HTTT', 'Hệ thống Thông tin', (SELECT id FROM f_fit)),
('MMT', 'Mạng máy tính & Truyền thông', (SELECT id FROM f_fit)),
('TOAN', 'Toán học', (SELECT id FROM f_khcb)),
('BM_LLCT', 'Lý luận Chính trị', (SELECT id FROM f_llct)), 
('BM_LUAT', 'Pháp luật', (SELECT id FROM f_llct)), 
('TT_GDTC', 'Giáo dục Thể chất', (SELECT id FROM f_gdtc)),
('TT_GDQP', 'Giáo dục Quốc phòng', (SELECT id FROM f_gdqp))
ON CONFLICT (code) DO NOTHING;

-- ==========================================
-- 2. INSERT MÔN HỌC (SUBJECTS)
-- ==========================================

WITH 
    d_khmt AS (SELECT id FROM departments WHERE code = 'KHMT'),
    d_ktpm AS (SELECT id FROM departments WHERE code = 'KTPM'),
    d_httt AS (SELECT id FROM departments WHERE code = 'HTTT'),
    d_mmt  AS (SELECT id FROM departments WHERE code = 'MMT'),
    d_toan AS (SELECT id FROM departments WHERE code = 'TOAN'),
    d_llct AS (SELECT id FROM departments WHERE code = 'BM_LLCT'),
    d_luat AS (SELECT id FROM departments WHERE code = 'BM_LUAT'),
    d_tc   AS (SELECT id FROM departments WHERE code = 'TT_GDTC'),
    d_qp   AS (SELECT id FROM departments WHERE code = 'TT_GDQP')

INSERT INTO subjects (code, current_name_vi, default_credits, department_id, description) VALUES 

-- === 2.1. ĐẠI CƯƠNG ===
('001201', 'Đại số', 2, (SELECT id FROM d_toan), 'Ma trận, định thức, hệ phương trình tuyến tính.'),
('001202', 'Giải tích 1', 3, (SELECT id FROM d_toan), 'Giới hạn, đạo hàm, tích phân.'),
('001205', 'Toán chuyên đề 1', 3, (SELECT id FROM d_toan), 'Xác suất thống kê, kiểm định giả thiết.'),
('122042', 'Nhập môn ngành CNTT', 3, (SELECT id FROM d_khmt), 'Định hướng nghề nghiệp, nhập môn Python.'),
('005105', 'Triết học Mác – Lênin', 3, (SELECT id FROM d_llct), 'Theo quy định của Bộ Giáo dục.'),
('005106', 'Kinh tế chính trị Mác – Lênin', 2, (SELECT id FROM d_llct), 'Theo quy định của Bộ Giáo dục.'),
('005107', 'Chủ nghĩa xã hội khoa học', 2, (SELECT id FROM d_llct), 'Theo quy định của Bộ Giáo dục.'),
('005102', 'Tư tưởng Hồ Chí Minh', 2, (SELECT id FROM d_llct), 'Theo quy định của Bộ Giáo dục.'),
('005108', 'Lịch sử Đảng cộng sản VN', 2, (SELECT id FROM d_llct), 'Theo quy định của Bộ Giáo dục.'),
('005004', 'Pháp luật đại cương', 2, (SELECT id FROM d_luat), 'Kiến thức cơ bản về nhà nước và pháp luật.'),
('124012', 'Tin học cơ bản', 2, (SELECT id FROM d_khmt), 'Kỹ năng tin học văn phòng.'),

-- === 2.2. CƠ SỞ NGÀNH ===
('121000', 'Cơ sở dữ liệu', 3, (SELECT id FROM d_httt), 'SQL, Đại số quan hệ.'),
('121037', 'Quản trị doanh nghiệp CNTT', 2, (SELECT id FROM d_httt), 'Tổng quan quản trị DN.'),
('122002', 'Toán rời rạc', 2, (SELECT id FROM d_khmt), 'Logic, Tập hợp, Đếm.'),
('122003', 'Lập trình hướng đối tượng', 3, (SELECT id FROM d_ktpm), 'OOP, Java/C++.'),
('122004', 'Lý thuyết đồ thị', 2, (SELECT id FROM d_khmt), 'Đường đi ngắn nhất, Cây khung.'),
('123002', 'Mạng máy tính', 3, (SELECT id FROM d_mmt), 'OSI, TCP/IP.'),
('124001', 'Kỹ thuật lập trình', 3, (SELECT id FROM d_ktpm), 'C/C++, Con trỏ, Đệ quy.'),
('124002', 'Cấu trúc dữ liệu và giải thuật', 3, (SELECT id FROM d_ktpm), 'Stack, Queue, Sort, Search.'),
('125000', 'Kiến trúc máy tính', 3, (SELECT id FROM d_mmt), 'Assembly, CPU, Memory.'),
('125001', 'Hệ điều hành', 3, (SELECT id FROM d_mmt), 'Process, Thread, Deadlock.'),

-- === 2.3. CHUYÊN NGÀNH ===
('121002', 'Thiết kế cơ sở dữ liệu', 3, (SELECT id FROM d_httt), 'Normal Forms, SP, Trigger.'),
('121008', 'Phân tích thiết kế hệ thống', 3, (SELECT id FROM d_httt), 'UML, OOAD.'),
('122005', 'Công nghệ phần mềm', 2, (SELECT id FROM d_ktpm), 'SDLC, Agile, Scrum.'),
('122038', 'Chuyên đề HTGT thông minh', 3, (SELECT id FROM d_httt), 'ITS, IoT.'),
('123013', 'Lập trình mạng', 3, (SELECT id FROM d_mmt), 'Socket, Multithreading.'),
('123033', 'An toàn thông tin', 3, (SELECT id FROM d_mmt), 'Crypto, Security.'),
('124003', 'Phân tích thiết kế giải thuật', 3, (SELECT id FROM d_khmt), 'Độ phức tạp thuật toán.'),
('126000', 'Thực tập tốt nghiệp', 3, (SELECT id FROM d_ktpm), 'Thực tập doanh nghiệp.'),

-- === 2.4. TỰ CHỌN & CHUYÊN SÂU ===
('001210', 'Tối ưu hóa', 2, (SELECT id FROM d_toan), 'Quy hoạch tuyến tính.'),
('121031', 'Lập trình Web', 3, (SELECT id FROM d_ktpm), 'HTML/CSS/JS, PHP/NodeJS.'),
('121034', 'Lập trình Mobile', 3, (SELECT id FROM d_ktpm), 'Android/iOS.'),
('122036', 'Lập trình Java', 2, (SELECT id FROM d_ktpm), 'Java Advanced.'),
('122039', 'Đồ án thực tế CNPM', 3, (SELECT id FROM d_ktpm), 'Capstone Project.'),
('126001', 'Luận văn tốt nghiệp', 6, (SELECT id FROM d_ktpm), 'Thesis.'),
('121033', 'Trí tuệ nhân tạo', 3, (SELECT id FROM d_khmt), 'AI/ML Basic.'),

-- === 2.5. MÔN ĐIỀU KIỆN ===
('007101', 'Đường lối quân sự', 2, (SELECT id FROM d_qp), 'GDQP 1'),
('007102', 'Công tác QP an ninh', 2, (SELECT id FROM d_qp), 'GDQP 2'),
('007103', 'Quân sự chung', 4, (SELECT id FROM d_qp), 'GDQP 3'),
('007104', 'Quân binh chủng', 1, (SELECT id FROM d_qp), 'GDQP 4'),
('004101', 'Lý thuyết GDTC', 1, (SELECT id FROM d_tc), 'GDTC 1'),
('004103', 'Bơi lội', 1, (SELECT id FROM d_tc), 'GDTC Tự chọn'),
('004107', 'Bóng đá', 1, (SELECT id FROM d_tc), 'GDTC Tự chọn')

ON CONFLICT (code) DO UPDATE 
SET current_name_vi = EXCLUDED.current_name_vi,
    default_credits = EXCLUDED.default_credits,
    description = EXCLUDED.description,
    department_id = EXCLUDED.department_id;

-- ==========================================
-- 3. PREREQUISITES (MAPPING)
-- ==========================================
DO $$
DECLARE
    rec RECORD;
    v_count INT := 0;
BEGIN
    FOR rec IN SELECT * FROM (VALUES 
        -- Chính trị (Soft chain)
        ('005106', '005105'), ('005107', '005106'), ('005102', '005107'), ('005108', '005102'),
        -- Chuyên ngành (Hard chain)
        ('122003', '124001'), ('122004', '124001'), ('122004', '122002'), ('122004', '124002'),
        ('124002', '124001'), ('125001', '125000'), ('121002', '121000'), ('121002', '122002'),
        ('121002', '124001'), ('121008', '121000'), ('121008', '121002'), ('122038', '124001'),
        ('122038', '121008'), ('123013', '123002'), ('123013', '124001'), ('124003', '124002'),
        ('121031', '121000'), ('121031', '124001'), ('121034', '122003'), ('121034', '124001'),
        ('122036', '122003'), ('122039', '122005')
    ) AS mapping(subject_code, prereq_code)
    LOOP
        INSERT INTO subject_relationships (subject_id, related_subject_id, type)
        SELECT s1.id, s2.id, 'PREREQUISITE'
        FROM subjects s1, subjects s2
        WHERE s1.code = rec.subject_code AND s2.code = rec.prereq_code
        ON CONFLICT DO NOTHING;
        
        v_count := v_count + 1;
    END LOOP;

    RAISE NOTICE 'Seed completed successfully. Processed % prerequisites mappings.', v_count;
END $$;

COMMIT;