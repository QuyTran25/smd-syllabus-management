-- Fix all Vietnamese encoding for subjects
SET client_encoding = 'UTF8';

-- General Education
UPDATE core_service.subjects SET current_name_vi = 'Tối ưu hóa', description = 'Phương pháp tối ưu hóa trong toán học.' WHERE code = '001210';
UPDATE core_service.subjects SET current_name_vi = 'Lý thuyết GDTC', description = 'Giáo dục thể chất lý thuyết.' WHERE code = '004101';
UPDATE core_service.subjects SET current_name_vi = 'Bơi lội', description = 'Kỹ năng bơi lội.' WHERE code = '004103';
UPDATE core_service.subjects SET current_name_vi = 'Bóng đá', description = 'Kỹ năng bóng đá.' WHERE code = '004107';
UPDATE core_service.subjects SET current_name_vi = 'Pháp luật đại cương', description = 'Kiến thức pháp luật cơ bản.' WHERE code = '005004';
UPDATE core_service.subjects SET current_name_vi = 'Tư tưởng Hồ Chí Minh', description = 'Tư tưởng Hồ Chí Minh.' WHERE code = '005102';
UPDATE core_service.subjects SET current_name_vi = 'Triết học Mác-Lênin', description = 'Triết học Mác-Lênin.' WHERE code = '005105';
UPDATE core_service.subjects SET current_name_vi = 'Kinh tế chính trị Mác-Lênin', description = 'Kinh tế chính trị Mác-Lênin.' WHERE code = '005106';
UPDATE core_service.subjects SET current_name_vi = 'Chủ nghĩa xã hội khoa học', description = 'Chủ nghĩa xã hội khoa học.' WHERE code = '005107';
UPDATE core_service.subjects SET current_name_vi = 'Lịch sử Đảng cộng sản VN', description = 'Lịch sử Đảng Cộng sản Việt Nam.' WHERE code = '005108';
UPDATE core_service.subjects SET current_name_vi = 'Đường lối quân sự', description = 'Đường lối quân sự của Đảng.' WHERE code = '007101';
UPDATE core_service.subjects SET current_name_vi = 'Công tác QP an ninh', description = 'Công tác quốc phòng an ninh.' WHERE code = '007102';
UPDATE core_service.subjects SET current_name_vi = 'Quân sự chung', description = 'Kiến thức quân sự chung.' WHERE code = '007103';
UPDATE core_service.subjects SET current_name_vi = 'Quân binh chủng', description = 'Kiến thức về binh chủng.' WHERE code = '007104';

-- IT Subjects
UPDATE core_service.subjects SET current_name_vi = 'Cơ sở dữ liệu', description = 'SQL, Đại số quan hệ, Thiết kế CSDL.' WHERE code = '121000';
UPDATE core_service.subjects SET current_name_vi = 'Thiết kế cơ sở dữ liệu', description = 'Thiết kế và tối ưu hóa CSDL.' WHERE code = '121002';
UPDATE core_service.subjects SET current_name_vi = 'Phân tích thiết kế hệ thống', description = 'UML, OOAD, thiết kế hệ thống.' WHERE code = '121008';
UPDATE core_service.subjects SET current_name_vi = 'Lập trình Web', description = 'HTML, CSS, JavaScript, PHP.' WHERE code = '121031';
UPDATE core_service.subjects SET current_name_vi = 'Trí tuệ nhân tạo', description = 'Machine Learning, Neural Network.' WHERE code = '121033';
UPDATE core_service.subjects SET current_name_vi = 'Lập trình Mobile', description = 'Android, iOS, React Native.' WHERE code = '121034';
UPDATE core_service.subjects SET current_name_vi = 'Quản trị doanh nghiệp CNTT', description = 'Quản trị doanh nghiệp công nghệ.' WHERE code = '121037';
UPDATE core_service.subjects SET current_name_vi = 'Lập trình hướng đối tượng', description = 'OOP, Kế thừa, Đa hình, Đóng gói.' WHERE code = '122003';
UPDATE core_service.subjects SET current_name_vi = 'Lý thuyết đồ thị', description = 'Đồ thị, thuật toán đồ thị.' WHERE code = '122004';
UPDATE core_service.subjects SET current_name_vi = 'Công nghệ phần mềm', description = 'Quy trình phát triển phần mềm.' WHERE code = '122005';
UPDATE core_service.subjects SET current_name_vi = 'Lập trình Java', description = 'Lập trình Java cơ bản và nâng cao.' WHERE code = '122036';
UPDATE core_service.subjects SET current_name_vi = 'Chuyên đề HTGT thông minh', description = 'Hệ thống giao thông thông minh, IoT.' WHERE code = '122038';
UPDATE core_service.subjects SET current_name_vi = 'Đồ án thực tế CNPM', description = 'Đồ án thực tế công nghệ phần mềm.' WHERE code = '122039';
UPDATE core_service.subjects SET current_name_vi = 'Chuyên đề thực tế 1', description = 'Thực hành dự án thực tế.' WHERE code = '122043';
UPDATE core_service.subjects SET current_name_vi = 'Lập trình mạng', description = 'Socket, Multithreading, Network Programming.' WHERE code = '123013';
UPDATE core_service.subjects SET current_name_vi = 'An toàn thông tin', description = 'Mã hóa, Bảo mật, Tấn công và phòng thủ.' WHERE code = '123033';

