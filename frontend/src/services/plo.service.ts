import { apiClient } from '@/config/api-config';

export interface PLO {
  id: string;
  code: string;
  description: string;
  category: 'KNOWLEDGE' | 'SKILLS' | 'COMPETENCE' | 'ATTITUDE';
  curriculumId: string;
  curriculumCode: string;
  curriculumName: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PLOResponse {
  success: boolean;
  message: string;
  data: PLO[];
}

export const ploService = {
  async getAllPLOs(): Promise<PLO[]> {
    try {
      const response = await apiClient.get<PLOResponse>('/api/plos');
      return response.data.data || [];
    } catch (error) {
      console.error('Error fetching PLOs:', error);
      return [];
    }
  },

  async getPLOById(id: string): Promise<PLO | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: PLO }>(
        `/api/plos/${id}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching PLO:', error);
      return null;
    }
  },

  async getPLOsByCurriculum(curriculumId: string): Promise<PLO[]> {
    try {
      const response = await apiClient.get<PLOResponse>(
        `/api/plos/curriculum/${curriculumId}`
      );
      return response.data.data || [];
    } catch (error) {
      console.error('Error fetching PLOs by curriculum:', error);
      return [];
    }
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

export default ploService;
