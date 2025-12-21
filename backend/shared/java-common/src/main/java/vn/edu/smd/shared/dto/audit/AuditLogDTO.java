package vn.edu.smd.shared.dto.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit Log DTO
 * Records all important actions in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogDTO {
    
    /**
     * Audit log UUID
     */
    private String id;
    
    /**
     * User who performed the action
     */
    private String userId;
    
    /**
     * User full name
     */
    private String userFullName;
    
    /**
     * User role at the time of action
     */
    private String userRole;
    
    /**
     * Action performed
     * e.g., "CREATE_SYLLABUS", "UPDATE_SYLLABUS", "APPROVE", "REJECT", "LOGIN", "LOGOUT"
     */
    private String action;
    
    /**
     * Resource type affected
     * e.g., "SYLLABUS", "USER", "SUBJECT", "PLO"
     */
    private String resourceType;
    
    /**
     * Resource ID affected
     */
    private String resourceId;
    
    /**
     * Resource name (for display)
     */
    private String resourceName;
    
    /**
     * Detailed action information (flexible JSONB)
     * Can contain before/after values, additional context
     */
    private JsonNode details;
    
    /**
     * IP address of the user
     */
    private String ipAddress;
    
    /**
     * User agent (browser/app info)
     */
    private String userAgent;
    
    /**
     * Action result status
     * "SUCCESS", "FAILED"
     */
    private String status;
    
    /**
     * Error message (if failed)
     */
    private String errorMessage;
    
    /**
     * Timestamp when action occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
