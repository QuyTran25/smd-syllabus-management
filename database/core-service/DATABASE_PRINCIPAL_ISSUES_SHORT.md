# Checklist CSDL - Hiệu trưởng

**Cần chỉnh:**

1. ENUM `syllabus_status`:
   - Thêm: `APPROVED`, `REVISION_IN_PROGRESS`, `PENDING_HOD_REVISION`, `PENDING_ADMIN_REPUBLISH`
2. Bảng `subjects`, `syllabus_versions`, `clos`, `academic_terms`:
   - Xem checklist Admin (các cột giống Admin, giá trị cụ thể xem checklist Admin)
3. Bảng `approval_history`:
   - Thêm: `actor_role` (HOD, AA, PRINCIPAL, ADMIN)

**Các vấn đề khác:**
- Dashboard, quản lý đề cương: giống Admin
- Xem chi tiết: DATABASE_PRINCIPAL_ISSUES.md, DATABASE_ADMIN_ISSUES.md