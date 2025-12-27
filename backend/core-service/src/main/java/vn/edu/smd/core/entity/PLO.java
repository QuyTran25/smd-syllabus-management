package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.smd.shared.enums.PloCategory;
import vn.edu.smd.core.converter.PloCategoryConverter; // ⭐ Import từ core.converter

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plos", schema = "core_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PLO {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ⭐ SỬA LỖI: Dùng @Convert để Hibernate hiểu được chữ "Knowledge"
    @Convert(converter = PloCategoryConverter.class)
    @Column(name = "category")
    private PloCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}