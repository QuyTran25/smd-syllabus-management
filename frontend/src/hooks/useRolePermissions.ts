/**
 * Custom hook for role-based permissions
 */

import { useMemo } from 'react';
import { useAuth } from './useAuth';
import { getAllowedStatuses, getStatusTabs, canViewStatus } from '../config/rolePermissions';
import { SyllabusStatus } from '../types/syllabus.types';

export function useRolePermissions() {
  const { user } = useAuth();
  
  const userRole = useMemo(() => {
    if (!user || !user.roles || user.roles.length === 0) {
      return null;
    }
    
    // Map backend roles to config keys
    const role = user.roles[0];
    const roleMap: Record<string, string> = {
      'Administrator': 'ADMIN',
      'Principal': 'PRINCIPAL',
      'Academic Affairs': 'AA',
      'Head of Department': 'HOD',
      'Lecturer': 'LECTURER'
    };
    
    return roleMap[role] || null;
  }, [user]);

  const allowedStatuses = useMemo(() => {
    if (!userRole) return [];
    return getAllowedStatuses(userRole);
  }, [userRole]);

  const statusTabs = useMemo(() => {
    if (!userRole) return [];
    return getStatusTabs(userRole);
  }, [userRole]);

  const canView = (status: SyllabusStatus): boolean => {
    if (!userRole) return false;
    return canViewStatus(userRole, status);
  };

  return {
    userRole,
    allowedStatuses,
    statusTabs,
    canView,
    isAdmin: userRole === 'ADMIN',
    isPrincipal: userRole === 'PRINCIPAL',
    isAA: userRole === 'AA',
    isHOD: userRole === 'HOD',
    isLecturer: userRole === 'LECTURER'
  };
}
