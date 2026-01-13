-- V41__Insert_Real_Data_For_Missing_Subjects.sql
-- Fix: Dựa trên cấu trúc V3 và logic JSONB của V13
-- Loại bỏ các cột không tồn tại (min_avg_grade, is_active, description...)

DO $$
DECLARE
    v_subject_id UUID;
    v_syllabus_id UUID;
    v_admin_id UUID;
    
    -- Cursor: Chỉ lấy các môn chưa có bất kỳ version nào
    missing_subject_cursor CURSOR FOR 
        SELECT s.id, s.code, s.current_name_vi, s.current_name_en, s.default_credits
        FROM core_service.subjects s
        LEFT JOIN core_service.syllabus_versions sv ON s.id = sv.subject_id
        WHERE sv.id IS NULL;
        
    v_record RECORD;
    
    -- Biến nội dung
    v_description TEXT;
    v_objectives TEXT;
    v_tasks TEXT;
    v_json_content JSONB;
    
    -- Biến ID con
    v_assess_id UUID;
    v_clo1_id UUID;
    v_clo2_id UUID;
    
BEGIN
    -- 1. Lấy ID Admin (Lấy user đầu tiên)
    SELECT id INTO v_admin_id FROM core_service.users LIMIT 1;
    
    IF v_admin_id IS NULL THEN
        RAISE NOTICE 'Không tìm thấy User nào. Skip migration.';
        RETURN;
    END IF;

    -- 2. Duyệt qua các môn thiếu đề cương
    OPEN missing_subject_cursor;
    
    LOOP
        FETCH missing_subject_cursor INTO v_record;
        EXIT WHEN NOT FOUND;
        
        v_subject_id := v_record.id;
        v_syllabus_id := gen_random_uuid();
        v_tasks := 'Tham gia đầy đủ các buổi học lý thuyết và thực hành. Hoàn thành các bài tập trên hệ thống LMS.';

        -- LOGIC NỘI DUNG GIẢ LẬP (Để dữ liệu nhìn như thật)
        IF v_record.current_name_vi LIKE '%Bơi%' OR v_record.current_name_vi LIKE '%Bóng%' OR v_record.current_name_vi LIKE '%Thể chất%' THEN
            v_description := 'Môn học trang bị kỹ năng vận động, rèn luyện sức khỏe và tinh thần thể thao thông qua bộ môn ' || v_record.current_name_vi || '.';
            v_objectives := 'Thực hiện đúng kỹ thuật động tác cơ bản và nâng cao. Hiểu luật thi đấu.';
        ELSIF v_record.current_name_vi LIKE '%Triết%' OR v_record.current_name_vi LIKE '%Mác%' OR v_record.current_name_vi LIKE '%Tư tưởng%' THEN
            v_description := 'Trang bị thế giới quan và phương pháp luận khoa học về ' || v_record.current_name_vi || '. Nâng cao bản lĩnh chính trị.';
            v_objectives := 'Vận dụng kiến thức lý luận để phân tích các vấn đề thực tiễn xã hội.';
        ELSIF v_record.current_name_vi LIKE '%Toán%' OR v_record.current_name_vi LIKE '%Đại số%' THEN
            v_description := 'Cung cấp nền tảng toán học logic và tư duy trừu tượng cần thiết cho ngành Công nghệ thông tin.';
            v_objectives := 'Áp dụng công thức và định lý toán học để giải quyết các bài toán kỹ thuật.';
        ELSE
            v_description := 'Môn học cung cấp kiến thức chuyên sâu và kỹ năng thực hành về ' || v_record.current_name_vi || '.';
            v_objectives := 'Nắm vững kiến thức cốt lõi. Vận dụng sáng tạo vào dự án thực tế.';
        END IF;

        -- TẠO JSON CONTENT (Cấu trúc chuẩn theo V13)
        v_json_content := jsonb_build_object(
            'description', v_description,
            'objectives', jsonb_build_array(v_objectives, 'Rèn luyện kỹ năng làm việc nhóm và tự học.'),
            'studentTasks', v_tasks,
            'textbooks', jsonb_build_array(
                jsonb_build_object(
                    'title', 'Giáo trình ' || v_record.current_name_vi,
                    'authors', 'Bộ môn phụ trách',
                    'year', 2024,
                    'type', 'required'
                )
            ),
            'teachingMethods', jsonb_build_array('Thuyết trình', 'Thảo luận nhóm', 'Thực hành'),
            'gradingPolicy', jsonb_build_object(
                'attendance', 10,
                'assignments', 20,
                'midterm', 30,
                'final', 40
            ),
            'courseOutline', jsonb_build_array(
                jsonb_build_object('week', 1, 'topic', 'Giới thiệu môn học', 'content', 'Tổng quan về ' || v_record.current_name_vi),
                jsonb_build_object('week', 2, 'topic', 'Kiến thức nền tảng', 'content', 'Các khái niệm cơ bản'),
                jsonb_build_object('week', 3, 'topic', 'Nội dung chuyên sâu 1', 'content', 'Phân tích và ứng dụng'),
                jsonb_build_object('week', 8, 'topic', 'Tổng kết và Thi', 'content', 'Đánh giá cuối kỳ')
            )
        );

        -- 3. INSERT VÀO SYLLABUS_VERSIONS
        -- Chỉ insert các cột có trong V3
        INSERT INTO core_service.syllabus_versions (
            id, subject_id, version_no, status, 
            snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
            content, -- JSON chứa tất cả thông tin chi tiết
            published_at, 
            created_at, updated_at, created_by
        ) VALUES (
            v_syllabus_id, v_subject_id, '1.0.0', 'PUBLISHED'::core_service.syllabus_status,
            v_record.code, v_record.current_name_vi, COALESCE(v_record.current_name_en, v_record.current_name_vi), v_record.default_credits,
            v_json_content,
            now(), 
            now(), now(), v_admin_id
        );

        -- 4. INSERT ASSESSMENT SCHEMES (Cấu trúc 4 cột điểm chuẩn V13)
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at, created_by)
        VALUES (gen_random_uuid(), v_syllabus_id, 'Chuyên cần', 10.0, now(), now(), v_admin_id);
        
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at, created_by)
        VALUES (gen_random_uuid(), v_syllabus_id, 'Bài tập', 20.0, now(), now(), v_admin_id);
        
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at, created_by)
        VALUES (gen_random_uuid(), v_syllabus_id, 'Giữa kỳ', 30.0, now(), now(), v_admin_id);
        
        -- Lưu ID thi cuối kỳ để map CLO sau này
        v_assess_id := gen_random_uuid();
        INSERT INTO core_service.assessment_schemes (id, syllabus_version_id, name, weight_percent, created_at, updated_at, created_by)
        VALUES (v_assess_id, v_syllabus_id, 'Cuối kỳ', 40.0, now(), now(), v_admin_id);

        -- 5. INSERT CLOS (Chuẩn đầu ra)
        v_clo1_id := gen_random_uuid();
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at, created_by)
        VALUES (v_clo1_id, v_syllabus_id, 'CLO1', 'Hiểu kiến thức cơ bản.', 'Understand', 40.0, now(), now(), v_admin_id);
        
        v_clo2_id := gen_random_uuid();
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight, created_at, updated_at, created_by)
        VALUES (v_clo2_id, v_syllabus_id, 'CLO2', 'Vận dụng giải quyết vấn đề.', 'Apply', 60.0, now(), now(), v_admin_id);

        -- 6. INSERT ASSESSMENT_CLO_MAPPINGS (Để hiện ma trận đánh giá)
        -- Map Thi cuối kỳ với cả 2 CLO
        INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
        VALUES (gen_random_uuid(), v_assess_id, v_clo1_id, now(), now());
        
        INSERT INTO core_service.assessment_clo_mappings (id, assessment_scheme_id, clo_id, created_at, updated_at)
        VALUES (gen_random_uuid(), v_assess_id, v_clo2_id, now(), now());

        RAISE NOTICE 'Success generating data for: %', v_record.current_name_vi;
        
    END LOOP;
    
    CLOSE missing_subject_cursor;
END $$;