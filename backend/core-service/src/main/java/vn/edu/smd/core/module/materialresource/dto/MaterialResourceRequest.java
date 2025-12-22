package vn.edu.smd.core.module.materialresource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MaterialResourceRequest {
    
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;
    
    @NotBlank(message = "Resource type is required")
    private String resourceType;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String author;
    
    private String publisher;
    
    private Integer year;
    
    private String url;
    
    private Boolean isRequired;
}
