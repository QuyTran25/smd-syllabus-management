package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Assessment-CLO Mapping Entity
 * Maps to table: assessment_clo_mappings
 */
@Entity
@Table(
    name = "assessment_clo_mappings",
    schema = "core_service",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_assessment_clo", columnNames = {"assessment_scheme_id", "clo_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentCloMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_scheme_id", nullable = false)
    private AssessmentScheme assessmentScheme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_id", nullable = false)
    private CLO clo;

    @Column(name = "contribution_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal contributionPercent = new BigDecimal("100.00");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
