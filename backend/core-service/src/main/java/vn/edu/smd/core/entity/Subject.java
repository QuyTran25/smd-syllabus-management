package vn.edu.smd.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.SubjectComponent;
import vn.edu.smd.shared.enums.SubjectType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subjects", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    // --- FIX LAZY LOADING & JSON SERIALIZATION ---
    
    // 1. Map object quan hệ (để lưu dữ liệu và dùng trong code Java)
    // Dùng @JsonIgnore để khi trả về API, nó không cố load object này -> Tránh lỗi Lazy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnore 
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    @JsonIgnore
    private Curriculum curriculum;

    // 2. Map cột ID ra biến riêng (Read-only)
    // Frontend chỉ cần ID để hiển thị hoặc filter, không cần cả object Department
    @Column(name = "department_id", insertable = false, updatable = false)
    private UUID departmentId;

    @Column(name = "curriculum_id", insertable = false, updatable = false)
    private UUID curriculumId;

    // ---------------------------------------------

    @Column(name = "current_name_vi", nullable = false, length = 255)
    private String currentNameVi;

    @Column(name = "current_name_en", length = 255)
    private String currentNameEn;

    @Column(name = "default_credits", nullable = false)
    private Integer defaultCredits;

    // Helper method (giữ lại nếu code cũ đang gọi hàm này)
    public Integer getCredits() {
        return defaultCredits;
    }

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 20)
    @Builder.Default
    private SubjectType subjectType = SubjectType.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "component", length = 20)
    @Builder.Default
    private SubjectComponent component = SubjectComponent.BOTH;

    @Column(name = "default_theory_hours")
    @Builder.Default
    private Integer defaultTheoryHours = 0;

    @Column(name = "default_practice_hours")
    @Builder.Default
    private Integer defaultPracticeHours = 0;

    @Column(name = "default_self_study_hours")
    @Builder.Default
    private Integer defaultSelfStudyHours = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_term")
    private Integer recommendedTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
<<<<<<< HEAD
}
=======

    // Helper method for backward compatibility
    public String getName() {
        return this.currentNameVi;
    }
}
>>>>>>> origin/main
