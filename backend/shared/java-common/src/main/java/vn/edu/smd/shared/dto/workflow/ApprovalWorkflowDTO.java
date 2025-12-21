package vn.edu.smd.shared.dto.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Approval Workflow DTO
 * Defines the approval process configuration with steps and roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalWorkflowDTO {
    
    /**
     * Workflow UUID
     */
    private String id;
    
    /**
     * Workflow name (e.g., "Standard Syllabus Approval", "Fast Track Approval")
     */
    private String name;
    
    /**
     * Workflow description
     */
    private String description;
    
    /**
     * Workflow steps in JSONB format
     * Example:
     * [
     *   {"role": "HOD", "order": 1, "required": true},
     *   {"role": "AA", "order": 2, "required": true},
     *   {"role": "PRINCIPAL", "order": 3, "required": false}
     * ]
     */
    private List<Map<String, Object>> steps;
    
    /**
     * Total number of steps
     */
    private Integer stepCount;
    
    /**
     * Is this workflow active?
     */
    private Boolean isActive;
    
    /**
     * Is this the default workflow?
     */
    private Boolean isDefault;
    
    /**
     * Number of syllabi using this workflow
     */
    private Integer usageCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
}
