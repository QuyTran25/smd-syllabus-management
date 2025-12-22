package vn.edu.smd.core.module.workflowstep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class WorkflowStepRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @NotNull(message = "Step order is required")
    private Integer stepOrder;

    @NotBlank(message = "Step name is required")
    @Size(max = 100, message = "Step name must not exceed 100 characters")
    private String stepName;

    @NotBlank(message = "Approver role is required")
    @Size(max = 50, message = "Approver role must not exceed 50 characters")
    private String approverRole;

    private UUID approverId;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private String comments;
}
