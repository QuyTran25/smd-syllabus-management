package vn.edu.smd.core.module.collaborationchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CollaborationChangeRequest {
    @NotNull(message = "Collaboration session ID is required")
    private UUID collaborationSessionId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Field name is required")
    @Size(max = 100, message = "Field name must not exceed 100 characters")
    private String fieldName;

    private String oldValue;

    private String newValue;

    @Size(max = 20, message = "Change type must not exceed 20 characters")
    private String changeType;
}
