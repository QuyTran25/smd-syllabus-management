package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subject Relationship Entity
 * Maps to table: subject_relationships
 * Represents PREREQUISITE, CO_REQUISITE, or REPLACEMENT relationships
 */
@Entity
@Table(
    name = "subject_relationships",
    schema = "core_service",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subject_id", "related_subject_id", "type"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_subject_id", nullable = false)
    private Subject relatedSubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SubjectRelationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
