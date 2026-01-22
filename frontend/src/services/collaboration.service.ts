import { apiClient } from '@/config/api-config';

export interface CollaborationRole {
  EDITOR: 'EDITOR';
  VIEWER: 'VIEWER';
}

export interface CollaboratorDTO {
  id: string;
  syllabusVersionId: string;
  userId: string;
  userName: string;
  userEmail: string;
  role: 'EDITOR' | 'VIEWER';
  assignedAt: string;
  assignedBy: string;
  assignedByName: string;
  isActive: boolean;
}

export const collaborationService = {
  // Get all collaborators for a syllabus
  getCollaboratorsBySyllabus: async (syllabusVersionId: string): Promise<CollaboratorDTO[]> => {
    const response = await apiClient.get<{ data: CollaboratorDTO[] }>(
      `/collaboration-sessions/syllabus/${syllabusVersionId}`
    );
    return response.data.data;
  },

  // Get all collaborations where user is a collaborator
  getMyCollaborations: async (userId: string): Promise<CollaboratorDTO[]> => {
    const response = await apiClient.get<{ data: CollaboratorDTO[] }>(
      `/collaboration-sessions/user/${userId}`
    );
    return response.data.data;
  },

  // Add collaborator to syllabus
  addCollaborator: async (syllabusVersionId: string, userId: string, role: 'EDITOR' | 'VIEWER'): Promise<CollaboratorDTO> => {
    const response = await apiClient.post<{ data: CollaboratorDTO }>('/collaboration-sessions', {
      syllabusVersionId,
      userId,
      role,
    });
    return response.data.data;
  },

  // Remove collaborator
  removeCollaborator: async (collaborationId: string): Promise<void> => {
    await apiClient.delete(`/collaboration-sessions/${collaborationId}`);
  },

  // Update collaborator role
  updateCollaborator: async (collaborationId: string, role: 'EDITOR' | 'VIEWER'): Promise<CollaboratorDTO> => {
    const response = await apiClient.put<{ data: CollaboratorDTO }>(
      `/collaboration-sessions/${collaborationId}`,
      { role }
    );
    return response.data.data;
  },
};
