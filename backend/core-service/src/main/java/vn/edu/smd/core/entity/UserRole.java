package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.smd.shared.enums.RoleScope;
import java.util.UUID;

@Entity
@Table(name = "user_roles", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type")
    private RoleScope scopeType;

    @Column(name = "scope_id")
    private UUID scopeId; // ✅ Bổ sung trường này
}