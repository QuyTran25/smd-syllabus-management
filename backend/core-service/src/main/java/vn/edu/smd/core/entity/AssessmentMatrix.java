package vn.edu.smd.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

@Entity
@Table(name = "assessment_matrix", schema = "core_service")
@Getter 
@Setter
public class AssessmentMatrix {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne 
    @JoinColumn(name = "syllabus_id")
    @JsonIgnore
    private Syllabus syllabus;

    private String method;
    private String form;
    private String criteria;
    private Integer weight;
}