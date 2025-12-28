package vn.edu.smd.core.module.teachingmethod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.util.UUID;

@Data
public class TeachingMethodRequest {
    
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;
    
    @NotBlank(message = "Method name is required")
    private String methodName;
    
    private String description;
    
    @Min(value = 0, message = "Percentage must be between 0 and 100")
    @Max(value = 100, message = "Percentage must be between 0 and 100")
    private Integer percentage;
}
