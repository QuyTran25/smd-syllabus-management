/*
 * V17__seed_admin_data.sql
 * Mục tiêu: Seed dữ liệu cho các trang Admin
 * - Tạo bảng semesters nếu chưa tồn tại
 * - Audit logs (lịch sử hoạt động hệ thống)
 * - Student feedback (phản hồi từ sinh viên)
 * - Update syllabus statuses for various states
 */

SET search_path TO core_service;

-- ==========================================
-- 0. CREATE SEMESTERS TABLE IF NOT EXISTS
-- ==========================================

CREATE TABLE IF NOT EXISTS semesters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    semester_number INT NOT NULL, -- 1, 2, 3 (for summer)
    academic_year VARCHAR(20) NOT NULL, -- e.g., "2023-2024"
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes if table was just created
CREATE INDEX IF NOT EXISTS idx_semesters_academic_year ON semesters(academic_year);
CREATE INDEX IF NOT EXISTS idx_semesters_is_active ON semesters(is_active);

-- ==========================================
-- 1. SEED AUDIT LOGS
-- ==========================================

DO $$
DECLARE
    v_admin_id UUID;
    v_principal_id UUID;
    v_hod_ktpm_id UUID;
    v_hod_khmt_id UUID;
    v_aa1_id UUID;
    v_aa2_id UUID;
    v_lecturer1_id UUID;
    v_lecturer2_id UUID;
    v_lecturer3_id UUID;
    v_student1_id UUID;
    v_student2_id UUID;
    v_syllabus_id UUID;
    v_syllabus_id2 UUID;
    v_syllabus_id3 UUID;
