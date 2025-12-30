/*
 * V16__seed_plos_and_approved_syllabi.sql
 * Bổ sung dữ liệu:
 * - PLOs (Program Learning Outcomes) cho chương trình đào tạo
 * - CLO-PLO Mappings 
 * - Cập nhật một số syllabus thành APPROVED
 */

BEGIN;

SET search_path TO core_service;

DO $$BEGIN RAISE NOTICE 'Starting V16 Migration: Seeding PLOs and APPROVED syllabi...'; END$$;

-- ==========================================
-- 1. INSERT PROGRAM LEARNING OUTCOMES (PLOs)
-- ==========================================

-- Tạo curriculum và PLOs
DO $$
DECLARE
    v_curriculum_id UUID;
    v_admin_id UUID;
    v_faculty_id UUID;
BEGIN
    -- Lấy admin user
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@smd.edu.vn' LIMIT 1;
    
    -- Lấy faculty CNTT
    SELECT id INTO v_faculty_id FROM faculties WHERE code = 'CNTT' LIMIT 1;
    
    -- Lấy hoặc tạo curriculum
    SELECT id INTO v_curriculum_id FROM curriculums WHERE code = 'CNTT' LIMIT 1;
    
    IF v_curriculum_id IS NULL THEN
        RAISE NOTICE 'Creating CNTT curriculum...';
        INSERT INTO curriculums (id, code, name, faculty_id, total_credits, created_by)
        VALUES (
            gen_random_uuid(),
            'CNTT',
            'Chương trình đào tạo ngành Công nghệ thông tin',
            v_faculty_id,
            130,
            v_admin_id
        )
        RETURNING id INTO v_curriculum_id;
    END IF;
    
    RAISE NOTICE 'Using curriculum_id: %', v_curriculum_id;
    
    -- Insert PLOs cho chương trình CNTT
    INSERT INTO plos (id, curriculum_id, code, description, created_by) VALUES
    -- Kiến thức
    (gen_random_uuid(), v_curriculum_id, 'PLO1', 'Áp dụng kiến thức toán học, khoa học và kỹ thuật vào các bài toán công nghệ thông tin', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO2', 'Phân tích, thiết kế và đánh giá hệ thống phần mềm đáp ứng yêu cầu người dùng', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO3', 'Thiết kế và quản lý cơ sở dữ liệu hiệu quả cho các ứng dụng thực tế', v_admin_id),
    -- Kỹ năng
    (gen_random_uuid(), v_curriculum_id, 'PLO4', 'Lập trình thành thạo với nhiều ngôn ngữ và công nghệ hiện đại', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO5', 'Làm việc nhóm hiệu quả trong các dự án phát triển phần mềm', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO6', 'Giao tiếp chuyên nghiệp bằng văn bản và thuyết trình', v_admin_id),
    -- Thái độ
    (gen_random_uuid(), v_curriculum_id, 'PLO7', 'Tuân thủ đạo đức nghề nghiệp và trách nhiệm xã hội trong công nghệ', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO8', 'Học hỏi liên tục và thích ứng với công nghệ mới', v_admin_id),
    -- Năng lực
    (gen_random_uuid(), v_curriculum_id, 'PLO9', 'Quản lý dự án và nguồn lực phát triển phần mềm', v_admin_id),
    (gen_random_uuid(), v_curriculum_id, 'PLO10', 'Nghiên cứu và đề xuất giải pháp sáng tạo cho các vấn đề kỹ thuật', v_admin_id)
    ON CONFLICT DO NOTHING;
    
    RAISE NOTICE 'PLOs inserted for curriculum: %', v_curriculum_id;
END $$;

-- ==========================================
-- 2. INSERT CLO-PLO MAPPINGS
-- ==========================================

-- Tạo ánh xạ CLO-PLO dựa trên bloom_level
INSERT INTO clo_plo_mappings (clo_id, plo_id, weight, created_by)
SELECT 
    c.id as clo_id,
    p.id as plo_id,
    CASE 
        WHEN c.bloom_level = 'Understand' AND p.code IN ('PLO1', 'PLO2') THEN 0.4
        WHEN c.bloom_level = 'Analyze' AND p.code IN ('PLO2', 'PLO3') THEN 0.4
        WHEN c.bloom_level = 'Apply' AND p.code IN ('PLO3', 'PLO4') THEN 0.4
        WHEN c.bloom_level = 'Create' AND p.code IN ('PLO4', 'PLO10') THEN 0.4
        WHEN c.bloom_level = 'Evaluate' AND p.code IN ('PLO5', 'PLO6') THEN 0.3
        ELSE 0.2
    END as weight,
    c.created_by
FROM clos c
CROSS JOIN plos p
WHERE 
    -- CLO1 (Understand) -> PLO1, PLO2
    (c.code = 'CLO1' AND p.code IN ('PLO1', 'PLO2'))
    -- CLO2 (Analyze) -> PLO2, PLO3
    OR (c.code = 'CLO2' AND p.code IN ('PLO2', 'PLO3'))
    -- CLO3 (Apply) -> PLO3, PLO4
    OR (c.code = 'CLO3' AND p.code IN ('PLO3', 'PLO4'))
    -- CLO4 (Create) -> PLO4, PLO10
    OR (c.code = 'CLO4' AND p.code IN ('PLO4', 'PLO10'))
    -- CLO5 (Evaluate) -> PLO5, PLO6
    OR (c.code = 'CLO5' AND p.code IN ('PLO5', 'PLO6'))
ON CONFLICT DO NOTHING;

-- ==========================================
-- 3. UPDATE SOME SYLLABI TO APPROVED STATUS
-- ==========================================

-- Lấy principal user để ghi nhận người phê duyệt
DO $$
DECLARE
    v_principal_id UUID;
    v_updated_count INT := 0;
BEGIN
    SELECT id INTO v_principal_id FROM users WHERE email = 'principal@smd.edu.vn' LIMIT 1;
    
    -- Cập nhật 5 syllabus đầu tiên có status PENDING_PRINCIPAL thành APPROVED
    UPDATE syllabus_versions 
    SET 
        status = 'APPROVED',
        principal_approved_at = NOW(),
        principal_approved_by = v_principal_id,
        updated_at = NOW(),
        updated_by = v_principal_id
    WHERE id IN (
        SELECT id FROM syllabus_versions 
        WHERE status = 'PENDING_PRINCIPAL' 
        ORDER BY created_at 
        LIMIT 5
    );
    
    GET DIAGNOSTICS v_updated_count = ROW_COUNT;
    RAISE NOTICE 'Updated % syllabi to APPROVED status', v_updated_count;
END $$;

-- ==========================================
-- 4. LOG SUMMARY
-- ==========================================

DO $$
DECLARE
    plo_count INT;
    mapping_count INT;
    approved_count INT;
    pending_count INT;
BEGIN
    SELECT COUNT(*) INTO plo_count FROM plos;
    SELECT COUNT(*) INTO mapping_count FROM clo_plo_mappings;
    SELECT COUNT(*) INTO approved_count FROM syllabus_versions WHERE status = 'APPROVED';
    SELECT COUNT(*) INTO pending_count FROM syllabus_versions WHERE status = 'PENDING_PRINCIPAL';
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'V16 Migration completed!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'PLOs created: %', plo_count;
    RAISE NOTICE 'CLO-PLO mappings created: %', mapping_count;
    RAISE NOTICE 'APPROVED syllabi: %', approved_count;
    RAISE NOTICE 'PENDING_PRINCIPAL syllabi: %', pending_count;
    RAISE NOTICE '================================';
END $$;

COMMIT;
