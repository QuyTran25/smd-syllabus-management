package vn.edu.smd.shared.dto.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.dto.syllabus.SyllabusListDTO;
import vn.edu.smd.shared.enums.ApprovalAction;
import vn.edu.smd.shared.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Approval History DTO
 * Records the approval/rejection actions on syllabus versions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalHistoryDTO {
    
    /**
     * History record UUID
     */
    private String id;
    
    /**
     * Syllabus version ID
     */
    private String syllabusVersionId;
    
    /**
     * Syllabus details (optional, for nested response)
     */
    private SyllabusListDTO syllabusVersion;
    
    /**
     * Actor (approver/rejector) user ID
     */
    private String actorId;
    
    /**
     * Actor details (optional, for nested response)
     */
    private UserDTO actor;
    
    /**
     * Decision: APPROVED, REJECTED
     */
    private ApprovalAction action;
    
    /**
     * Step number in the workflow
     */
    private Integer stepNumber;
    
    /**
     * Role code of the approver (HOD, AA, PRINCIPAL)
     */
    private UserRole roleCode;
    
    /**
     * Comment or reason for the decision
     */
    private String comment;
    
    /**
     * Batch ID (for bulk approvals)
     */
    private String batchId;
    
    /**
     * Number of items in the batch (if applicable)
     */
    private Integer batchSize;
    
    /**
     * Time taken to make decision (in minutes)
     */
    private Long decisionTimeMinutes;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
