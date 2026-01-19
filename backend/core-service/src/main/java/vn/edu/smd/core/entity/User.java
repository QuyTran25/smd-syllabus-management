package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.AuthProvider;
import vn.edu.smd.shared.enums.Gender;
import vn.edu.smd.shared.enums.RoleScope;
import vn.edu.smd.shared.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Entity
 * Maps to table: users
 */
@Entity
@Table(name = "users", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdByUser;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Column(name = "fcm_token_updated_at")
    private LocalDateTime fcmTokenUpdatedAt;

    // Helper methods for backward compatibility
    public String getPassword() {
        return this.passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public String getPhoneNumber() {
        return this.phone;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public Set<Role> getRoles() {
        return this.userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    public void setRoles(Set<Role> roles) {
        this.userRoles.clear();
        if (roles != null) {
            roles.forEach(role -> {
                UserRole userRole = new UserRole();
                userRole.setUser(this);
                userRole.setRole(role);
                userRole.setScopeType(RoleScope.GLOBAL);
                this.userRoles.add(userRole);
            });
        }
    }
}
