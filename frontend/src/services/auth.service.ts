import { LoginCredentials, AuthResponse, User, UserRole } from '@/types';
import { apiClient } from '@/config/api-config';

// Map backend role name to frontend role code
const mapRoleToCode = (roleName: string): UserRole => {
  const roleMap: Record<string, UserRole> = {
    // English names
    Administrator: UserRole.ADMIN,
    Principal: UserRole.PRINCIPAL,
    'Academic Affairs': UserRole.AA,
    'Head of Department': UserRole.HOD,
    Lecturer: UserRole.LECTURER,
    Student: UserRole.STUDENT,
    // Code names
    ADMIN: UserRole.ADMIN,
    PRINCIPAL: UserRole.PRINCIPAL,
    AA: UserRole.AA,
    HOD: UserRole.HOD,
    LECTURER: UserRole.LECTURER,
    STUDENT: UserRole.STUDENT,
    // Vietnamese names (THÊM MỚI)
    'Quản trị viên': UserRole.ADMIN,
    'Hiệu trưởng': UserRole.PRINCIPAL,
    'Phòng Đào tạo': UserRole.AA,
    'Trưởng Bộ môn': UserRole.HOD,
    'Giảng viên': UserRole.LECTURER,
    'Sinh viên': UserRole.STUDENT,
    // Variations
    Admin: UserRole.ADMIN,
    'Head Of Department': UserRole.HOD,
    'Truong Bo mon': UserRole.HOD,
    'Phong Dao tao': UserRole.AA,
    'Hieu truong': UserRole.PRINCIPAL,
    'Giang vien': UserRole.LECTURER,
  };

  // Try exact match first
  if (roleMap[roleName]) {
    console.log('✅ [mapRoleToCode] Exact match:', roleName, '→', roleMap[roleName]);
    return roleMap[roleName];
  }

  // Try case-insensitive match
  const lowerRoleName = roleName.toLowerCase();
  for (const [key, value] of Object.entries(roleMap)) {
    if (key.toLowerCase() === lowerRoleName) {
      console.log('✅ [mapRoleToCode] Case-insensitive match:', roleName, '→', value);
      return value;
    }
  }

  // Try partial match for common keywords
  if (lowerRoleName.includes('admin') || lowerRoleName.includes('quản trị')) {
    console.log('⚠️ [mapRoleToCode] Partial match (admin):', roleName, '→', UserRole.ADMIN);
    return UserRole.ADMIN;
  }
  if (lowerRoleName.includes('principal') || lowerRoleName.includes('hiệu trưởng')) {
    console.log('⚠️ [mapRoleToCode] Partial match (principal):', roleName, '→', UserRole.PRINCIPAL);
    return UserRole.PRINCIPAL;
  }
  if (lowerRoleName.includes('head') || lowerRoleName.includes('trưởng bộ')) {
    console.log('⚠️ [mapRoleToCode] Partial match (hod):', roleName, '→', UserRole.HOD);
    return UserRole.HOD;
  }
  if (lowerRoleName.includes('academic') || lowerRoleName.includes('đào tạo')) {
    console.log('⚠️ [mapRoleToCode] Partial match (aa):', roleName, '→', UserRole.AA);
    return UserRole.AA;
  }
  if (lowerRoleName.includes('lecturer') || lowerRoleName.includes('giảng viên')) {
    console.log('⚠️ [mapRoleToCode] Partial match (lecturer):', roleName, '→', UserRole.LECTURER);
    return UserRole.LECTURER;
  }

  // Fallback to LECTURER
  console.error('❌ [mapRoleToCode] NO MATCH for role:', roleName, '→ Defaulting to LECTURER');
  return UserRole.LECTURER;
};

// Real authentication service using backend API
export const authService = {
  // Login
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    // 🟢 FIX: Đổi '/api/auth/login' -> '/auth/login' (Vì baseURL đã có /api)
    const response = await apiClient.post('/auth/login', credentials);
    // Backend returns accessToken, not token
    const { accessToken: token, refreshToken } = response.data.data;

    // Get user info using the token
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    // 🟢 FIX: Đổi '/api/auth/me' -> '/auth/me'
    const userResponse = await apiClient.get('/auth/me');
    const userInfo = userResponse.data.data;

    // Map backend user info to frontend User type
    const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
    const user: User = {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      role: mapRoleToCode(rawRole),
      phone: userInfo.phoneNumber,
      isActive: userInfo.status === 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return {
      token,
      refreshToken,
      user,
    };
  },

  // Logout
  logout: async (): Promise<void> => {
    // 🟢 FIX: Đổi '/api/auth/logout' -> '/auth/logout'
    await apiClient.post('/auth/logout');
    delete apiClient.defaults.headers.common['Authorization'];
  },

  // Verify token and get current user
  getCurrentUser: async (token: string): Promise<User> => {
    console.log('📡 [authService.getCurrentUser] Setting token and calling GET /api/auth/me');
    console.log('📡 [authService.getCurrentUser] Token preview:', token.substring(0, 30) + '...');

    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;

    try {
      // 🟢 FIX: Đổi '/api/auth/me' -> '/auth/me'
      const response = await apiClient.get('/auth/me');
      console.log('✅ [authService.getCurrentUser] GET /api/auth/me SUCCESS');
      console.log('✅ [authService.getCurrentUser] Response data:', response.data);

      const userInfo = response.data.data;

      // Map backend user info to frontend User type
      const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
      console.log(
        '🔍 [authService.getCurrentUser] Backend returned roles:',
        userInfo.roles,
        'First role:',
        rawRole
      );
      const mappedRole = mapRoleToCode(rawRole);
      console.log('✅ [authService.getCurrentUser] Mapped to frontend role:', mappedRole);

      const user: User = {
        id: userInfo.id,
        email: userInfo.email,
        fullName: userInfo.fullName,
        role: mappedRole,
        phone: userInfo.phoneNumber,
        isActive: userInfo.status === 'ACTIVE',
        createdAt: new Date().toISOString(),
      };

      return user;
    } catch (error) {
      console.error('❌ [authService.getCurrentUser] GET /auth/me FAILED:', error);
      throw error;
    }
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    // 🟢 FIX: Đổi '/api/auth/refresh-token' -> '/auth/refresh-token'
    const response = await apiClient.post('/auth/refresh-token', { refreshToken });
    const { accessToken: newToken, refreshToken: newRefreshToken } = response.data.data;

    // Get updated user info
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
    // 🟢 FIX: Đổi '/api/auth/me' -> '/auth/me'
    const userResponse = await apiClient.get('/auth/me');
    const userInfo = userResponse.data.data;

    // Map backend user info to frontend User type
    const rawRole = userInfo.roles && userInfo.roles.length > 0 ? userInfo.roles[0] : 'Lecturer';
    const user: User = {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      role: mapRoleToCode(rawRole),
      phone: userInfo.phoneNumber,
      isActive: userInfo.status === 'ACTIVE',
      createdAt: new Date().toISOString(),
    };

    return {
      token: newToken,
      refreshToken: newRefreshToken,
      user,
    };
  },
};
