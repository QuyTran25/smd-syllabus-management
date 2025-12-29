package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.core.config.ErrorReportSectionConverter;
import vn.edu.smd.shared.enums.ErrorReportSection;
import vn.edu.smd.shared.enums.FeedbackType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Syllabus Error Report Entity
 * Maps to table: syllabus_error_reports
 */
@Entity
@Table(name = "syllabus_error_reports", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusErrorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_version_id", nullable = false)
    private SyllabusVersion syllabusVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    @Builder.Default
    private FeedbackType type = FeedbackType.ERROR;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Convert(converter = ErrorReportSectionConverter.class)
    @Column(name = "section")
    @Builder.Default
    private ErrorReportSection section = ErrorReportSection.OTHER;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "edit_enabled")
    @Builder.Default
    private Boolean editEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
