-- Recreate CLOs with proper Vietnamese
DELETE FROM core_service.clo_plo_mappings;
DELETE FROM core_service.assessment_clo_mappings;
DELETE FROM core_service.clos;

-- Insert CLOs for each syllabus
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
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (uuid_generate_v4(), syllabus.syllabus_id, 'CLO1', 
            'Trình bày được các khái niệm cơ bản, định nghĩa và nguyên lý nền tảng của ' || subject_name,
            'UNDERSTAND', 20.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (uuid_generate_v4(), syllabus.syllabus_id, 'CLO2',
            'Giải thích được cách thức hoạt động và mối quan hệ giữa các thành phần trong ' || subject_name,
            'UNDERSTAND', 15.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (uuid_generate_v4(), syllabus.syllabus_id, 'CLO3',
            'Áp dụng kiến thức ' || subject_name || ' để giải quyết các bài toán thực tế',
            'APPLY', 25.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (uuid_generate_v4(), syllabus.syllabus_id, 'CLO4',
            'Phân tích và đánh giá các giải pháp kỹ thuật trong lĩnh vực ' || subject_name,
            'ANALYZE', 20.00);
        
        INSERT INTO core_service.clos (id, syllabus_version_id, code, description, bloom_level, weight)
        VALUES (uuid_generate_v4(), syllabus.syllabus_id, 'CLO5',
            'Thiết kế và xây dựng được giải pháp hoàn chỉnh ứng dụng kiến thức ' || subject_name,
            'CREATE', 20.00);
    END LOOP;
END $$;

SELECT COUNT(*) as clo_count FROM core_service.clos;
