import { Course, CourseFilters, CoursePrerequisite } from '@/types';

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Mock courses data (do PĐT tạo)
export const mockCourses: Course[] = [
  {
    id: 'course-1',
    code: 'CS101',
    name: 'Lập trình căn bản',
    nameEn: 'Introduction to Programming',
    credits: 3,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-1',
    semesterName: 'HK1 2024-2025',
    prerequisites: [], // Môn cơ bản không có tiên quyết
    createdAt: '2024-08-01T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-08-01T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-2',
    code: 'CS201',
    name: 'Cấu trúc dữ liệu và Giải thuật',
    nameEn: 'Data Structures and Algorithms',
    credits: 4,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-2',
    semesterName: 'HK2 2024-2025',
    prerequisites: [
      {
        courseId: 'course-1',
        courseCode: 'CS101',
        courseName: 'Lập trình căn bản',
        type: 'required', // Bắt buộc phải học CS101 trước
      },
    ],
    createdAt: '2024-08-15T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-08-15T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-3',
    code: 'CS202',
    name: 'Toán rời rạc',
    nameEn: 'Discrete Mathematics',
    credits: 3,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-1',
    semesterName: 'HK1 2024-2025',
    prerequisites: [],
    createdAt: '2024-08-01T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-08-01T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-4',
    code: 'CS301',
    name: 'Cơ sở dữ liệu',
    nameEn: 'Database Systems',
    credits: 3,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-1',
    semesterName: 'HK1 2025-2026',
    prerequisites: [
      {
        courseId: 'course-2',
        courseCode: 'CS201',
        courseName: 'Cấu trúc dữ liệu và Giải thuật',
        type: 'required',
      },
      {
        courseId: 'course-3',
        courseCode: 'CS202',
        courseName: 'Toán rời rạc',
        type: 'recommended', // Khuyến nghị học nhưng không bắt buộc
      },
    ],
    createdAt: '2024-09-01T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-09-01T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-5',
    code: 'CS302',
    name: 'Mạng máy tính',
    nameEn: 'Computer Networks',
    credits: 3,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-2',
    semesterName: 'HK2 2024-2025',
    prerequisites: [
      {
        courseId: 'course-1',
        courseCode: 'CS101',
        courseName: 'Lập trình căn bản',
        type: 'required',
      },
    ],
    createdAt: '2024-08-20T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-08-20T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-6',
    code: 'CS401',
    name: 'Hệ điều hành',
    nameEn: 'Operating Systems',
    credits: 3,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-1',
    semesterName: 'HK1 2025-2026',
    prerequisites: [
      {
        courseId: 'course-2',
        courseCode: 'CS201',
        courseName: 'Cấu trúc dữ liệu và Giải thuật',
        type: 'required',
      },
    ],
    createdAt: '2024-09-05T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-09-05T00:00:00Z',
    isActive: true,
  },
  {
    id: 'course-7',
    code: 'CS501',
    name: 'Trí tuệ nhân tạo',
    nameEn: 'Artificial Intelligence',
    credits: 4,
    departmentId: 'dept-1',
    departmentName: 'Khoa Công nghệ Thông tin',
    facultyId: 'faculty-1',
    facultyName: 'Khoa CNTT',
    semesterId: 'sem-2',
    semesterName: 'HK2 2025-2026',
    prerequisites: [
      {
        courseId: 'course-2',
        courseCode: 'CS201',
        courseName: 'Cấu trúc dữ liệu và Giải thuật',
        type: 'required',
      },
      {
        courseId: 'course-3',
        courseCode: 'CS202',
        courseName: 'Toán rời rạc',
        type: 'required',
      },
    ],
    createdAt: '2024-09-10T00:00:00Z',
    createdBy: 'aa-user-1',
    updatedAt: '2024-09-10T00:00:00Z',
    isActive: true,
  },
];

export const courseService = {
  // Get all courses with filters
  getCourses: async (filters?: CourseFilters): Promise<Course[]> => {
    await delay(300);

    let filtered = [...mockCourses];

    if (filters?.departmentId) {
      filtered = filtered.filter((c) => c.departmentId === filters.departmentId);
    }

    if (filters?.facultyId) {
      filtered = filtered.filter((c) => c.facultyId === filters.facultyId);
    }

    if (filters?.semesterId) {
      filtered = filtered.filter((c) => c.semesterId === filters.semesterId);
    }

    if (filters?.isActive !== undefined) {
      filtered = filtered.filter((c) => c.isActive === filters.isActive);
    }

    if (filters?.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (c) =>
          c.code.toLowerCase().includes(searchLower) ||
          c.name.toLowerCase().includes(searchLower) ||
          c.nameEn.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  },

  // Get course by ID
  getCourseById: async (id: string): Promise<Course> => {
    await delay(200);
    const course = mockCourses.find((c) => c.id === id);
    if (!course) throw new Error('Course not found');
    return course;
  },

  // Get course by code
  getCourseByCode: async (code: string): Promise<Course | null> => {
    await delay(200);
    const course = mockCourses.find((c) => c.code === code);
    return course || null;
  },

  // Create course (AA only)
  createCourse: async (data: Omit<Course, 'id' | 'createdAt' | 'updatedAt'>): Promise<Course> => {
    await delay(500);

    const newCourse: Course = {
      ...data,
      id: `course-${Date.now()}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockCourses.push(newCourse);
    return newCourse;
  },

  // Update course (AA only)
  updateCourse: async (id: string, data: Partial<Course>): Promise<Course> => {
    await delay(500);

    const index = mockCourses.findIndex((c) => c.id === id);
    if (index === -1) throw new Error('Course not found');

    const updated = {
      ...mockCourses[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };

    mockCourses[index] = updated;
    return updated;
  },

  // Delete course (AA only)
  deleteCourse: async (id: string): Promise<void> => {
    await delay(300);

    const index = mockCourses.findIndex((c) => c.id === id);
    if (index === -1) throw new Error('Course not found');

    mockCourses.splice(index, 1);
  },

  // Update prerequisites (AA only)
  updatePrerequisites: async (
    courseId: string,
    prerequisites: CoursePrerequisite[]
  ): Promise<Course> => {
    await delay(400);

    const index = mockCourses.findIndex((c) => c.id === courseId);
    if (index === -1) throw new Error('Course not found');

    mockCourses[index].prerequisites = prerequisites;
    mockCourses[index].updatedAt = new Date().toISOString();

    return mockCourses[index];
  },

  // Toggle active status
  toggleCourseStatus: async (id: string): Promise<Course> => {
    await delay(300);

    const index = mockCourses.findIndex((c) => c.id === id);
    if (index === -1) throw new Error('Course not found');

    mockCourses[index].isActive = !mockCourses[index].isActive;
    mockCourses[index].updatedAt = new Date().toISOString();

    return mockCourses[index];
  },
};
