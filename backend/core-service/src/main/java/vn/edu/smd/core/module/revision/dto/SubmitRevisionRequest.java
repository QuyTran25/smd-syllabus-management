package vn.edu.smd.core.module.revision.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request to submit revision for HOD approval
 */
@Data
public class SubmitRevisionRequest {
    
    @NotNull(message = "Revision session ID is required")
    private UUID revisionSessionId;
    
    private String summary;
}