BEGIN
    -- Lấy user IDs
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_principal_id FROM users WHERE email = 'principal@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_hod_ktpm_id FROM users WHERE email = 'hod.ktpm@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_hod_khmt_id FROM users WHERE email = 'hod.khmt@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_aa1_id FROM users WHERE email = 'aa1@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_aa2_id FROM users WHERE email = 'aa2@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_lecturer1_id FROM users WHERE email = 'gv1.ktpm@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_lecturer2_id FROM users WHERE email = 'gv2.ktpm@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_lecturer3_id FROM users WHERE email = 'gv1.khmt@smd.edu.vn' LIMIT 1;
    
    -- Lấy student IDs (qua bảng user_roles)
    SELECT u.id INTO v_student1_id FROM users u 
        JOIN user_roles ur ON u.id = ur.user_id 
        JOIN roles r ON ur.role_id = r.id 
        WHERE r.code = 'STUDENT' LIMIT 1;
    SELECT u.id INTO v_student2_id FROM users u 
        JOIN user_roles ur ON u.id = ur.user_id 
        JOIN roles r ON ur.role_id = r.id 
        WHERE r.code = 'STUDENT' OFFSET 1 LIMIT 1;
    
    -- Lấy một số syllabus IDs
    SELECT id INTO v_syllabus_id FROM syllabus_versions WHERE status = 'PUBLISHED' LIMIT 1;
    SELECT id INTO v_syllabus_id2 FROM syllabus_versions WHERE status = 'APPROVED' LIMIT 1;
    SELECT id INTO v_syllabus_id3 FROM syllabus_versions WHERE status = 'PENDING_PRINCIPAL' LIMIT 1;

    -- ==========================================
    -- AUDIT LOGS - Các hoạt động hệ thống
    -- ==========================================
    
    -- 1. Admin login
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', v_admin_id, 'Admin đăng nhập hệ thống', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '7 days');
    
    -- 2. Admin cấu hình học kỳ
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Semester', NULL, 'CREATE', v_admin_id, 'Tạo học kỳ mới: HK1 2025-2026', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '7 days' + INTERVAL '10 minutes');
    
    -- 3. AA tạo môn học
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Subject', NULL, 'CREATE', v_aa1_id, 'Tạo môn học mới: Nhập môn Lập trình (CS101)', 'SUCCESS', '192.168.1.110', NOW() - INTERVAL '6 days');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Subject', NULL, 'CREATE', v_aa1_id, 'Tạo môn học mới: Cấu trúc Dữ liệu (CS201)', 'SUCCESS', '192.168.1.110', NOW() - INTERVAL '6 days' + INTERVAL '5 minutes');
    
    -- 4. AA tạo PLO
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('PLO', NULL, 'CREATE', v_aa2_id, 'Tạo PLO1: Kiến thức nền tảng CNTT', 'SUCCESS', '192.168.1.111', NOW() - INTERVAL '6 days' + INTERVAL '30 minutes');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('PLO', NULL, 'UPDATE', v_aa2_id, 'Cập nhật mô tả PLO5: Năng lực tự học và nghiên cứu', 'SUCCESS', '192.168.1.111', NOW() - INTERVAL '5 days');
    
    -- 5. HoD phân công giảng viên
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('TeachingAssignment', NULL, 'CREATE', v_hod_ktpm_id, 'Phân công GV Nguyễn Văn A soạn đề cương CS101', 'SUCCESS', '192.168.1.105', NOW() - INTERVAL '5 days' + INTERVAL '2 hours');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('TeachingAssignment', NULL, 'CREATE', v_hod_ktpm_id, 'Phân công GV Trần Thị B soạn đề cương CS201', 'SUCCESS', '192.168.1.105', NOW() - INTERVAL '5 days' + INTERVAL '2 hours' + INTERVAL '15 minutes');
    
    -- 6. Lecturer tạo đề cương
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'CREATE', v_lecturer1_id, 'Tạo đề cương mới: Nhập môn Lập trình v1.0', 'SUCCESS', '192.168.1.115', NOW() - INTERVAL '4 days');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('CLO', NULL, 'CREATE', v_lecturer1_id, 'Thêm 5 CLO cho đề cương CS101', 'SUCCESS', '192.168.1.115', NOW() - INTERVAL '4 days' + INTERVAL '30 minutes');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'UPDATE', v_lecturer1_id, 'Cập nhật nội dung đề cương CS101', 'SUCCESS', '192.168.1.115', NOW() - INTERVAL '4 days' + INTERVAL '1 hour');
    
    -- 7. Lecturer submit đề cương
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'SUBMIT', v_lecturer1_id, 'Nộp đề cương CS101 để phê duyệt', 'SUCCESS', '192.168.1.115', NOW() - INTERVAL '3 days' - INTERVAL '12 hours');
    
    -- 8. HoD duyệt
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'APPROVE', v_hod_ktpm_id, 'Trưởng BM duyệt đề cương CS101 - Nhập môn Lập trình', 'SUCCESS', '192.168.1.105', NOW() - INTERVAL '3 days');
    
    -- 9. AA duyệt
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'APPROVE', v_aa1_id, 'Phòng Đào tạo duyệt đề cương CS101', 'SUCCESS', '192.168.1.110', NOW() - INTERVAL '2 days' - INTERVAL '6 hours');
    
    -- 10. Principal duyệt
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'APPROVE', v_principal_id, 'Hiệu trưởng phê duyệt đề cương CS101', 'SUCCESS', '192.168.1.120', NOW() - INTERVAL '2 days');
    
    -- 11. Admin xuất bản
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id, 'PUBLISH', v_admin_id, 'Xuất bản đề cương CS101 - Nhập môn Lập trình', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '1 day' - INTERVAL '12 hours');
    
    -- 12. Thêm một số hoạt động khác
    -- HoD từ chối một đề cương
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id3, 'REJECT', v_hod_khmt_id, 'Từ chối đề cương CS301 - Thiếu CLO ánh xạ PLO', 'SUCCESS', '192.168.1.106', NOW() - INTERVAL '1 day');
    
    -- Lecturer login
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', v_lecturer2_id, 'Giảng viên đăng nhập hệ thống', 'SUCCESS', '192.168.1.125', NOW() - INTERVAL '18 hours');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', v_lecturer3_id, 'Giảng viên đăng nhập hệ thống', 'SUCCESS', '192.168.1.126', NOW() - INTERVAL '16 hours');
    
    -- Principal login
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', v_principal_id, 'Hiệu trưởng đăng nhập hệ thống', 'SUCCESS', '192.168.1.120', NOW() - INTERVAL '12 hours');
    
    -- Batch approval by Principal
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', NULL, 'APPROVE', v_principal_id, 'Phê duyệt hàng loạt 5 đề cương', 'SUCCESS', '192.168.1.120', NOW() - INTERVAL '10 hours');
    
    -- Admin cập nhật cấu hình
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('SystemConfig', NULL, 'UPDATE', v_admin_id, 'Cập nhật thời gian phê duyệt tối đa: HoD=3 ngày, AA=5 ngày', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '8 hours');
    
    -- Export log
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', NULL, 'EXPORT', v_aa1_id, 'Xuất danh sách đề cương ra Excel', 'SUCCESS', '192.168.1.110', NOW() - INTERVAL '6 hours');
    
    -- Thêm các hoạt động gần đây
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', v_admin_id, 'Admin đăng nhập hệ thống', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '4 hours');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('User', NULL, 'CREATE', v_admin_id, 'Tạo tài khoản mới cho giảng viên Lê Văn D', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL '3 hours');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id2, 'SUBMIT', v_lecturer2_id, 'Nộp đề cương CS201 để phê duyệt', 'SUCCESS', '192.168.1.125', NOW() - INTERVAL '2 hours');
    
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('Syllabus', v_syllabus_id2, 'APPROVE', v_hod_ktpm_id, 'Trưởng BM duyệt đề cương CS201', 'SUCCESS', '192.168.1.105', NOW() - INTERVAL '1 hour');
    
    -- Failed login attempt
    INSERT INTO audit_logs (entity_name, entity_id, action, actor_id, description, status, ip_address, created_at)
    VALUES ('System', NULL, 'LOGIN', NULL, 'Đăng nhập thất bại - Sai mật khẩu (user: unknown@smd.edu.vn)', 'FAILED', '192.168.1.200', NOW() - INTERVAL '30 minutes');
    
    RAISE NOTICE 'Inserted audit logs successfully';
