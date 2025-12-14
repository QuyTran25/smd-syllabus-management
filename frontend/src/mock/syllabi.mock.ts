import { Syllabus, SyllabusStatus, CLO, AssessmentCriteria, CLOPLOMapping } from '@/types';

// Sample CLOs
const generateCLOs = (count: number): CLO[] => {
  const bloomLevels = ['Remember', 'Understand', 'Apply', 'Analyze', 'Evaluate', 'Create'];
  return Array.from({ length: count }, (_, i) => ({
    id: `clo-${i + 1}`,
    code: `CLO${i + 1}`,
    description: `Sinh viên có khả năng ${['hiểu', 'phân tích', 'áp dụng', 'đánh giá', 'tổng hợp'][i % 5]} các khái niệm cơ bản của môn học`,
    bloomLevel: bloomLevels[i % bloomLevels.length],
    weight: Math.round(100 / count),
  }));
};

// Sample Assessment Criteria
const generateAssessmentCriteria = (): AssessmentCriteria[] => [
  {
    id: 'assess-1',
    name: 'Thi giữa kỳ',
    description: 'Kiểm tra kiến thức lý thuyết giữa học kỳ',
    weight: 20,
    method: 'Thi viết',
    relatedCLOs: ['clo-1', 'clo-2'],
  },
  {
    id: 'assess-2',
    name: 'Thi cuối kỳ',
    description: 'Kiểm tra tổng hợp kiến thức toàn bộ môn học',
    weight: 50,
    method: 'Thi viết',
    relatedCLOs: ['clo-1', 'clo-2', 'clo-3', 'clo-4'],
  },
  {
    id: 'assess-3',
    name: 'Bài tập lớn',
    description: 'Thực hành và áp dụng kiến thức vào bài toán thực tế',
    weight: 20,
    method: 'Báo cáo + Demo',
    relatedCLOs: ['clo-3', 'clo-4'],
  },
  {
    id: 'assess-4',
    name: 'Chuyên cần',
    description: 'Điểm danh, tham gia lớp học',
    weight: 10,
    method: 'Quan sát',
    relatedCLOs: [],
  },
];

// Sample PLO Mappings
const generatePLOMappings = (): CLOPLOMapping[] => [
  { cloId: 'clo-1', ploId: 'plo-1', contributionLevel: 'HIGH' },
  { cloId: 'clo-1', ploId: 'plo-2', contributionLevel: 'MEDIUM' },
  { cloId: 'clo-2', ploId: 'plo-2', contributionLevel: 'HIGH' },
  { cloId: 'clo-3', ploId: 'plo-3', contributionLevel: 'HIGH' },
  { cloId: 'clo-4', ploId: 'plo-4', contributionLevel: 'MEDIUM' },
];

