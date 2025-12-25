export interface Subject {
  id: string;
  code: string;
  currentNameVi: string;
  currentNameEn: string;
  defaultCredits: number;
  departmentId: string; // Trường chúng ta vừa fix
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}