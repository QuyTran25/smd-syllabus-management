-- V32: Enrich syllabus data with complete information
-- Update academic terms with academic_year and add missing content fields

SET search_path TO core_service;

-- ==========================================
-- 1. UPDATE ACADEMIC TERMS WITH ACADEMIC_YEAR
-- ==========================================
UPDATE academic_terms SET academic_year = '2024-2025' WHERE code = 'HK1_2024';
UPDATE academic_terms SET academic_year = '2024-2025' WHERE code = 'HK2_2024';

-- ==========================================
-- 2. ENRICH SYLLABUS CONTENT WITH FULL DATA
-- ==========================================

-- Update syllabus 122002 (OOP) - DRAFT
UPDATE syllabus_versions 
SET content = jsonb_set(
    jsonb_set(
        jsonb_set(
            jsonb_set(
                jsonb_set(content, '{clos}', '[
                    {"code": "CLO1", "description": "Phân tích và thiết kế các lớp đối tượng phù hợp cho bài toán thực tế", "bloomLevel": "Phân tích", "weight": 25},
                    {"code": "CLO2", "description": "Hiểu và áp dụng các nguyên lý OOP: kế thừa, đa hình, đóng gói, trừu tượng", "bloomLevel": "Áp dụng", "weight": 30},
                    {"code": "CLO3", "description": "Xây dựng ứng dụng Java/C++ hoàn chỉnh sử dụng OOP", "bloomLevel": "Tạo tác", "weight": 30},
                    {"code": "CLO4", "description": "Làm việc nhóm và trình bày dự án OOP", "bloomLevel": "Tạo tác", "weight": 15}
                ]'::jsonb),
            '{assessmentMethods}', '[
                {"name": "Bài kiểm tra giữa kỳ", "type": "Thi viết", "weight": 30, "description": "Kiểm tra lý thuyết OOP và code nhỏ"},
                {"name": "Bài tập thực hành", "type": "Bài tập", "weight": 20, "description": "4 bài tập lớn về OOP"},
                {"name": "Đồ án nhóm", "type": "Dự án", "weight": 20, "description": "Xây dựng ứng dụng OOP hoàn chỉnh"},
                {"name": "Thi cuối kỳ", "type": "Thi viết + Code", "weight": 30, "description": "Tổng hợp toàn bộ kiến thức"}
            ]'::jsonb),
        '{gradingPolicy}', '{"midterm": 30, "final": 30, "assignments": 20, "project": 20}'::jsonb),
    '{textbooks}', '[
        {"title": "Lập trình hướng đối tượng với Java", "authors": "Nguyễn Văn A, Trần Thị B", "year": "2022"},
        {"title": "Head First Object-Oriented Analysis and Design", "authors": "Brett McLaughlin", "year": "2006"}
    ]'::jsonb),
'{references}', '"1. Effective Java - Joshua Bloch (2018)\n2. Design Patterns: Elements of Reusable Object-Oriented Software - Gang of Four (1994)\n3. Clean Code - Robert C. Martin (2008)"'::jsonb)
WHERE snap_subject_code = '122002';

