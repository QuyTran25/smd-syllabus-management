import { User, UserRole } from '@/types';
import { mockUsers } from '@/mock';

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const userService = {
  // Get all users with filters
  getUsers: async (filters?: {
    role?: UserRole;
    isActive?: boolean;
    search?: string;
  }): Promise<User[]> => {
    await delay(300);

    let filtered = [...mockUsers];

    if (filters?.role) {
      filtered = filtered.filter((u) => u.role === filters.role);
    }

    if (filters?.isActive !== undefined) {
      filtered = filtered.filter((u) => u.isActive === filters.isActive);
    }

    if (filters?.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(
        (u) =>
          u.fullName.toLowerCase().includes(searchLower) ||
          u.email.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  },

  // Get user by ID
  getUserById: async (id: string): Promise<User> => {
    await delay(200);
    const user = mockUsers.find((u) => u.id === id);
    if (!user) throw new Error('User not found');
    return user;
  },

  // Create user
  createUser: async (data: Omit<User, 'id' | 'createdAt' | 'lastLogin'>): Promise<User> => {
    await delay(500);

    // Auto-populate managerName if managerId is provided
    let managerName: string | undefined;
    if (data.managerId) {
      const manager = mockUsers.find(u => u.id === data.managerId);
      managerName = manager?.fullName;
    }

    const newUser: User = {
      ...data,
      managerName,
      id: `user-${Date.now()}`,
      createdAt: new Date().toISOString(),
    };

    mockUsers.push(newUser);
    return newUser;
  },

  // Update user
  updateUser: async (id: string, data: Partial<User>): Promise<User> => {
    await delay(500);

    const index = mockUsers.findIndex((u) => u.id === id);
    if (index === -1) throw new Error('User not found');

    // Auto-populate managerName if managerId is provided
    let managerName = data.managerName;
    if (data.managerId) {
      const manager = mockUsers.find(u => u.id === data.managerId);
      managerName = manager?.fullName;
    }

    const updated = { ...mockUsers[index], ...data, managerName };
    mockUsers[index] = updated;
    return updated;
  },

  // Delete user
  deleteUser: async (id: string): Promise<void> => {
    await delay(300);

    const index = mockUsers.findIndex((u) => u.id === id);
    if (index === -1) throw new Error('User not found');

    mockUsers.splice(index, 1);
  },

  // Toggle user status (lock/unlock)
  toggleUserStatus: async (id: string): Promise<User> => {
    await delay(300);

    const index = mockUsers.findIndex((u) => u.id === id);
    if (index === -1) throw new Error('User not found');

    mockUsers[index].isActive = !mockUsers[index].isActive;
    return mockUsers[index];
  },

  // Bulk import users from CSV
  importUsers: async (file: File): Promise<{ success: number; failed: number; errors: string[] }> => {
    await delay(1500);

    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      const errors: string[] = [];
      let successCount = 0;
      let failedCount = 0;

      reader.onload = (e) => {
        try {
          const text = e.target?.result as string;
          const lines = text.split('\n').filter((line) => line.trim());

          if (lines.length === 0) {
            reject(new Error('File CSV rỗng'));
            return;
          }

          // Parse header
          const header = lines[0].split(',').map((h) => h.trim().toLowerCase());
          const requiredFields = ['email', 'fullname', 'role'];
          
          // Validate header
          const missingFields = requiredFields.filter((field) => !header.includes(field));
          if (missingFields.length > 0) {
            reject(new Error(`Thiếu các cột: ${missingFields.join(', ')}`));
            return;
          }

          // Parse data rows
          for (let i = 1; i < lines.length; i++) {
            const values = lines[i].split(',').map((v) => v.trim());
            
            if (values.length !== header.length) {
              errors.push(`Dòng ${i + 1}: Số cột không khớp với header`);
              failedCount++;
              continue;
            }

            const row: any = {};
            header.forEach((key, index) => {
              row[key] = values[index];
            });

            // Validate required fields
            if (!row.email || !row.fullname || !row.role) {
              errors.push(`Dòng ${i + 1}: Thiếu thông tin bắt buộc`);
              failedCount++;
              continue;
            }

            // Validate email format
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(row.email)) {
              errors.push(`Dòng ${i + 1}: Email không hợp lệ (${row.email})`);
              failedCount++;
              continue;
            }

            // Validate role
            const validRoles = ['ADMIN', 'HOD', 'AA', 'PRINCIPAL', 'LECTURER', 'STUDENT'];
            if (!validRoles.includes(row.role.toUpperCase())) {
              errors.push(`Dòng ${i + 1}: Role không hợp lệ (${row.role})`);
              failedCount++;
              continue;
            }

            // Check duplicate email
            if (mockUsers.find((u) => u.email.toLowerCase() === row.email.toLowerCase())) {
              errors.push(`Dòng ${i + 1}: Email đã tồn tại (${row.email})`);
              failedCount++;
              continue;
            }

            // Create user
            try {
              const newUser: User = {
                id: `user-${Date.now()}-${i}`,
                email: row.email,
                fullName: row.fullname,
                role: row.role.toUpperCase() as UserRole,
                isActive: true,
                createdAt: new Date().toISOString(),
              };

              mockUsers.push(newUser);
              successCount++;
            } catch (error) {
              errors.push(`Dòng ${i + 1}: Lỗi tạo user`);
              failedCount++;
            }
          }

          resolve({
            success: successCount,
            failed: failedCount,
            errors: errors.slice(0, 10), // Limit to first 10 errors
          });
        } catch (error) {
          reject(new Error('Lỗi parse file CSV'));
        }
      };

      reader.onerror = () => {
        reject(new Error('Lỗi đọc file'));
      };

      reader.readAsText(file);
    });
  },
};
