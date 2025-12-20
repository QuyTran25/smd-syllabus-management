. Hiệu trưởng & Admin	
- Thêm syllabus_status: APPROVED, REVISION...


- Bảng approval_history: Thêm actor_role


- Bảng users: is_active, last_login, created_by


- Bảng audit_logs: description, status


2. Phòng Đào tạo (AA)	
- Bảng subjects: subject_type, component, recommended_term, description


- Quan trọng: default_theory_hours, default_practice_hours... (Kèm logic đổi tên cột cũ để giữ dữ liệu)


- Bảng academic_terms: academic_year


3. HOD & Giảng viên	
- Bảng syllabus_versions: description, objectives, student_tasks


- Bảng clos: weight


- Phân công: comments chung (bảng cha) & comments riêng (bảng con cộng tác viên)



4. Sinh viên	
- Bảng syllabus_error_reports: title, edit_enabled, admin_response, resolved_by/at


- Quan trọng: Chuẩn hóa Enum section (subject_info, clo...) kèm logic convert dữ liệu cũ.



5. Hệ thống (System)	
- Indexes bổ sung để tối ưu tốc độ.


- Cập nhật View v_syllabus_full để map đúng các cột mới.