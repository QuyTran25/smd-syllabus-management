package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "syllabus_clos", schema = "core_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SyllabusCLO {
    @Id
    private UUID id;

    private String code;
    private String description;

    @Column(name = "bloom_level")
    private String bloomLevel;

    private Integer weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clo_plo_mapping", schema = "core_service",
        joinColumns = @JoinColumn(name = "clo_id"),
        inverseJoinColumns = @JoinColumn(name = "plo_id")
    )
    private List<PLO> plos;
}