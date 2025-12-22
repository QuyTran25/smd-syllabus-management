package vn.edu.smd.core.module.approval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.smd.shared.enums.DecisionType;

import java.util.UUID;

@Data
public class ApprovalRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @NotNull(message = "Action is required")
    private DecisionType action;

    private String comment;
}
