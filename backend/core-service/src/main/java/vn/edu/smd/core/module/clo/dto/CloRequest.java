package vn.edu.smd.core.module.clo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CloRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 50, message = "Bloom level must not exceed 50 characters")
    private String bloomLevel;

    private BigDecimal weight;
}
