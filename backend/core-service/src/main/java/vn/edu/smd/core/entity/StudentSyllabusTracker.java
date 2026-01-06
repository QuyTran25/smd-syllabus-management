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

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "syllabus_id", nullable = false)
    private UUID syllabusId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}