END $$;

-- ==========================================
-- 2. SEED STUDENT FEEDBACK (syllabus_error_reports)
-- ==========================================

DO $$
DECLARE
    v_student1_id UUID;
    v_student2_id UUID;
    v_student3_id UUID;
    v_admin_id UUID;
    v_syllabus_published_id UUID;
    v_syllabus_published_id2 UUID;
BEGIN
    -- Lấy admin ID
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    
    -- Lấy student IDs - tạo mới nếu chưa có
    -- Kiểm tra xem đã có student profiles chưa
    SELECT sp.user_id INTO v_student1_id 
    FROM student_profiles sp 
    JOIN users u ON sp.user_id = u.id 
    LIMIT 1;
    
    -- Nếu chưa có student, dùng user có email chứa 'student' hoặc tạo mới
    IF v_student1_id IS NULL THEN
        -- Tạo student users nếu chưa có
        INSERT INTO users (email, full_name, password_hash, is_active, created_at)
        VALUES 
            ('student1@smd.edu.vn', 'Nguyễn Văn Sinh Viên A', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', true, NOW()),
            ('student2@smd.edu.vn', 'Trần Thị Sinh Viên B', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', true, NOW()),
            ('student3@smd.edu.vn', 'Lê Văn Sinh Viên C', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', true, NOW())
        ON CONFLICT (email) DO NOTHING;
        
        SELECT id INTO v_student1_id FROM users WHERE email = 'student1@smd.edu.vn';
        SELECT id INTO v_student2_id FROM users WHERE email = 'student2@smd.edu.vn';
        SELECT id INTO v_student3_id FROM users WHERE email = 'student3@smd.edu.vn';
    ELSE
        -- Lấy các student khác nếu có
        SELECT sp.user_id INTO v_student2_id 
        FROM student_profiles sp 
        JOIN users u ON sp.user_id = u.id 
        OFFSET 1 LIMIT 1;
        
        SELECT sp.user_id INTO v_student3_id 
        FROM student_profiles sp 
        JOIN users u ON sp.user_id = u.id 
        OFFSET 2 LIMIT 1;
    END IF;
    
    -- Lấy syllabus đã published
    SELECT id INTO v_syllabus_published_id FROM syllabus_versions WHERE status = 'PUBLISHED' LIMIT 1;
    SELECT id INTO v_syllabus_published_id2 FROM syllabus_versions WHERE status = 'PUBLISHED' OFFSET 1 LIMIT 1;
    
    -- Nếu không có syllabus published thứ 2, dùng cái đầu
    IF v_syllabus_published_id2 IS NULL THEN
        v_syllabus_published_id2 := v_syllabus_published_id;
    END IF;
    
    -- Chỉ insert nếu có đủ dữ liệu
    IF v_syllabus_published_id IS NOT NULL AND v_student1_id IS NOT NULL THEN
        -- Feedback 1: Lỗi chính tả - PENDING
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status, created_at
        ) VALUES (
            v_syllabus_published_id,
            v_student1_id,
            'ERROR',
            'CLO 1.2 có lỗi chính tả',
            'CLO 1.2 ghi "áp dungj" thay vì "áp dụng". Đề nghị sửa lại cho chính xác.',
            'clo',
            'PENDING',
            NOW() - INTERVAL '2 days'
        );
        
        -- Feedback 2: Lỗi tổng % - IN_REVIEW (đã được admin xem xét)
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status,
            admin_response, responded_by, responded_at, edit_enabled, created_at
        ) VALUES (
            v_syllabus_published_id,
            COALESCE(v_student2_id, v_student1_id),
            'ERROR',
            'Tổng % đánh giá không đúng 100%',
            'Tổng tỷ lệ các phương pháp đánh giá chỉ có 95%, thiếu 5%. Đề nghị kiểm tra lại bảng đánh giá.',
            'assessment_matrix',
            'IN_REVIEW',
            'Đã xác nhận có lỗi. Đang gửi yêu cầu giảng viên chỉnh sửa.',
            v_admin_id,
            NOW() - INTERVAL '12 hours',
            true,
            NOW() - INTERVAL '3 days'
        );
        
        -- Feedback 3: Đề xuất thêm tài liệu - RESOLVED
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status,
            admin_response, responded_by, responded_at, created_at
        ) VALUES (
            v_syllabus_published_id2,
            COALESCE(v_student3_id, v_student1_id),
            'SUGGESTION',
            'Đề xuất thêm tài liệu tham khảo',
            'Đề xuất thêm sách "Introduction to Algorithms" (CLRS) vào danh mục tài liệu tham khảo vì đây là sách kinh điển.',
            'reference',
            'RESOLVED',
            'Đã chuyển đề xuất cho giảng viên xem xét. Giảng viên đã thêm sách vào đề cương.',
            v_admin_id,
            NOW() - INTERVAL '1 day',
            NOW() - INTERVAL '5 days'
        );
        
        -- Feedback 4: Thiếu môn tiên quyết - PENDING
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status, created_at
        ) VALUES (
            v_syllabus_published_id2,
            COALESCE(v_student2_id, v_student1_id),
            'ERROR',
            'Thiếu môn học tiên quyết CS101',
            'Đề cương không liệt kê CS101 là môn tiên quyết, nhưng trong lớp học thầy có yêu cầu kiến thức từ môn này.',
            'subject_info',
            'PENDING',
            NOW() - INTERVAL '1 day'
        );
        
        -- Feedback 5: Câu hỏi về nội dung - RESOLVED
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status,
            admin_response, responded_by, responded_at, created_at
        ) VALUES (
            v_syllabus_published_id,
            v_student1_id,
            'QUESTION',
            'Hỏi về thời lượng thực hành',
            'Xin hỏi 45 tiết thực hành có bao gồm thời gian làm đồ án không ạ?',
            'other',
            'RESOLVED',
            'Theo quy định, 45 tiết thực hành không bao gồm thời gian làm đồ án. Đồ án là hoạt động tự học của sinh viên.',
            v_admin_id,
            NOW() - INTERVAL '6 hours',
            NOW() - INTERVAL '2 days'
        );
        
        -- Feedback 6: Feedback khác - REJECTED
        INSERT INTO syllabus_error_reports (
            syllabus_version_id, user_id, type, title, description, section, status,
            admin_response, responded_by, responded_at, created_at
        ) VALUES (
            v_syllabus_published_id,
            COALESCE(v_student3_id, v_student1_id),
            'OTHER',
            'Yêu cầu thay đổi giờ học',
            'Em muốn đề xuất đổi giờ học từ sáng sang chiều.',
            'other',
            'REJECTED',
            'Xin lỗi, đề xuất này không thuộc phạm vi nội dung đề cương. Vui lòng liên hệ Phòng Đào tạo để được hỗ trợ.',
            v_admin_id,
            NOW() - INTERVAL '4 hours',
            NOW() - INTERVAL '1 day'
        );
        
        RAISE NOTICE 'Inserted student feedbacks successfully';
    ELSE
        RAISE NOTICE 'Skipped student feedbacks - missing required data';
    END IF;
