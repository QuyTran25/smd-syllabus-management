package vn.edu.smd.core.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_change_requests", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "requested_role", nullable = false, length = 50)
    private String requestedRole;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, APPROVED, DENIED

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "handled_by")
    private UUID handledBy;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
