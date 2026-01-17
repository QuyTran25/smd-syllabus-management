package vn.edu.smd.core.module.revision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request for HOD to approve/reject revision
 */
@Data
public class ReviewRevisionRequest {
    
    @NotNull(message = "Revision session ID is required")
    private UUID revisionSessionId;
    
    @NotBlank(message = "Decision is required (APPROVED or REJECTED)")
    private String decision; // APPROVED or REJECTED
    
    private String comment;
}
