import { Semester, SemesterFilters } from '@/types';

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Mock semesters data
export const mockSemesters: Semester[] = [
  {
    id: 'sem-1',
    code: 'HK1-2024',
    name: 'Học kỳ 1 năm 2024-2025',
    startDate: '2024-09-01',
    endDate: '2024-12-31',
    academicYear: '2024-2025',
    isActive: false,
    createdAt: '2024-06-01T00:00:00Z',
    createdBy: 'admin-1',
    updatedAt: '2024-06-01T00:00:00Z',
  },
  {
    id: 'sem-2',
    code: 'HK2-2024',
    name: 'Học kỳ 2 năm 2024-2025',
    startDate: '2025-01-01',
    endDate: '2025-05-31',
    academicYear: '2024-2025',
    isActive: true, // Current semester
    createdAt: '2024-06-01T00:00:00Z',
    createdBy: 'admin-1',
    updatedAt: '2024-09-01T00:00:00Z',
  },
  {
    id: 'sem-3',
    code: 'HK1-2025',
    name: 'Học kỳ 1 năm 2025-2026',
    startDate: '2025-09-01',
    endDate: '2025-12-31',
    academicYear: '2025-2026',
    isActive: false,
    createdAt: '2024-11-01T00:00:00Z',
    createdBy: 'admin-1',
    updatedAt: '2024-11-01T00:00:00Z',
  },
  {
    id: 'sem-4',
    code: 'HK2-2025',
    name: 'Học kỳ 2 năm 2025-2026',
    startDate: '2026-01-01',
    endDate: '2026-05-31',
    academicYear: '2025-2026',
    isActive: false,
    createdAt: '2024-11-01T00:00:00Z',
    createdBy: 'admin-1',
    updatedAt: '2024-11-01T00:00:00Z',
  },
];

export const semesterService = {
  // Get all semesters
  getSemesters: async (filters?: SemesterFilters): Promise<Semester[]> => {
    await delay(300);

    let filtered = [...mockSemesters];

    if (filters?.isActive !== undefined) {
      filtered = filtered.filter((s) => s.isActive === filters.isActive);
    }

    if (filters?.academicYear) {
      filtered = filtered.filter((s) => s.academicYear === filters.academicYear);
    }

    if (filters?.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (s) =>
          s.code.toLowerCase().includes(searchLower) ||
          s.name.toLowerCase().includes(searchLower)
      );
    }

    return filtered.sort((a, b) => 
      new Date(b.startDate).getTime() - new Date(a.startDate).getTime()
    );
  },

  // Get semester by ID
  getSemesterById: async (id: string): Promise<Semester> => {
    await delay(200);
    const semester = mockSemesters.find((s) => s.id === id);
    if (!semester) throw new Error('Semester not found');
    return semester;
  },

  // Get active semester
  getActiveSemester: async (): Promise<Semester | null> => {
    await delay(200);
    return mockSemesters.find((s) => s.isActive) || null;
  },

  // Create semester
  createSemester: async (data: Omit<Semester, 'id' | 'createdAt' | 'updatedAt'>): Promise<Semester> => {
    await delay(500);

    const newSemester: Semester = {
      ...data,
      id: `sem-${Date.now()}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockSemesters.push(newSemester);
    return newSemester;
  },

  // Update semester
  updateSemester: async (id: string, data: Partial<Semester>): Promise<Semester> => {
    await delay(500);

    const index = mockSemesters.findIndex((s) => s.id === id);
    if (index === -1) throw new Error('Semester not found');

    const updated = {
      ...mockSemesters[index],
      ...data,
      updatedAt: new Date().toISOString(),
    };

    mockSemesters[index] = updated;
    return updated;
  },

  // Delete semester
  deleteSemester: async (id: string): Promise<void> => {
    await delay(300);

    const index = mockSemesters.findIndex((s) => s.id === id);
    if (index === -1) throw new Error('Semester not found');

    // Check if semester is in use
    const semester = mockSemesters[index];
    if (semester.isActive) {
      throw new Error('Cannot delete active semester');
    }

    mockSemesters.splice(index, 1);
  },

  // Set active semester (only one can be active at a time)
  setActiveSemester: async (id: string): Promise<Semester> => {
    await delay(400);

    const index = mockSemesters.findIndex((s) => s.id === id);
    if (index === -1) throw new Error('Semester not found');

    // Deactivate all others
    mockSemesters.forEach((s) => {
      s.isActive = false;
    });

    // Activate this one
    mockSemesters[index].isActive = true;
    mockSemesters[index].updatedAt = new Date().toISOString();

    return mockSemesters[index];
  },
};
