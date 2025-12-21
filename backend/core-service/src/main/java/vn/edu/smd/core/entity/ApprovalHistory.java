package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.smd.shared.enums.ActorRoleType;
import vn.edu.smd.shared.enums.DecisionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Approval History Entity
 * Maps to table: approval_history
 */
@Entity
@Table(name = "approval_history", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_version_id", nullable = false)
    private SyllabusVersion syllabusVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private DecisionType action;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "step_number")
    private Integer stepNumber;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", length = 20)
    private ActorRoleType actorRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
