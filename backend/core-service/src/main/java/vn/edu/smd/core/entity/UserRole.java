// package vn.edu.smd.core.entity;

// import jakarta.persistence.*;
// import lombok.*;
// import org.hibernate.annotations.CreationTimestamp;
// import vn.edu.smd.shared.enums.RoleScope;

// import java.time.LocalDateTime;
// import java.util.UUID;

// /**
//  * User Role Mapping Entity
//  * Maps to table: user_roles
//  * Represents user roles with scope (GLOBAL, FACULTY, DEPARTMENT)
//  */
// @Entity
// @Table(
//     name = "user_roles",
//     schema = "core_service",
//     uniqueConstraints = {
//         @UniqueConstraint(columnNames = {"user_id", "role_id", "scope_type", "scope_id"})
//     }
// )
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class UserRole {

//     @Id
//     @GeneratedValue(strategy = GenerationType.UUID)
//     private UUID id;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "user_id", nullable = false)
//     private User user;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "role_id", nullable = false)
//     private Role role;

//     @Enumerated(EnumType.STRING)
//     @Column(name = "scope_type", nullable = false, length = 20)
//     private RoleScope scopeType;

//     @Column(name = "scope_id")
//     private UUID scopeId;

//     @CreationTimestamp
//     @Column(name = "created_at", nullable = false, updatable = false)
//     private LocalDateTime createdAt;
// }



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