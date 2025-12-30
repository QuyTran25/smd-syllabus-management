package vn.edu.smd.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.ComponentType;
import vn.edu.smd.shared.enums.CourseType;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Syllabus Version Entity
 * Maps to table: syllabus_versions
 */
@Entity
@Table(
    name = "syllabus_versions",
    schema = "core_service",
    indexes = {
        @Index(name = "idx_syllabus_subject", columnList = "subject_id"),
        @Index(name = "idx_syllabus_term", columnList = "academic_term_id"),
        @Index(name = "idx_syllabus_previous", columnList = "previous_version_id"),
        @Index(name = "idx_syllabus_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_term_id")
    private AcademicTerm academicTerm;

    @Column(name = "version_no", nullable = false, length = 20)
    private String versionNo;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "core_service.syllabus_status")
    @Builder.Default
    private SyllabusStatus status = SyllabusStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_version_id")
    private SyllabusVersion previousVersion;

    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;

    // Snapshot fields
    @Column(name = "snap_subject_code", nullable = false, length = 20)
    private String snapSubjectCode;

    @Column(name = "snap_subject_name_vi", nullable = false, length = 255)
    private String snapSubjectNameVi;

    @Column(name = "snap_subject_name_en", length = 255)
    private String snapSubjectNameEn;

    @Column(name = "snap_credit_count", nullable = false)
    private Integer snapCreditCount;

    @Column(name = "keywords", columnDefinition = "text[]")
    private String[] keywords;

    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private Map<String, Object> content;

    // V8 additions - Post-publication & Workflow
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "unpublished_at")
    private LocalDateTime unpublishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unpublished_by")
    private User unpublishedBy;

    @Column(name = "unpublish_reason", columnDefinition = "TEXT")
    private String unpublishReason;

    @Column(name = "is_edit_enabled", nullable = false)
    @Builder.Default
    private Boolean isEditEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edit_enabled_by")
    private User editEnabledBy;

    @Column(name = "edit_enabled_at")
    private LocalDateTime editEnabledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private ApprovalWorkflow workflow;

    @Column(name = "current_approval_step")
    @Builder.Default
    private Integer currentApprovalStep = 0;

    // V8 additions - Frontend Detail Fields
    @Convert(converter = vn.edu.smd.core.converter.CourseTypeConverter.class)
    @Column(name = "course_type", length = 20)
    @Builder.Default
    private CourseType courseType = CourseType.REQUIRED;

    @Convert(converter = vn.edu.smd.core.converter.ComponentTypeConverter.class)
    @Column(name = "component_type", length = 20)
    @Builder.Default
    private ComponentType componentType = ComponentType.MAJOR;

    @Column(name = "theory_hours")
    @Builder.Default
    private Integer theoryHours = 0;

    @Column(name = "practice_hours")
    @Builder.Default
    private Integer practiceHours = 0;

    @Column(name = "self_study_hours")
    @Builder.Default
    private Integer selfStudyHours = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grading_scale_id")
    private GradingScale gradingScale;

    @Column(name = "student_duties", columnDefinition = "TEXT")
    private String studentDuties;

    // V10 additions
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "student_tasks", columnDefinition = "TEXT")
    private String studentTasks;

    // Approval workflow tracking
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "hod_approved_at")
    private LocalDateTime hodApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_approved_by")
    private User hodApprovedBy;

    @Column(name = "aa_approved_at")
    private LocalDateTime aaApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aa_approved_by")
    private User aaApprovedBy;

    @Column(name = "principal_approved_at")
    private LocalDateTime principalApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principal_approved_by")
    private User principalApprovedBy;

    // Audit fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
