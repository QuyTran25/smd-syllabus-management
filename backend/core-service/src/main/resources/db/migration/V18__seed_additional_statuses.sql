-- V18: Seed thêm dữ liệu đề cương với các trạng thái khác nhau cho testing
-- Mục đích: Đảm bảo Admin có thể thấy đủ 7 trạng thái trong trang Quản lý Đề cương
-- Các status cần có: APPROVED, PUBLISHED, REJECTED, REVISION_IN_PROGRESS, PENDING_ADMIN_REPUBLISH, INACTIVE, ARCHIVED

SET search_path TO core_service;

DO $$
DECLARE
    v_subject_id UUID;
    v_lecturer_id UUID;
    v_syllabus_id UUID;
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
                snap_subject_code, snap_subject_name_vi, snap_credit_count,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 'v1.0', 'REJECTED',
                'WEB301', 'Lập trình Web nâng cao (Bị từ chối)', 3,
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created REJECTED syllabus';
        END IF;

        -- 2. REVISION_IN_PROGRESS - Đang chỉnh sửa
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'REVISION_IN_PROGRESS' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_code, snap_subject_name_vi, snap_credit_count,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 'v1.0', 'REVISION_IN_PROGRESS',
                'SEC201', 'An toàn thông tin (Đang chỉnh sửa)', 3,
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created REVISION_IN_PROGRESS syllabus';
        END IF;

        -- 3. PENDING_ADMIN_REPUBLISH - Chờ Admin duyệt xuất bản lại
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'PENDING_ADMIN_REPUBLISH' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_code, snap_subject_name_vi, snap_credit_count,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 'v1.0', 'PENDING_ADMIN_REPUBLISH',
                'AI301', 'Trí tuệ nhân tạo (Chờ xuất bản lại)', 4,
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created PENDING_ADMIN_REPUBLISH syllabus';
        END IF;

        -- 4. INACTIVE - Ngưng sử dụng
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'INACTIVE' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_code, snap_subject_name_vi, snap_credit_count,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 'v1.0', 'INACTIVE',
                'PAS101', 'Lập trình Pascal (Ngưng sử dụng)', 2,
                v_lecturer_id, NOW(), NOW()
            );
            RAISE NOTICE 'Created INACTIVE syllabus';
        END IF;

        -- 5. ARCHIVED - Đã lưu trữ
        IF NOT EXISTS (SELECT 1 FROM syllabus_versions WHERE status = 'ARCHIVED' LIMIT 1) THEN
            INSERT INTO syllabus_versions (
                id, subject_id, version_no, status,
                snap_subject_code, snap_subject_name_vi, snap_credit_count,
                created_by, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_subject_id, 'v1.0', 'ARCHIVED',
                'CPP101', 'Lập trình C++ năm 2020 (Đã lưu trữ)', 3,
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

