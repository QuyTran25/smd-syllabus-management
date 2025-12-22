package vn.edu.smd.core.module.collaboration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.smd.shared.enums.CollaboratorRole;

import java.util.UUID;

@Data
public class CollaborationRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private CollaboratorRole role;
}
