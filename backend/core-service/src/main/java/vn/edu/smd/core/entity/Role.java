package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "roles", schema = "core_service")
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
    private Boolean isSystem = false;

    // ✅ BỔ SUNG HOẶC KIỂM TRA 2 TRƯỜNG NÀY
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false) // ✅ Chỉ định rõ name bằng snake_case
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false) // ✅ Chỉ định rõ name bằng snake_case
    private LocalDateTime updatedAt;
}