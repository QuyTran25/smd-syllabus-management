package vn.edu.smd.core.module.performanceindicator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PerformanceIndicatorRequest {
    @NotNull(message = "PLO ID is required")
    private UUID ploId;

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;
}
