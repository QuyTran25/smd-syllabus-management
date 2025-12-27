package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.core.converter.PloCategoryConverter;
import vn.edu.smd.shared.enums.PloCategory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Program Learning Outcome (PLO) Entity
 * Maps to table: plos
 */
@Entity
@Table(name = "plos", schema = "core_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PLO {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Convert(converter = PloCategoryConverter.class)
    @Column(name = "category", length = 20)
    @Builder.Default
    private PloCategory category = PloCategory.KNOWLEDGE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
