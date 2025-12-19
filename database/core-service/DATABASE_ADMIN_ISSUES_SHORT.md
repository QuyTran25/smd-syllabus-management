# Checklist CSDL - Admin

**Cần chỉnh:**

1. Bảng `subjects`:
   - Thêm: `subject_type` (REQUIRED, ELECTIVE)
   - Thêm: `component` (THEORY, PRACTICE, BOTH)
   - Thêm: `theory_hours`, `practice_hours`, `self_study_hours` (số nguyên)
2. Bảng `syllabus_versions`:
   - Thêm: `description`, `objectives`, `student_tasks`
3. Bảng `clos`:
   - Thêm: `weight` (số thực, 0-100)
4. Bảng `users`:
   - Thêm: `is_active` (boolean), `last_login` (timestamp), `created_by` (UUID)
5. Bảng `academic_terms`:
   - Thêm: `academic_year` (chuỗi, ví dụ: "2024-2025")
6. Bảng `syllabus_error_reports`:
   - Thêm: `title`, `edit_enabled` (boolean), `admin_response`, `resolved_by`, `resolved_at`
7. Bảng `audit_logs`:
   - Thêm: `description`, `status` (success, failed)
8. ENUM `syllabus_status`:
   - Thêm: `APPROVED`, `REVISION_IN_PROGRESS`, `PENDING_HOD_REVISION`, `PENDING_ADMIN_REPUBLISH`

**Các vấn đề khác:**
- Đã đủ: các trường cơ bản dashboard, lịch sử phê duyệt, thông báo
- Xem chi tiết: DATABASE_ADMIN_ISSUES.md, DATABASE_PRINCIPAL_ISSUES.md, DATABASE_AA_ISSUES.md
