-- V18: Seed thêm dữ liệu đề cương với các trạng thái khác nhau cho testing
-- Mục đích: Đảm bảo Admin có thể thấy đủ 7 trạng thái trong trang Quản lý Đề cương
-- Các status cần có: APPROVED, PUBLISHED, REJECTED, REVISION_IN_PROGRESS, PENDING_ADMIN_REPUBLISH, INACTIVE, ARCHIVED

SET search_path TO core_service;

DO $$
DECLARE
    v_subject_id UUID;
    v_lecturer_id UUID;
BEGIN
    -- Lấy lecturer
    SELECT id INTO v_lecturer_id FROM users 
    WHERE email = 'gv1.ktpm@smd.edu.vn' LIMIT 1;
    
    -- Nếu không có, lấy bất kỳ user nào
    IF v_lecturer_id IS NULL THEN
        SELECT id INTO v_lecturer_id FROM users LIMIT 1;
    END IF;
    
    -- Lấy subject
    SELECT id INTO v_subject_id FROM subjects LIMIT 1;

    -- Chỉ seed nếu có đủ dữ liệu cần thiết
    IF v_subject_id IS NOT NULL AND v_lecturer_id IS NOT NULL THEN
        
        -- 1. REJECTED - Đề cương bị từ chối
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'REJECTED' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status, 
                snap_subject_name_vi, snap_subject_code, snap_credit_count, 
                theory_hours, practice_hours, self_study_hours,
                description, objectives,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 1, 'REJECTED',
                'Lập trình Web nâng cao (Bị từ chối)', 'WEB301', 3, 
                30, 15, 45,
                'Đề cương này đã bị từ chối do chưa đạt yêu cầu về CLO', 'Nâng cao kỹ năng lập trình web',
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created REJECTED syllabus';
        END IF;

        -- 2. REVISION_IN_PROGRESS - Đang chỉnh sửa
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'REVISION_IN_PROGRESS' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_name_vi, snap_subject_code, snap_credit_count,
                theory_hours, practice_hours, self_study_hours,
                description, objectives,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 1, 'REVISION_IN_PROGRESS',
                'An toàn thông tin (Đang chỉnh sửa)', 'SEC201', 3,
                30, 15, 45,
                'Đề cương đang trong quá trình chỉnh sửa theo góp ý của Trưởng BM', 'Hiểu và áp dụng các nguyên tắc bảo mật',
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created REVISION_IN_PROGRESS syllabus';
        END IF;

        -- 3. PENDING_ADMIN_REPUBLISH - Chờ Admin duyệt xuất bản lại
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'PENDING_ADMIN_REPUBLISH' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_name_vi, snap_subject_code, snap_credit_count,
                theory_hours, practice_hours, self_study_hours,
                description, objectives,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 1, 'PENDING_ADMIN_REPUBLISH',
                'Trí tuệ nhân tạo (Chờ xuất bản lại)', 'AI301', 4,
                45, 15, 60,
                'Đề cương đã được cập nhật và chờ Admin xuất bản lại', 'Nắm vững các thuật toán AI cơ bản',
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created PENDING_ADMIN_REPUBLISH syllabus';
        END IF;

        -- 4. INACTIVE - Ngưng sử dụng
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'INACTIVE' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_name_vi, snap_subject_code, snap_credit_count,
                theory_hours, practice_hours, self_study_hours,
                description, objectives,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 1, 'INACTIVE',
                'Lập trình Pascal (Ngưng sử dụng)', 'PAS101', 2,
                15, 30, 30,
                'Đề cương này đã ngưng sử dụng do thay đổi chương trình đào tạo', 'Học ngôn ngữ Pascal cơ bản',
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created INACTIVE syllabus';
        END IF;

        -- 5. ARCHIVED - Đã lưu trữ
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'ARCHIVED' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_name_vi, snap_subject_code, snap_credit_count,
                theory_hours, practice_hours, self_study_hours,
                description, objectives,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 1, 'ARCHIVED',
                'Lập trình C++ năm 2020 (Đã lưu trữ)', 'CPP101-2020', 3,
                30, 15, 45,
                'Đề cương cũ đã được lưu trữ - phiên bản mới đã thay thế', 'Nắm vững C++ cơ bản',
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created ARCHIVED syllabus';
        END IF;

        RAISE NOTICE 'V18 seed completed successfully!';
    ELSE
        RAISE NOTICE 'Skipped - missing required data (subject or lecturer)';
    END IF;
END $$;

-- Kiểm tra kết quả
SELECT status, COUNT(*) as count 
FROM syllabus_versions 
GROUP BY status 
ORDER BY status;