-- Update syllabus 121031 (Database) - PENDING_HOD
UPDATE syllabus_versions 
SET content = jsonb_build_object(
    'description', 'Môn học cung cấp kiến thức nền tảng về hệ quản trị cơ sở dữ liệu quan hệ, SQL, thiết kế cơ sở dữ liệu và các kỹ thuật tối ưu hóa truy vấn.',
    'objectives', 'Sinh viên hiểu các khái niệm CSDL quan hệ. Thành thạo SQL và thiết kế schema. Áp dụng normalization và indexing. Xây dựng ứng dụng kết nối CSDL.',
    'teachingMethods', 'Giảng lý thuyết kết hợp thực hành SQL trên PostgreSQL/MySQL. Phân tích case study thiết kế CSDL thực tế. Lab thực hành tuần và dự án nhóm.',
    'gradingPolicy', jsonb_build_object('midterm', 30, 'final', 40, 'assignments', 20, 'project', 10),
    'textbooks', jsonb_build_array(
        jsonb_build_object('title', 'Database System Concepts', 'authors', 'Silberschatz, Korth, Sudarshan', 'year', '2019'),
        jsonb_build_object('title', 'Cơ sở dữ liệu quan hệ', 'authors', 'Phạm Văn C', 'year', '2021')
    ),
    'references', '1. SQL Performance Explained - Markus Winand\n2. PostgreSQL Documentation\n3. MySQL High Performance - Baron Schwartz',
    'studentDuties', 'Tham gia đầy đủ các buổi học và lab (tối thiểu 80%). Hoàn thành bài tập SQL hàng tuần. Thực hiện dự án thiết kế CSDL cho hệ thống thực tế.',
    'clos', jsonb_build_array(
        jsonb_build_object('code', 'CLO1', 'description', 'Hiểu các khái niệm CSDL quan hệ, ràng buộc, khóa', 'bloomLevel', 'Hiểu', 'weight', 20),
        jsonb_build_object('code', 'CLO2', 'description', 'Thành thạo SQL: SELECT, JOIN, subquery, aggregation', 'bloomLevel', 'Áp dụng', 'weight', 30),
        jsonb_build_object('code', 'CLO3', 'description', 'Thiết kế schema, normalization, ER diagram', 'bloomLevel', 'Phân tích', 'weight', 25),
        jsonb_build_object('code', 'CLO4', 'description', 'Tối ưu hóa truy vấn, indexing, transaction', 'bloomLevel', 'Áp dụng', 'weight', 25)
    ),
    'assessmentMethods', jsonb_build_array(
        jsonb_build_object('name', 'Kiểm tra giữa kỳ', 'type', 'Thi viết + SQL', 'weight', 30, 'description', 'Lý thuyết và viết truy vấn SQL'),
        jsonb_build_object('name', 'Bài tập SQL', 'type', 'Bài tập', 'weight', 20, 'description', 'Bài tập hàng tuần về SQL'),
        jsonb_build_object('name', 'Dự án thiết kế CSDL', 'type', 'Dự án', 'weight', 10, 'description', 'Thiết kế CSDL cho hệ thống thực tế'),
        jsonb_build_object('name', 'Thi cuối kỳ', 'type', 'Thi viết + SQL', 'weight', 40, 'description', 'Tổng hợp toàn bộ kiến thức')
    ),
    'prerequisites', jsonb_build_array(
        jsonb_build_object('code', 'CS101', 'name', 'Nhập môn lập trình', 'type', 'required'),
        jsonb_build_object('code', 'CS201', 'name', 'Cấu trúc dữ liệu', 'type', 'recommended')
    )
)
WHERE snap_subject_code = '121031';

