package vn.edu.smd.core.module.subjectcomponent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class SubjectComponentRequest {
    
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;
    
    @NotBlank(message = "Component type is required")
    private String componentType;
    
    @NotNull(message = "Hours is required")
    @Min(value = 0, message = "Hours must be non-negative")
    private Integer hours;
    
    private String description;
    
    private Integer displayOrder;
}
