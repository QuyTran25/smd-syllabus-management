-- V21: Fix Vietnamese encoding for all tables (moved to avoid version collisions)

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

-- Delete old CLOs and recreate with proper Vietnamese
DELETE FROM core_service.clo_plo_mappings;
DELETE FROM core_service.assessment_clo_mappings;
DELETE FROM core_service.clos;

-- Insert CLOs for each syllabus with proper Vietnamese
DO $$
DECLARE
    syllabus RECORD;
    subject_name TEXT;
BEGIN
    FOR syllabus IN 
        SELECT sv.id as syllabus_id, s.current_name_vi as subject_name
        FROM core_service.syllabus_versions sv
        JOIN core_service.subjects s ON sv.subject_id = s.id
    LOOP
        subject_name := syllabus.subject_name;
        
        -- CLO1: Remember/Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO1',
            'Trình bày được các khái niệm cơ bản, định nghĩa và nguyên lý nền tảng của ' || subject_name,
            'UNDERSTAND',
            20.00
        );
        
        -- CLO2: Understand
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO2',
            'Giải thích được cách thức hoạt động và mối quan hệ giữa các thành phần trong ' || subject_name,
            'UNDERSTAND',
            15.00
        );
        
        -- CLO3: Apply
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO3',
            'Áp dụng kiến thức ' || subject_name || ' để giải quyết các bài toán thực tế',
            'APPLY',
            25.00
        );
        
        -- CLO4: Analyze
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (
            gen_random_uuid(),
            syllabus.syllabus_id,
            'CLO4',
            'Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực ' || subject_name,
            'ANALYZE',
            20.00
        );
        
        -- CLO5: Create
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
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