-- Update syllabus 121033 (Web Development) - DRAFT
UPDATE syllabus_versions 
SET content = jsonb_build_object(
    'description', 'Môn học trang bị kiến thức và kỹ năng phát triển ứng dụng web full-stack hiện đại với HTML5, CSS3, JavaScript, React, Node.js và cơ sở dữ liệu.',
    'objectives', 'Thành thạo HTML/CSS/JavaScript và responsive design. Xây dựng SPA với React. Phát triển REST API với Node.js/Express. Tích hợp frontend-backend-database.',
    'teachingMethods', 'Giảng lý thuyết kết hợp live coding demo. Workshop xây dựng components React. Hackathon phát triển web app. Code review và pair programming.',
    'gradingPolicy', jsonb_build_object('midterm', 25, 'final', 35, 'assignments', 25, 'project', 15),
    'textbooks', jsonb_build_array(
        jsonb_build_object('title', 'Learning React', 'authors', 'Alex Banks, Eve Porcello', 'year', '2020'),
        jsonb_build_object('title', 'Node.js Design Patterns', 'authors', 'Mario Casciaro', 'year', '2020')
    ),
    'references', '1. MDN Web Docs - Mozilla\n2. React Documentation\n3. Node.js Best Practices\n4. CSS Tricks',
    'studentDuties', 'Tham gia đầy đủ các buổi học và workshop. Hoàn thành coding exercises hàng tuần. Deploy ứng dụng web lên cloud. Tham gia code review nhóm.',
    'clos', jsonb_build_array(
        jsonb_build_object('code', 'CLO1', 'description', 'Thành thạo HTML5, CSS3, JavaScript ES6+', 'bloomLevel', 'Áp dụng', 'weight', 25),
        jsonb_build_object('code', 'CLO2', 'description', 'Xây dựng SPA với React Hooks và state management', 'bloomLevel', 'Tạo tác', 'weight', 30),
        jsonb_build_object('code', 'CLO3', 'description', 'Phát triển REST API với Node.js/Express/MongoDB', 'bloomLevel', 'Tạo tác', 'weight', 25),
        jsonb_build_object('code', 'CLO4', 'description', 'Deploy và maintain web app production-ready', 'bloomLevel', 'Áp dụng', 'weight', 20)
    ),
    'assessmentMethods', jsonb_build_array(
        jsonb_build_object('name', 'Kiểm tra giữa kỳ', 'type', 'Thi thực hành', 'weight', 25, 'description', 'Xây dựng web page responsive'),
        jsonb_build_object('name', 'Bài tập coding', 'type', 'Bài tập', 'weight', 25, 'description', 'Exercises về React và Node.js'),
        jsonb_build_object('name', 'Dự án web app', 'type', 'Dự án', 'weight', 15, 'description', 'Full-stack web application'),
        jsonb_build_object('name', 'Thi cuối kỳ', 'type', 'Thi thực hành', 'weight', 35, 'description', 'Xây dựng web app hoàn chỉnh')
    ),
    'prerequisites', jsonb_build_array(
        jsonb_build_object('code', 'CS101', 'name', 'Nhập môn lập trình', 'type', 'required'),
        jsonb_build_object('code', 'CS102', 'name', 'Lập trình hướng đối tượng', 'type', 'recommended')
    )
)
WHERE snap_subject_code = '121033';

-- Update syllabus 122043 (Mobile App Development) - DRAFT
UPDATE syllabus_versions 
SET content = jsonb_build_object(
    'description', 'Môn học cung cấp kiến thức và kỹ năng phát triển ứng dụng di động đa nền tảng với React Native, Flutter hoặc native Android/iOS.',
    'objectives', 'Hiểu kiến trúc ứng dụng mobile. Thành thạo React Native hoặc Flutter. Tích hợp API, database, authentication. Publish app lên store.',
    'teachingMethods', 'Giảng lý thuyết về mobile architecture. Live coding React Native/Flutter. Lab thực hành trên thiết bị thật. Sprint development theo Scrum.',
    'gradingPolicy', jsonb_build_object('midterm', 25, 'final', 30, 'assignments', 20, 'project', 25),
    'textbooks', jsonb_build_array(
        jsonb_build_object('title', 'React Native in Action', 'authors', 'Nader Dabit', 'year', '2019'),
        jsonb_build_object('title', 'Flutter in Action', 'authors', 'Eric Windmill', 'year', '2020')
    ),
    'references', '1. React Native Documentation\n2. Flutter Documentation\n3. iOS Human Interface Guidelines\n4. Material Design Guidelines',
    'studentDuties', 'Tham gia đầy đủ các buổi học và lab. Hoàn thành sprint tasks hàng tuần. Test app trên nhiều thiết bị. Tham gia sprint review và retrospective.',
    'clos', jsonb_build_array(
        jsonb_build_object('code', 'CLO1', 'description', 'Hiểu kiến trúc mobile app và lifecycle', 'bloomLevel', 'Hiểu', 'weight', 20),
        jsonb_build_object('code', 'CLO2', 'description', 'Thành thạo React Native/Flutter components và navigation', 'bloomLevel', 'Áp dụng', 'weight', 30),
        jsonb_build_object('code', 'CLO3', 'description', 'Tích hợp API, local storage, push notification', 'bloomLevel', 'Áp dụng', 'weight', 25),
        jsonb_build_object('code', 'CLO4', 'description', 'Xây dựng và deploy mobile app hoàn chỉnh', 'bloomLevel', 'Tạo tác', 'weight', 25)
    ),
    'assessmentMethods', jsonb_build_array(
        jsonb_build_object('name', 'Kiểm tra giữa kỳ', 'type', 'Thi thực hành', 'weight', 25, 'description', 'Xây dựng màn hình mobile'),
        jsonb_build_object('name', 'Sprint tasks', 'type', 'Bài tập', 'weight', 20, 'description', 'Hoàn thành features theo sprint'),
        jsonb_build_object('name', 'Mobile app project', 'type', 'Dự án', 'weight', 25, 'description', 'Ứng dụng mobile hoàn chỉnh'),
        jsonb_build_object('name', 'Thi cuối kỳ', 'type', 'Thi thực hành', 'weight', 30, 'description', 'Xây dựng app với yêu cầu mới')
    ),
    'prerequisites', jsonb_build_array(
        jsonb_build_object('code', 'CS102', 'name', 'Lập trình hướng đối tượng', 'type', 'required'),
        jsonb_build_object('code', 'CS103', 'name', 'Phát triển web', 'type', 'recommended')
    )
)
WHERE snap_subject_code = '122043';

