package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * =========================================================================
 * Entity: Role
 * -------------------------------------------------------------------------
 * ANTI-CONFLICT STRATEGY:
 * - Khôi phục các trường Auditing (createdAt, updatedAt) để fix lỗi compile 
 * trong RoleService.
 * - Sử dụng JPA Auditing thay vì Hibernate Annotation để thống nhất với Core.
 * =========================================================================
 */
@Entity
@Table(name = "roles", schema = "core_service")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private String description;

    @Column(name = "is_system")
    @Builder.Default
    private Boolean isSystem = false;

    // FIX: Khôi phục các trường này để fix lỗi biên dịch Severity 8
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}