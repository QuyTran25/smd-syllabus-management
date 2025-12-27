package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Role Entity
 * Maps to table: roles
 */
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

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;
}