// Course data templates
const courseTemplates = [
  // Software Engineering Department
  { code: 'SE101', name: 'Nhập môn Lập trình', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE102', name: 'Cấu trúc Dữ liệu và Giải thuật', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'SE201', name: 'Lập trình Hướng đối tượng', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE202', name: 'Cơ sở Dữ liệu', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE301', name: 'Công nghệ Phần mềm', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'SE302', name: 'Kiến trúc Phần mềm', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE303', name: 'Phát triển Ứng dụng Web', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'SE304', name: 'Phát triển Ứng dụng Mobile', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE401', name: 'Quản lý Dự án Phần mềm', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE402', name: 'Đảm bảo Chất lượng Phần mềm', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE403', name: 'DevOps và CI/CD', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE501', name: 'Microservices Architecture', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'SE502', name: 'Cloud Computing', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'SE503', name: 'Machine Learning cơ bản', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'SE504', name: 'Blockchain và Web3', dept: 'Software Engineering', faculty: 'Công Nghệ Thông Tin', credits: 3 },

  // Computer Science Department
  { code: 'CS101', name: 'Toán Rời rạc', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS102', name: 'Xác suất Thống kê', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS201', name: 'Trí tuệ Nhân tạo', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'CS202', name: 'Học Máy', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'CS301', name: 'Xử lý Ngôn ngữ Tự nhiên', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS302', name: 'Computer Vision', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'CS303', name: 'Deep Learning', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'CS401', name: 'Big Data Analytics', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS402', name: 'Data Mining', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS501', name: 'Reinforcement Learning', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 4 },
  { code: 'CS502', name: 'Graph Neural Networks', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 3 },
  { code: 'CS503', name: 'AI Ethics', dept: 'Computer Science', faculty: 'Công Nghệ Thông Tin', credits: 2 },

  // Electronics Engineering Department
  { code: 'EE101', name: 'Mạch Điện Tử Cơ bản', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE102', name: 'Vật lý Bán dẫn', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 3 },
  { code: 'EE201', name: 'Mạch Tương tự', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE202', name: 'Mạch Số', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE301', name: 'Vi xử lý và Vi điều khiển', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE302', name: 'Hệ thống Nhúng', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE401', name: 'IoT và Ứng dụng', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 3 },
  { code: 'EE402', name: 'FPGA và ASIC', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 4 },
  { code: 'EE501', name: 'Thiết kế PCB nâng cao', dept: 'Electronics Engineering', faculty: 'Kỹ Thuật Điện - Điện Tử', credits: 3 },

  // Mechanical Engineering Department
  { code: 'ME101', name: 'Vẽ Kỹ thuật', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 3 },
  { code: 'ME102', name: 'Sức Bền Vật liệu', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 4 },
  { code: 'ME201', name: 'Cơ học Lý thuyết', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 4 },
  { code: 'ME202', name: 'Nhiệt động Lực học', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 3 },
  { code: 'ME301', name: 'CAD/CAM/CAE', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 4 },
  { code: 'ME302', name: 'Công nghệ Chế tạo Máy', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 4 },
  { code: 'ME401', name: 'Robot Công nghiệp', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 4 },
  { code: 'ME402', name: 'Tự động hóa Sản xuất', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 3 },
  { code: 'ME501', name: 'In 3D và Ứng dụng', dept: 'Mechanical Engineering', faculty: 'Kỹ Thuật Cơ Khí', credits: 3 },
];

// Get random lecturer from department
const getLecturerByDept = (dept: string): { id: string; name: string } => {
  const lecturers = {
    'Software Engineering': [
      { id: 'lec-001', name: 'ThS. Nguyễn Văn An' },
      { id: 'lec-002', name: 'TS. Trần Thị Bình' },
      { id: 'lec-003', name: 'ThS. Lê Văn Cường' },
    ],
    'Computer Science': [
      { id: 'lec-004', name: 'ThS. Phạm Thị Dung' },
      { id: 'lec-005', name: 'TS. Hoàng Văn Em' },
      { id: 'lec-006', name: 'ThS. Đỗ Thị Phương' },
    ],
    'Electronics Engineering': [
      { id: 'lec-007', name: 'ThS. Vũ Văn Giang' },
      { id: 'lec-008', name: 'TS. Bùi Thị Hà' },
      { id: 'lec-009', name: 'ThS. Đinh Văn Inh' },
    ],
    'Mechanical Engineering': [
      { id: 'lec-010', name: 'ThS. Cao Thị Kim' },
      { id: 'lec-011', name: 'TS. Mai Văn Long' },
      { id: 'lec-012', name: 'ThS. Tô Thị Mai' },
    ],
  };
  const deptLecturers = lecturers[dept as keyof typeof lecturers] || lecturers['Software Engineering'];
  return deptLecturers[Math.floor(Math.random() * deptLecturers.length)];
};

// Generate syllabus status distribution
const getStatusByIndex = (index: number): SyllabusStatus => {
  // Distribute evenly across statuses
  if (index < 10) return SyllabusStatus.DRAFT;
  if (index < 20) return SyllabusStatus.PENDING_HOD;
  if (index < 30) return SyllabusStatus.PENDING_AA;
  if (index < 35) return SyllabusStatus.PENDING_PRINCIPAL;
  if (index < 45) return SyllabusStatus.APPROVED;
  if (index < 55) return SyllabusStatus.PUBLISHED;
  if (index < 58) return SyllabusStatus.REJECTED;
  return SyllabusStatus.ARCHIVED;
};

// Generate 60 syllabi
export const mockSyllabi: Syllabus[] = courseTemplates.map((course, index) => {
  const lecturer = getLecturerByDept(course.dept);
  const status = getStatusByIndex(index);
  const clos = generateCLOs(4);
  const createdDate = new Date(2024, 0, 1 + index);
  
  return {
    id: `syllabus-${String(index + 1).padStart(3, '0')}`,
    courseCode: course.code,
    courseName: course.name,
    courseNameEn: course.name, // In real app, would be English translation
    credits: course.credits,
    
    semester: index % 2 === 0 ? 'HK1-2024' : 'HK2-2024',
    academicYear: '2024-2025',
    department: course.dept,
    faculty: course.faculty,
    
    // New fields theo template
    courseType: index % 3 === 0 ? 'required' : (index % 3 === 1 ? 'elective' : 'free'),
    componentType: index < 10 ? 'general' : (index < 30 ? 'foundation' : (index < 50 ? 'major' : 'thesis')),
    timeAllocation: {
      theory: course.credits * 10,
      practice: course.credits * 10,
      selfStudy: course.credits * 30,
    },
    gradeScale: 10,
    studentDuties: '- Tham gia đầy đủ các buổi học (tối thiểu 80% số tiết)\n- Hoàn thành đầy đủ các bài tập được giao\n- Tham gia tích cực vào các hoạt động học tập nhóm\n- Tự nghiên cứu và chuẩn bị trước nội dung bài học',
    
    description: `Môn học ${course.name} cung cấp cho sinh viên những kiến thức cơ bản và nâng cao về lĩnh vực ${course.name.toLowerCase()}. Sinh viên sẽ được trang bị kỹ năng thực hành và lý thuyết vững chắc để áp dụng vào thực tế.`,
    objectives: [
      `Hiểu được các khái niệm cơ bản về ${course.name}`,
      'Phát triển kỹ năng phân tích và giải quyết vấn đề',
      'Áp dụng kiến thức vào các dự án thực tế',
      'Làm việc nhóm hiệu quả và giao tiếp chuyên nghiệp',
    ],
    outline: `# Nội dung chi tiết môn học ${course.name}\n\n## Chương 1: Giới thiệu\n- Tổng quan về môn học\n- Ứng dụng thực tế\n\n## Chương 2: Kiến thức cơ bản\n- Các khái niệm nền tảng\n- Nguyên lý hoạt động\n\n## Chương 3: Kỹ năng thực hành\n- Bài tập thực hành\n- Case study\n\n## Chương 4: Ứng dụng nâng cao\n- Dự án nhóm\n- Nghiên cứu chuyên sâu`,
    
    clos,
    ploMappings: generatePLOMappings(),
    assessmentMethods: [
      { id: '1', method: 'Thi giữa kỳ', form: 'Thi', clos: ['CLO1', 'CLO2'], criteria: 'A1.2', weight: 30 },
      { id: '2', method: 'Thi cuối kỳ', form: 'Thi', clos: ['CLO1', 'CLO2', 'CLO3', 'CLO4'], criteria: 'A2.1', weight: 40 },
      { id: '3', method: 'Dự án nhóm', form: 'Nhóm', clos: ['CLO3', 'CLO4'], criteria: 'A3.1', weight: 20 },
      { id: '4', method: 'Bài tập', form: 'Cá nhân', clos: ['CLO2'], criteria: 'A1.1', weight: 10 },
    ],
    assessmentCriteria: generateAssessmentCriteria(),
    prerequisites: index > 0 && index % 5 !== 0 ? [{
      courseId: `syllabus-${String(index).padStart(3, '0')}`,
      courseCode: courseTemplates[index - 1].code,
      courseName: courseTemplates[index - 1].name,
      type: 'required',
    }] : [],
    
    status,
    version: 1,
    
    ownerId: lecturer.id,
    ownerName: lecturer.name,
    
    createdAt: createdDate.toISOString(),
    updatedAt: new Date(createdDate.getTime() + 86400000 * (index % 10)).toISOString(),
    submittedAt: status !== SyllabusStatus.DRAFT ? new Date(createdDate.getTime() + 86400000).toISOString() : undefined,
    hodApprovedAt: [SyllabusStatus.PENDING_AA, SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status) 
      ? new Date(createdDate.getTime() + 86400000 * 2).toISOString() : undefined,
    hodApprovedBy: [SyllabusStatus.PENDING_AA, SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status) 
      ? 'hod-001' : undefined,
    aaApprovedAt: [SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status)
      ? new Date(createdDate.getTime() + 86400000 * 4).toISOString() : undefined,
    aaApprovedBy: [SyllabusStatus.PENDING_PRINCIPAL, SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status)
      ? 'aa-001' : undefined,
    principalApprovedAt: [SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status)
      ? new Date(createdDate.getTime() + 86400000 * 6).toISOString() : undefined,
    principalApprovedBy: [SyllabusStatus.APPROVED, SyllabusStatus.PUBLISHED].includes(status)
      ? 'principal-001' : undefined,
    publishedAt: status === SyllabusStatus.PUBLISHED
      ? new Date(createdDate.getTime() + 86400000 * 7).toISOString() : undefined,
    publishedBy: status === SyllabusStatus.PUBLISHED ? 'admin-001' : undefined,
    
    totalStudyHours: course.credits * 15,
    theoryHours: Math.floor(course.credits * 15 * 0.6),
    practiceHours: Math.floor(course.credits * 15 * 0.3),
    selfStudyHours: Math.floor(course.credits * 15 * 0.1),
    
    references: [
      'Giáo trình chính của môn học (2024)',
      'Tài liệu tham khảo từ IEEE',
      'Research papers liên quan',
    ],
    materials: [
      'Slide bài giảng',
      'Video hướng dẫn',
      'Bài tập thực hành',
    ],
  };
});

// Helper functions
export const getSyllabusByStatus = (status: SyllabusStatus): Syllabus[] => {
  return mockSyllabi.filter((s) => s.status === status);
};

export const getSyllabusByDepartment = (dept: string): Syllabus[] => {
  return mockSyllabi.filter((s) => s.department === dept);
};

export const getSyllabusByOwner = (ownerId: string): Syllabus[] => {
  return mockSyllabi.filter((s) => s.ownerId === ownerId);
};