-- Update syllabus 121000 (Introduction to Programming) - REJECTED
UPDATE syllabus_versions 
SET content = jsonb_build_object(
    'description', 'Môn học nhập môn lập trình, cung cấp kiến thức nền tảng về tư duy thuật toán, cấu trúc dữ liệu cơ bản và ngôn ngữ lập trình Python.',
    'objectives', 'Hiểu khái niệm thuật toán và lập trình. Thành thạo cú pháp Python cơ bản. Giải quyết bài toán bằng code. Debug và test chương trình.',
    'teachingMethods', 'Giảng lý thuyết về algorithms. Live coding Python trong lớp. Bài tập thực hành tự làm. Peer programming và code review.',
    'gradingPolicy', jsonb_build_object('midterm', 30, 'final', 40, 'assignments', 25, 'project', 5),
    'textbooks', jsonb_build_array(
        jsonb_build_object('title', 'Python Crash Course', 'authors', 'Eric Matthes', 'year', '2019'),
        jsonb_build_object('title', 'Think Python', 'authors', 'Allen Downey', 'year', '2015')
    ),
    'references', '1. Python Documentation\n2. Real Python Tutorials\n3. Automate the Boring Stuff with Python',
    'studentDuties', 'Tham gia đầy đủ các buổi học lý thuyết và thực hành. Hoàn thành bài tập coding hàng tuần trên online judge. Đọc tài liệu và tự học.',
    'clos', jsonb_build_array(
        jsonb_build_object('code', 'CLO1', 'description', 'Hiểu khái niệm biến, kiểu dữ liệu, vòng lặp, hàm', 'bloomLevel', 'Hiểu', 'weight', 30),
        jsonb_build_object('code', 'CLO2', 'description', 'Viết chương trình Python giải quyết bài toán đơn giản', 'bloomLevel', 'Áp dụng', 'weight', 35),
        jsonb_build_object('code', 'CLO3', 'description', 'Debug và fix lỗi trong code', 'bloomLevel', 'Phân tích', 'weight', 20),
        jsonb_build_object('code', 'CLO4', 'description', 'Đọc hiểu code của người khác', 'bloomLevel', 'Hiểu', 'weight', 15)
    ),
    'assessmentMethods', jsonb_build_array(
        jsonb_build_object('name', 'Kiểm tra giữa kỳ', 'type', 'Thi coding', 'weight', 30, 'description', 'Viết code giải bài toán'),
        jsonb_build_object('name', 'Bài tập hàng tuần', 'type', 'Bài tập', 'weight', 25, 'description', 'Coding exercises trên online judge'),
        jsonb_build_object('name', 'Mini project', 'type', 'Dự án', 'weight', 5, 'description', 'Chương trình Python nhỏ'),
        jsonb_build_object('name', 'Thi cuối kỳ', 'type', 'Thi coding', 'weight', 40, 'description', 'Tổng hợp kiến thức Python')
    ),
    'prerequisites', jsonb_build_array()
)
WHERE snap_subject_code = '121000';
