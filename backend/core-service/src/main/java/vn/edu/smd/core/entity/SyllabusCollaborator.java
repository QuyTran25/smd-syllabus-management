package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.smd.shared.enums.CollaboratorRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Syllabus Collaborator Entity
 * Maps to table: syllabus_collaborators
 */
@Entity
@Table(
    name = "syllabus_collaborators",
    schema = "core_service",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_syllabus_collaborator", columnNames = {"syllabus_version_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusCollaborator {

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
    @Column(name = "role", nullable = false, length = 20)
    private CollaboratorRole role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}
