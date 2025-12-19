# Checklist CSDL - Phòng Đào Tạo (AA)

**Cần chỉnh:**

1. Bảng `subjects`:
   - Thêm: `description`
   - Thêm: `recommended_term` (số nguyên, 1-10)
   - Thêm: `subject_type` (giá trị: REQUIRED, ELECTIVE)
   - Thêm: `component` (giá trị: THEORY, PRACTICE, BOTH)
   - Thêm: `default_theory_hours`, `default_practice_hours`, `default_self_study_hours` (số nguyên)

**Các vấn đề khác:**
- Các trường workflow, trạng thái, các cột khác giống Admin/Hiệu trưởng
- Xem chi tiết: DATABASE_AA_ISSUES.md, DATABASE_ADMIN_ISSUES.md, DATABASE_PRINCIPAL_ISSUES.md
