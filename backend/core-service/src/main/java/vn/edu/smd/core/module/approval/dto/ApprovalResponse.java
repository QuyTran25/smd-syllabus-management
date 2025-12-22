package vn.edu.smd.core.module.approval.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.ActorRoleType;
import vn.edu.smd.shared.enums.DecisionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApprovalResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private UUID actorId;
    private String actorName;
    private DecisionType action;
    private String comment;
    private UUID batchId;
    private Integer stepNumber;
    private String roleCode;
    private ActorRoleType actorRole;
    private LocalDateTime createdAt;
}
