package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.MappingLevel;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CLO-PI Mapping Entity
 * Maps to table: clo_pi_mappings
 */
@Entity
@Table(
    name = "clo_pi_mappings",
    schema = "core_service",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_clo_pi", columnNames = {"clo_id", "pi_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloPiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_id", nullable = false)
    private CLO clo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pi_id", nullable = false)
    private PerformanceIndicator pi;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 1)
    private MappingLevel level;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