END $$;

-- ==========================================
-- 3. ENSURE SYLLABUS STATUSES FOR ADMIN PAGES
-- ==========================================

-- Đảm bảo có ít nhất 3 đề cương APPROVED cho PublishQueuePage
DO $$
DECLARE
    v_aa_id UUID;
    v_principal_id UUID;
    approved_count INT;
BEGIN
    SELECT id INTO v_aa_id FROM users WHERE email = 'aa1@smd.edu.vn' LIMIT 1;
    SELECT id INTO v_principal_id FROM users WHERE email = 'principal@smd.edu.vn' LIMIT 1;
    
    SELECT COUNT(*) INTO approved_count FROM syllabus_versions WHERE status = 'APPROVED';
    
    -- Nếu chưa đủ 3 đề cương APPROVED, chuyển thêm từ PENDING_PRINCIPAL
    IF approved_count < 3 THEN
        UPDATE syllabus_versions 
        SET 
            status = 'APPROVED',
            principal_approved_at = NOW() - INTERVAL '1 day',
            principal_approved_by = v_principal_id,
            updated_at = NOW()
        WHERE id IN (
            SELECT id FROM syllabus_versions 
            WHERE status = 'PENDING_PRINCIPAL' 
            ORDER BY created_at 
            LIMIT (3 - approved_count)
        );
        
        RAISE NOTICE 'Updated % syllabi to APPROVED status', (3 - approved_count);
    END IF;
