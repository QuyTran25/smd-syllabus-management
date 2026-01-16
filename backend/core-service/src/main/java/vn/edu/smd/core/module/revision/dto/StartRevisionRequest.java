package vn.edu.smd.core.module.revision.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request to start a revision session
 */
@Data
public class StartRevisionRequest {
    
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;
    
    @NotEmpty(message = "At least one feedback must be selected")
    private List<UUID> feedbackIds;
    
    private String description;
}
