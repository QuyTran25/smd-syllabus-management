-- Fix UTF-8 encoding for all users
UPDATE core_service.users SET full_name = 'Lê Hoàng Nam' WHERE email = 'gv.le@smd.edu.vn';
UPDATE core_service.users SET full_name = 'Nguyễn Minh Tuấn' WHERE email = 'gv.nguyen@smd.edu.vn';
UPDATE core_service.users SET full_name = 'Phạm Thị Hoa' WHERE email = 'gv.pham@smd.edu.vn';
UPDATE core_service.users SET full_name = 'Trần Thị Lan' WHERE email = 'gv.tran@smd.edu.vn';
UPDATE core_service.users SET full_name = 'Võ Thị Ngọc' WHERE email = 'gv.vo@smd.edu.vn';

-- Fix department names
UPDATE core_service.departments SET name = 'Kỹ thuật Phần mềm' WHERE code = 'KTPM';
UPDATE core_service.departments SET name = 'Khoa học Máy tính' WHERE code = 'KHMT';
UPDATE core_service.departments SET name = 'Hệ thống Thông tin' WHERE code = 'HTTT';

-- Fix subject names
UPDATE core_service.subjects SET name_vi = 'Kỹ thuật lập trình' WHERE code = '124001';
UPDATE core_service.subjects SET name_vi = 'Cấu trúc dữ liệu và giải thuật' WHERE code = '124002';
UPDATE core_service.subjects SET name_vi = 'Cơ sở dữ liệu' WHERE code = '124003';
UPDATE core_service.subjects SET name_vi = 'Công nghệ phần mềm' WHERE code = '124004';
UPDATE core_service.subjects SET name_vi = 'Mạng máy tính' WHERE code = '124005';
UPDATE core_service.subjects SET name_vi = 'Hệ điều hành' WHERE code = '125001';
UPDATE core_service.subjects SET name_vi = 'Trí tuệ nhân tạo' WHERE code = '125002';
UPDATE core_service.subjects SET name_vi = 'Xử lý ảnh số' WHERE code = '125003';
UPDATE core_service.subjects SET name_vi = 'Hệ thống thông tin quản lý' WHERE code = '126001';
UPDATE core_service.subjects SET name_vi = 'Phân tích và thiết kế hệ thống' WHERE code = '126002';

-- Fix academic terms
UPDATE core_service.academic_terms SET name = 'Học kỳ 1 năm 2024-2025' WHERE code = 'HK1_2024';
UPDATE core_service.academic_terms SET name = 'Học kỳ 2 năm 2024-2025' WHERE code = 'HK2_2024';

-- Verify
SELECT 'Users fixed:' as info, COUNT(*) as count FROM core_service.users WHERE full_name !~ '[?]';
SELECT 'Departments fixed:' as info, COUNT(*) as count FROM core_service.departments WHERE name !~ '[?]';
SELECT 'Subjects fixed:' as info, COUNT(*) as count FROM core_service.subjects WHERE name_vi !~ '[?]';
SELECT 'Academic terms fixed:' as info, COUNT(*) as count FROM core_service.academic_terms WHERE name !~ '[?]';
