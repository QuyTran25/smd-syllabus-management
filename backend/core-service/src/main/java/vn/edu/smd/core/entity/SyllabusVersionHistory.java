package vn.edu.smd.core.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import vn.edu.smd.shared.enums.ComponentType;
import vn.edu.smd.shared.enums.CourseType;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Syllabus Version History Entity
 * Stores snapshots of syllabus versions for comparison and rollback
 * Maps to table: syllabus_version_history
 */
@Entity
@Table(
    name = "syllabus_version_history",
    schema = "core_service",
    indexes = {
        @Index(name = "idx_history_syllabus_id", columnList = "syllabus_id"),
        @Index(name = "idx_history_version_number", columnList = "version_number"),
        @Index(name = "idx_history_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusVersionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private SyllabusVersion syllabusVersion;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_no", nullable = false, length = 20)
    private String versionNo;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "core_service.syllabus_status")
    private SyllabusStatus status;

    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private Map<String, Object> content;

    @Column(name = "keywords", columnDefinition = "text[]")
    private String[] keywords;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "student_tasks", columnDefinition = "TEXT")
    private String studentTasks;

    @Column(name = "student_duties", columnDefinition = "TEXT")
    private String studentDuties;

    // Snapshot fields
    @Column(name = "snap_subject_code", length = 20)
    private String snapSubjectCode;

    @Column(name = "snap_subject_name_vi", length = 255)
    private String snapSubjectNameVi;

    @Column(name = "snap_subject_name_en", length = 255)
    private String snapSubjectNameEn;

    @Column(name = "snap_credit_count")
    private Integer snapCreditCount;

    @Convert(converter = vn.edu.smd.core.converter.CourseTypeConverter.class)
    @Column(name = "course_type", length = 20)
    private CourseType courseType;

    @Convert(converter = vn.edu.smd.core.converter.ComponentTypeConverter.class)
    @Column(name = "component_type", length = 20)
    private ComponentType componentType;

    @Column(name = "theory_hours")
    private Integer theoryHours;

    @Column(name = "practice_hours")
    private Integer practiceHours;

    @Column(name = "self_study_hours")
    private Integer selfStudyHours;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "snapshot_reason", length = 100)
    private String snapshotReason;
}
