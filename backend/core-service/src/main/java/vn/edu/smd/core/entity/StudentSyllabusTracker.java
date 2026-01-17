package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_syllabus_tracker")
@Data
public class StudentSyllabusTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // --- MAPPING CHÍNH (Để lưu dữ liệu và lấy thông tin User) ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student; 

    // --- MAPPING PHỤ (Để sửa lỗi Repository findByStudentId) ---
    // insertable = false, updatable = false: Để tránh xung đột với biến 'student' ở trên
    @Column(name = "student_id", insertable = false, updatable = false)
    private UUID studentId;

    @Column(name = "syllabus_id", nullable = false)
    private UUID syllabusId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- HÀM SETTER TÙY CHỈNH ---
    // Khi Service gọi setStudentId(uuid), ta phải set luôn cả Object User để Hibernate lưu được xuống DB
    public void setStudentId(UUID studentId) {
        this.studentId = studentId; // Cập nhật biến phụ
        if (studentId != null) {
            // Cập nhật biến chính (quan trọng để lưu DB)
            this.student = User.builder().id(studentId).build();
        }
    }
}