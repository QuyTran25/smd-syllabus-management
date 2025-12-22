package vn.edu.smd.core.module.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AssessmentRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    private UUID parentId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Weight percent is required")
    private BigDecimal weightPercent;
}
