-- V31: Add full content to empty syllabi for testing
-- Add detailed content to syllabi 122002 (DRAFT) and 124003 (REVISION_IN_PROGRESS)

DO $$
DECLARE
    v_syllabus_122002 UUID;
    v_syllabus_124003 UUID;
BEGIN
    -- Get syllabus IDs
    SELECT id INTO v_syllabus_122002 
    FROM core_service.syllabus_versions 
    WHERE snap_subject_code = '122002' AND status = 'DRAFT';
    
    SELECT id INTO v_syllabus_124003 
    FROM core_service.syllabus_versions 
    WHERE snap_subject_code = '124003' AND status = 'REVISION_IN_PROGRESS';
    
    -- Update syllabus 122002 with full content
    IF v_syllabus_122002 IS NOT NULL THEN
        UPDATE core_service.syllabus_versions
        SET 
            description = 'Môn học cung cấp kiến thức toàn diện về Lập trình hướng đối tượng, bao gồm các khái niệm cơ bản, nguyên lý thiết kế và kỹ thuật lập trình nâng cao.',
            objectives = 'Hiểu rõ các khái niệm cơ bản về lập trình hướng đối tượng. Vận dụng các nguyên lý SOLID trong thiết kế phần mềm. Xây dựng các ứng dụng Java với các design patterns phổ biến. Phát triển kỹ năng debug và tối ưu hóa code. Làm việc nhóm hiệu quả trong dự án phần mềm.',
            content = jsonb_build_object(
                'description', 'Môn học cung cấp kiến thức toàn diện về Lập trình hướng đối tượng',
                'objectives', 'Hiểu rõ các khái niệm cơ bản về lập trình hướng đối tượng. Vận dụng các nguyên lý SOLID trong thiết kế phần mềm.',
                'teachingMethods', 'Giảng dạy lý thuyết kết hợp thực hành trên máy tính. Thảo luận nhóm và làm việc theo dự án. Học tập qua các case study thực tế từ doanh nghiệp.',
                'gradingPolicy', jsonb_build_object(
                    'midterm', 20,
                    'final', 40,
                    'assignments', 25,
                    'project', 15,
                    'bonus', 'Điểm cộng cho sinh viên tích cực'
                ),
                'textbooks', jsonb_build_array(
                    jsonb_build_object(
                        'type', 'required',
                        'title', 'Head First Object-Oriented Analysis and Design',
                        'authors', 'Brett McLaughlin, Gary Pollice, David West',
                        'publisher', 'O''Reilly Media',
                        'year', 2024
                    ),
                    jsonb_build_object(
                        'type', 'reference',
                        'title', 'Design Patterns: Elements of Reusable Object-Oriented Software',
                        'authors', 'Gang of Four',
                        'publisher', 'Addison-Wesley',
                        'year', 2023
                    )
                ),
                'prerequisites', jsonb_build_array(
                    jsonb_build_object('code', 'CS101', 'name', 'Nhập môn lập trình', 'type', 'required'),
                    jsonb_build_object('code', 'CS201', 'name', 'Cấu trúc dữ liệu', 'type', 'required')
                ),
                'courseOutline', jsonb_build_array(
                    jsonb_build_object('week', 1, 'topic', 'Giới thiệu OOP', 'content', 'Class, Object, Encapsulation', 'activities', 'Bài giảng + Lab'),
                    jsonb_build_object('week', 2, 'topic', 'Inheritance và Polymorphism', 'content', 'Kế thừa, đa hình, overriding', 'activities', 'Thực hành coding'),
                    jsonb_build_object('week', 3, 'topic', 'Abstraction và Interface', 'content', 'Abstract class, Interface, Contract', 'activities', 'Workshop'),
                    jsonb_build_object('week', 4, 'topic', 'SOLID Principles', 'content', 'Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion', 'activities', 'Case study')
                ),
                'studentDuties', 'Tham gia đầy đủ các buổi học (tối thiểu 80%). Hoàn thành các bài tập được giao đúng hạn. Tham gia tích cực vào các hoạt động nhóm. Làm đồ án nhóm và báo cáo kết quả.'
            )
        WHERE id = v_syllabus_122002;
        
        RAISE NOTICE 'Updated syllabus 122002 with full content';
    END IF;
    
    -- Update syllabus 124003 with full content
    IF v_syllabus_124003 IS NOT NULL THEN
        UPDATE core_service.syllabus_versions
        SET 
            description = 'Môn học giới thiệu về Trí tuệ nhân tạo, bao gồm các thuật toán học máy, mạng nơ-ron, xử lý ngôn ngữ tự nhiên và ứng dụng AI trong thực tế.',
            objectives = 'Nắm vững kiến thức cơ bản về AI và Machine Learning. Triển khai các thuật toán AI cơ bản bằng Python. Áp dụng Deep Learning vào các bài toán thực tế. Đánh giá và tối ưu hóa model AI. Hiểu về ethical AI và responsible AI.',
            content = jsonb_build_object(
                'description', 'Môn học giới thiệu về Trí tuệ nhân tạo với các thuật toán hiện đại',
                'objectives', 'Nắm vững kiến thức cơ bản về AI và Machine Learning. Triển khai các thuật toán AI cơ bản.',
                'teachingMethods', 'Thuyết trình kết hợp demo trực tiếp. Thực hành coding với Python và TensorFlow. Project-based learning với dataset thực tế. Guest lecture từ các chuyên gia AI.',
                'gradingPolicy', jsonb_build_object(
                    'midterm', 25,
                    'final', 35,
                    'assignments', 20,
                    'project', 20,
                    'bonus', 'Tham gia Kaggle competition'
                ),
                'textbooks', jsonb_build_array(
                    jsonb_build_object(
                        'type', 'required',
                        'title', 'Artificial Intelligence: A Modern Approach',
                        'authors', 'Stuart Russell, Peter Norvig',
                        'publisher', 'Pearson',
                        'year', 2024
                    ),
                    jsonb_build_object(
                        'type', 'reference',
                        'title', 'Deep Learning',
                        'authors', 'Ian Goodfellow, Yoshua Bengio, Aaron Courville',
                        'publisher', 'MIT Press',
                        'year', 2023
                    )
                ),
                'prerequisites', jsonb_build_array(
                    jsonb_build_object('code', 'CS201', 'name', 'Cấu trúc dữ liệu', 'type', 'required'),
                    jsonb_build_object('code', 'MATH101', 'name', 'Đại số tuyến tính', 'type', 'required'),
                    jsonb_build_object('code', 'STAT101', 'name', 'Xác suất thống kê', 'type', 'recommended')
                ),
                'courseOutline', jsonb_build_array(
                    jsonb_build_object('week', 1, 'topic', 'Introduction to AI', 'content', 'History, applications, future of AI', 'activities', 'Discussion'),
                    jsonb_build_object('week', 2, 'topic', 'Machine Learning Basics', 'content', 'Supervised, unsupervised, reinforcement learning', 'activities', 'Lab: Scikit-learn'),
                    jsonb_build_object('week', 3, 'topic', 'Neural Networks', 'content', 'Perceptron, backpropagation, activation functions', 'activities', 'Code from scratch'),
                    jsonb_build_object('week', 4, 'topic', 'Deep Learning', 'content', 'CNN, RNN, LSTM, Transformers', 'activities', 'Project kickoff')
                ),
                'studentDuties', 'Hoàn thành các bài tập coding hàng tuần. Tham gia các buổi lab practice. Đọc paper và present findings. Làm project nhóm về AI application.'
            )
        WHERE id = v_syllabus_124003;
        
        RAISE NOTICE 'Updated syllabus 124003 with full content';
    END IF;
    
    RAISE NOTICE 'Migration V31 completed successfully';
END $$;