-- Other subjects that may have encoding issues
UPDATE core_service.subjects SET current_name_vi = 'Phân tích thiết kế giải thuật', description = 'Phân tích và thiết kế thuật toán.' WHERE current_name_vi LIKE 'Ph_n t_ch thi_t k_ gi_i thu_t';
UPDATE core_service.subjects SET current_name_vi = 'Cấu trúc dữ liệu và giải thuật', description = 'Mảng, Danh sách liên kết, Cây, Đồ thị.' WHERE current_name_vi LIKE 'C_u tr_c d_ li_u%';
UPDATE core_service.subjects SET current_name_vi = 'Hệ thống thông tin', description = 'Hệ thống thông tin quản lý.' WHERE current_name_vi LIKE 'H_ th_ng th_ng tin%';
UPDATE core_service.subjects SET current_name_vi = 'Xử lý ảnh', description = 'Xử lý ảnh số và thị giác máy tính.' WHERE current_name_vi LIKE 'X_ l_ _nh%';
UPDATE core_service.subjects SET current_name_vi = 'Mạng máy tính', description = 'Giao thức mạng, TCP/IP, Routing.' WHERE current_name_vi LIKE 'M_ng m_y t_nh%';
UPDATE core_service.subjects SET current_name_vi = 'Quản trị mạng', description = 'Quản trị và bảo trì hệ thống mạng.' WHERE current_name_vi LIKE 'Qu_n tr_ m_ng%';
UPDATE core_service.subjects SET current_name_vi = 'Điện toán đám mây', description = 'Cloud Computing, AWS, Azure.' WHERE current_name_vi LIKE '_i_n to_n _?m m_y%';
UPDATE core_service.subjects SET current_name_vi = 'Khai phá dữ liệu', description = 'Data Mining, Machine Learning.' WHERE current_name_vi LIKE 'Khai ph_ d_ li_u%';
UPDATE core_service.subjects SET current_name_vi = 'Học máy', description = 'Machine Learning, Deep Learning.' WHERE current_name_vi LIKE 'H_c m_y%';

-- Update any remaining subjects with ? characters
UPDATE core_service.subjects SET 
    current_name_vi = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
        current_name_vi, '?', 'đ'), 'ô', 'ô'), 'ư', 'ư'), 'ơ', 'ơ'), 'ă', 'ă'), 'â', 'â'), 'ê', 'ê'), 'á', 'á'), 'à', 'à'), 'ả', 'ả')
WHERE current_name_vi LIKE '%?%';

-- Count updated
SELECT COUNT(*) as subjects_with_issues FROM core_service.subjects WHERE current_name_vi LIKE '%?%';
