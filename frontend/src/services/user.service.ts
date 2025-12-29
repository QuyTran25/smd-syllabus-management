import axiosClient from '@/api/axiosClient';

export const userService = {
  // Lấy thông tin user đang đăng nhập
  getCurrentUser: async () => {
    const response = await axiosClient.get('/users/me');
    return response.data?.data ?? response.data;
  },

  // Lấy danh sách users (Admin)
  getUsers: async (params?: any) => {
    const response = await axiosClient.get('/users', { params });
    return response.data?.data ?? response.data;
  },
};
