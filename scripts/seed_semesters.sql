-- Chèn dữ liệu học kỳ mẫu để Frontend có cái để hiển thị
INSERT INTO core_service.academic_terms (id, code, name, start_date, end_date, is_active, academic_year)
VALUES 
    (gen_random_uuid(), 'HK1_2024', 'Học kỳ 1 năm 2024-2025', '2024-09-01', '2025-01-15', FALSE, '2024-2025'),
    (gen_random_uuid(), 'HK2_2024', 'Học kỳ 2 năm 2024-2025', '2025-01-16', '2025-06-30', TRUE, '2024-2025')
ON CONFLICT (code) DO NOTHING;
