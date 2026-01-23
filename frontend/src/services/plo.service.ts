import { apiClient } from '@/config/api-config';

// This interface should match the PloResponse DTO from the backend
export interface PLO {
  id: string;
  code: string;
  description: string;
  category: 'KNOWLEDGE' | 'SKILLS' | 'COMPETENCE' | 'ATTITUDE';
  subjectId: string;
  subjectCode: string;
  subjectName: string;
  createdAt?: string;
  updatedAt?: string;
}

// This interface is for the list response
export interface PLOListResponse {
  success: boolean;
  message: string;
  data: PLO[];
}

// This interface is for a single PLO response (create/update)
export interface PLOSingleResponse {
  success: boolean;
  message: string;
  data: PLO;
}

export const ploService = {
  async getAllPLOs(): Promise<PLO[]> {
    try {
      const response = await apiClient.get<PLOListResponse>('/plos');
      return response.data.data || [];
    } catch (error) {
      console.error('Error fetching PLOs:', error);
      return [];
    }
  },

  async getPLOById(id: string): Promise<PLO | null> {
    try {
      const response = await apiClient.get<PLOSingleResponse>(`/plos/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching PLO:', error);
      return null;
    }
  },

  async getPLOsBySubject(subjectId: string): Promise<PLO[]> {
    try {
      const response = await apiClient.get<PLOListResponse>(`/plos/subject/${subjectId}`);
      return response.data.data || [];
    } catch (error) {
      console.error('Error fetching PLOs by subject:', error);
      return [];
    }
  },

  async createPLO(data: Partial<PLO>): Promise<PLO> {
    const response = await apiClient.post<PLOSingleResponse>('/plos', data);
    return response.data.data;
  },

  async updatePLO(id: string, data: Partial<PLO>): Promise<PLO> {
    const response = await apiClient.put<PLOSingleResponse>(`/plos/${id}`, data);
    return response.data.data;
  },

  async deletePLO(id: string): Promise<void> {
    await apiClient.delete(`/plos/${id}`);
  },

  // Map category to Vietnamese display name
  getCategoryDisplayName(category: PLO['category']): string {
    const categoryMap: Record<PLO['category'], string> = {
      KNOWLEDGE: 'Kiến thức',
      SKILLS: 'Kỹ năng',
      COMPETENCE: 'Năng lực',
      ATTITUDE: 'Thái độ',
    };
    return categoryMap[category] || category;
  },
};
