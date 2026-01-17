package vn.edu.smd.core.module.plo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.smd.shared.enums.PloCategory;

import java.util.UUID;

@Data
public class PloRequest {
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Description is required")
    private String description;

    private PloCategory category;
}
