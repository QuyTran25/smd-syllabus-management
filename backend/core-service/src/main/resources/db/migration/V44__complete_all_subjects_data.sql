-- V42__complete_all_subjects_data.sql
-- Đã xóa BEGIN; và COMMIT; để tránh xung đột với Flyway

SET search_path TO core_service;

DO $$
DECLARE
    v_admin_id UUID;
    v_curriculum_id UUID;
    v_syllabus_id UUID;
    v_subject_record RECORD;
    v_clo_id UUID;
    v_assess_id UUID;
    v_plo_record RECORD;
    v_weight NUMERIC;
    v_description TEXT;
    v_objectives JSONB;
    v_course_outline JSONB;
    v_textbooks JSONB;
    v_json_content JSONB;
    clo_count INT;
    i INT;
    j INT;
    week INT;
    v_clo_ids UUID[];
    v_assess_ids UUID[];
    
    -- [SỬA] Đưa 2 biến mảng này lên đầu, khởi tạo giá trị luôn tại đây
    assess_names TEXT[] := ARRAY['Chuyên cần', 'Bài tập', 'Giữa kỳ', 'Cuối kỳ'];
    assess_weights NUMERIC[] := ARRAY[10, 20, 30, 40];
BEGIN
    -- Lấy admin ID
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    
    -- Lấy curriculum ID (từ V19)
    SELECT id INTO v_curriculum_id FROM curriculums WHERE code = 'CNTT' LIMIT 1;
    
    -- Duyệt tất cả subjects chưa có syllabus_versions đầy đủ
    FOR v_subject_record IN
        SELECT s.id AS subject_id, s.code, s.current_name_vi AS name_vi, s.current_name_en AS name_en, s.default_credits AS credits
        FROM subjects s
        LEFT JOIN syllabus_versions sv ON s.id = sv.subject_id
        LEFT JOIN clos c ON sv.id = c.syllabus_version_id
        WHERE sv.id IS NULL OR c.id IS NULL
    LOOP
        v_syllabus_id := gen_random_uuid();
        
        -- Tạo description varied
        v_description := CASE
            WHEN v_subject_record.name_vi ~* 'toán|math' THEN 'Môn học cung cấp nền tảng toán học cho CNTT, bao gồm đại số tuyến tính và toán rời rạc.'
            WHEN v_subject_record.name_vi ~* 'triết|philosophy' THEN 'Trang bị tư duy logic và phương pháp luận khoa học qua các học thuyết triết học.'
            WHEN v_subject_record.name_vi ~* 'thể chất|physical' THEN 'Rèn luyện sức khỏe và kỹ năng vận động cơ bản qua các bài tập thể dục.'
            ELSE 'Khám phá kiến thức chuyên sâu về ' || v_subject_record.name_vi || ' với ứng dụng thực tiễn.'
        END;
        
        -- Objectives varied
        v_objectives := jsonb_build_array(
            'Nắm vững khái niệm cốt lõi của ' || v_subject_record.name_vi,
            CASE WHEN random() > 0.5 THEN 'Áp dụng kiến thức vào giải quyết vấn đề thực tế.' ELSE 'Phân tích các case study liên quan.' END,
            'Phát triển kỹ năng làm việc nhóm qua dự án.',
            CASE WHEN random() > 0.3 THEN 'Tối ưu hóa hiệu suất và đánh giá kết quả.' ELSE 'Tích hợp công nghệ mới vào lĩnh vực.' END
        );
        
        -- Course outline varied
        v_course_outline := '[]'::jsonb;
        FOR week IN 1..(4 + floor(random()*5)) LOOP
            v_course_outline := v_course_outline || jsonb_build_object(
                'week', week,
                'topic', 'Chủ đề tuần ' || week || ': ' || CASE WHEN week % 2 = 0 THEN 'Nâng cao' ELSE 'Cơ bản' END,
                'content', 'Nội dung chi tiết tuần ' || week,
                'activities', CASE WHEN random() > 0.5 THEN 'Thực hành' ELSE 'Thảo luận' END
            );
        END LOOP;
        
        -- Textbooks varied
        v_textbooks := jsonb_build_array(
            jsonb_build_object('title', 'Giáo trình ' || v_subject_record.name_vi, 'authors', 'Tác giả Việt Nam', 'year', 2023 + floor(random()*2), 'type', 'required'),
            jsonb_build_object('title', 'Advanced ' || v_subject_record.code, 'authors', 'International Author', 'year', 2020 + floor(random()*4), 'type', 'reference')
        );
        
        -- JSON content full
        v_json_content := jsonb_build_object(
            'description', v_description,
            'objectives', v_objectives,
            'teachingMethods', jsonb_build_array('Giảng dạy kết hợp thực hành', 'Dự án nhóm', CASE WHEN random() > 0.5 THEN 'Seminar' ELSE 'Workshop' END),
            'gradingPolicy', jsonb_build_object('attendance', 10, 'assignments', 20 + floor(random()*10), 'midterm', 30, 'final', 40 - floor(random()*5)),
            'textbooks', v_textbooks,
            'prerequisites', jsonb_build_array(jsonb_build_object('code', 'PRE' || floor(random()*100), 'name', 'Môn tiên quyết ' || floor(random()*3), 'type', CASE WHEN random() > 0.5 THEN 'required' ELSE 'recommended' END)),
            'courseOutline', v_course_outline,
            'studentDuties', 'Tham gia học tập đầy đủ, hoàn thành bài tập và dự án đúng hạn.'
        );
        
        -- Insert syllabus_version
        INSERT INTO syllabus_versions (
            id, subject_id, version_no, status, created_by,
            snap_subject_code, snap_subject_name_vi, snap_subject_name_en, snap_credit_count,
            theory_hours, practice_hours, self_study_hours,
            content, published_at, created_at, updated_at
        ) VALUES (
            v_syllabus_id, v_subject_record.subject_id, 'v1.0', 'PUBLISHED'::syllabus_status, v_admin_id,
            v_subject_record.code, v_subject_record.name_vi, COALESCE(v_subject_record.name_en, v_subject_record.name_vi), v_subject_record.credits,
            30 + floor(random()*15), 30 + floor(random()*15), 60 + floor(random()*30),
            v_json_content, NOW(), NOW(), NOW()
        );
        
        -- Insert 3-5 CLOs varied
        clo_count := 3 + floor(random()*3);
        v_clo_ids := '{}';
        FOR i IN 1..clo_count LOOP
            v_clo_id := gen_random_uuid();
            INSERT INTO clos (
                id, syllabus_version_id, code, description, bloom_level, weight, created_by, created_at, updated_at
            ) VALUES (
                v_clo_id, v_syllabus_id, 'CLO' || i,
                CASE i 
                    WHEN 1 THEN 'Hiểu khái niệm cơ bản của ' || v_subject_record.name_vi
                    WHEN 2 THEN 'Áp dụng kỹ thuật ' || v_subject_record.code || ' vào thực tế'
                    ELSE 'Phân tích và tối ưu hóa trong ' || v_subject_record.name_vi
                END,
                CASE floor(random()*4) 
                    WHEN 0 THEN 'Understand' WHEN 1 THEN 'Apply' WHEN 2 THEN 'Analyze' ELSE 'Create' 
                END,
                100.0 / clo_count, v_admin_id, NOW(), NOW()
            );
            v_clo_ids := v_clo_ids || v_clo_id;
        END LOOP;
        
        -- Insert assessments varied
        -- [SỬA] Đã bỏ phần DECLARE ở đây, sử dụng biến mảng đã khai báo ở đầu
        v_assess_ids := '{}';
        FOR j IN 1..4 LOOP
            v_assess_id := gen_random_uuid();
            INSERT INTO assessment_schemes (
                id, syllabus_version_id, name, weight_percent, created_by, created_at, updated_at
            ) VALUES (
                v_assess_id, v_syllabus_id, assess_names[j], assess_weights[j] + floor(random()*5 - 2.5), v_admin_id, NOW(), NOW()
            );
            v_assess_ids := v_assess_ids || v_assess_id;
        END LOOP;
        
        -- Insert assessment_clo_mappings (random map)
        FOREACH v_clo_id IN ARRAY v_clo_ids LOOP
            FOREACH v_assess_id IN ARRAY v_assess_ids LOOP
                IF random() > 0.4 THEN -- 60% chance map
                    INSERT INTO assessment_clo_mappings (
                        id, assessment_scheme_id, clo_id, contribution_percent, created_at, updated_at
                    ) VALUES (
                        gen_random_uuid(), v_assess_id, v_clo_id, random()*30 + 10, NOW(), NOW()
                    );
                END IF;
            END LOOP;
        END LOOP;
        
        -- Insert clo_plo_mappings
        FOREACH v_clo_id IN ARRAY v_clo_ids LOOP
            FOR v_plo_record IN 
                SELECT id, code FROM plos WHERE curriculum_id = v_curriculum_id ORDER BY RANDOM() LIMIT 2 + floor(random()*3)
            LOOP
                v_weight := random()*0.3 + 0.1;
                INSERT INTO clo_plo_mappings (
                    id, clo_id, plo_id, weight, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(), v_clo_id, v_plo_record.id, v_weight, NOW(), NOW()
                );
            END LOOP;
        END LOOP;
        
        RAISE NOTICE 'Hoàn thiện data cho môn: % (%)', v_subject_record.name_vi, v_subject_record.code;
    END LOOP;
    
    RAISE NOTICE 'V42 Migration hoàn tất. Đã bổ sung data cho tất cả môn thiếu.';
END $$;