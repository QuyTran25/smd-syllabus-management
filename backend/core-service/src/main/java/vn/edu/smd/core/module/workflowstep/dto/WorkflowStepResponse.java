package vn.edu.smd.core.module.workflowstep.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WorkflowStepResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private Integer stepOrder;
    private String stepName;
    private String approverRole;
    private UUID approverId;
    private String approverName;
    private String status;
    private String comments;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
