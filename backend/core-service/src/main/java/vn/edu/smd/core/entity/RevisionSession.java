package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.RevisionSessionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Revision Session Entity
 * Tracks revision cycles for fixing published syllabi based on feedback
 * Maps to table: revision_sessions
 */
@Entity
@Table(
    name = "revision_sessions",
    schema = "core_service",
    indexes = {
        @Index(name = "idx_revision_session_syllabus", columnList = "syllabus_version_id"),
        @Index(name = "idx_revision_session_status", columnList = "status"),
        @Index(name = "idx_revision_session_initiated_at", columnList = "initiated_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_version_id", nullable = false)
    private SyllabusVersion syllabusVersion;

    @Column(name = "session_number", nullable = false)
    @Builder.Default
    private Integer sessionNumber = 1;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcType(org.hibernate.dialect.PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "core_service.revision_session_status")
    @Builder.Default
    private RevisionSessionStatus status = RevisionSessionStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    @Builder.Default
    private LocalDateTime initiatedAt = LocalDateTime.now();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_lecturer_id")
    private User assignedLecturer;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // HOD approval tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_reviewed_by")
    private User hodReviewedBy;

    @Column(name = "hod_reviewed_at")
    private LocalDateTime hodReviewedAt;

    @Column(name = "hod_decision", length = 20)
    private String hodDecision;

    @Column(name = "hod_comment", columnDefinition = "TEXT")
    private String hodComment;

    // Admin republish tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "republished_by")
    private User republishedBy;

    @Column(name = "republished_at")
    private LocalDateTime republishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship with feedback
    @OneToMany(mappedBy = "revisionSession", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SyllabusErrorReport> feedbacks = new ArrayList<>();
}
