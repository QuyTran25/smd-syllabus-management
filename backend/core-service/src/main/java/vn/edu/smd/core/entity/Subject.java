package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.SubjectComponent;
import vn.edu.smd.shared.enums.SubjectType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subject (Môn học) Entity
 * Maps to table: subjects
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;

    @Column(name = "current_name_vi", nullable = false, length = 255)
    private String currentNameVi;

    @Column(name = "current_name_en", length = 255)
    private String currentNameEn;

    @Column(name = "default_credits", nullable = false)
    private Integer defaultCredits;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // V10 additions
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
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method for backward compatibility
    public String getName() {
        return this.currentNameVi;
    }
}