END $$;

-- Đảm bảo có ít nhất 3 đề cương PUBLISHED cho PublishedSyllabiPage
DO $$
DECLARE
    v_admin_id UUID;
    published_count INT;
BEGIN
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    
    SELECT COUNT(*) INTO published_count FROM syllabus_versions WHERE status = 'PUBLISHED';
    
    -- Nếu chưa đủ 3 đề cương PUBLISHED, chuyển từ APPROVED
    IF published_count < 3 THEN
        UPDATE syllabus_versions 
        SET 
            status = 'PUBLISHED',
            published_at = NOW() - INTERVAL '12 hours',
            effective_date = CURRENT_DATE,
            updated_at = NOW()
        WHERE id IN (
            SELECT id FROM syllabus_versions 
            WHERE status = 'APPROVED' 
            ORDER BY principal_approved_at NULLS LAST
            LIMIT (3 - published_count)
        );
        
        RAISE NOTICE 'Updated % syllabi to PUBLISHED status', (3 - published_count);
    END IF;
END $$;

-- ==========================================
-- 4. SEED SEMESTERS (Học kỳ)
-- ==========================================

DO $$
DECLARE
    v_admin_id UUID;
    semester_count INT;
BEGIN
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    SELECT COUNT(*) INTO semester_count FROM semesters;
    
    -- Chỉ insert nếu chưa có semester nào
    IF semester_count = 0 THEN
        -- Học kỳ 1 năm 2023-2024 (đã kết thúc)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HK1-2023', 'Học kỳ 1 năm 2023-2024', 1, '2023-2024', '2023-09-01', '2024-01-15', FALSE, v_admin_id);
        
        -- Học kỳ 2 năm 2023-2024 (đã kết thúc)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HK2-2023', 'Học kỳ 2 năm 2023-2024', 2, '2023-2024', '2024-02-01', '2024-06-15', FALSE, v_admin_id);
        
        -- Học kỳ hè 2023-2024 (đã kết thúc)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HKH-2023', 'Học kỳ hè năm 2023-2024', 3, '2023-2024', '2024-06-20', '2024-08-15', FALSE, v_admin_id);
        
        -- Học kỳ 1 năm 2024-2025 (đang diễn ra - ACTIVE)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HK1-2024', 'Học kỳ 1 năm 2024-2025', 1, '2024-2025', '2024-09-01', '2025-01-15', TRUE, v_admin_id);
        
        -- Học kỳ 2 năm 2024-2025 (sắp tới)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HK2-2024', 'Học kỳ 2 năm 2024-2025', 2, '2024-2025', '2025-02-01', '2025-06-15', FALSE, v_admin_id);
        
        -- Học kỳ hè 2024-2025 (sắp tới)
        INSERT INTO semesters (code, name, semester_number, academic_year, start_date, end_date, is_active, created_by)
        VALUES ('HKH-2024', 'Học kỳ hè năm 2024-2025', 3, '2024-2025', '2025-06-20', '2025-08-15', FALSE, v_admin_id);
        
        RAISE NOTICE 'Seeded 6 semesters successfully';
    ELSE
        RAISE NOTICE 'Semesters already exist, skipping seed';
    END IF;
END $$;

-- ==========================================
-- 5. LOG SUMMARY
-- ==========================================

DO $$
DECLARE
    audit_count INT;
    feedback_count INT;
    approved_count INT;
    published_count INT;
    semester_count INT;
BEGIN
    SELECT COUNT(*) INTO audit_count FROM audit_logs;
    SELECT COUNT(*) INTO feedback_count FROM syllabus_error_reports;
    SELECT COUNT(*) INTO approved_count FROM syllabus_versions WHERE status = 'APPROVED';
    SELECT COUNT(*) INTO published_count FROM syllabus_versions WHERE status = 'PUBLISHED';
    SELECT COUNT(*) INTO semester_count FROM semesters;
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'V17 Admin Data Seed completed!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Audit logs: %', audit_count;
    RAISE NOTICE 'Student feedbacks: %', feedback_count;
    RAISE NOTICE 'Semesters: %', semester_count;
    RAISE NOTICE 'APPROVED syllabi: %', approved_count;
    RAISE NOTICE 'PUBLISHED syllabi: %', published_count;
    RAISE NOTICE '================================';
END $$;

COMMIT;
