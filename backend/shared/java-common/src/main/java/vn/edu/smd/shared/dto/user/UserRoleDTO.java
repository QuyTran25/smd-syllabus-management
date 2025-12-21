package vn.edu.smd.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.RoleScope;

import java.time.LocalDateTime;

/**
 * User Role Assignment DTO
 * Maps users to roles with specific scope (GLOBAL, FACULTY, DEPARTMENT)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleDTO {
    
    /**
     * User role assignment UUID
     */
    private String id;
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * User details (optional, for nested response)
     */
    private UserDTO user;
    
    /**
     * Role ID
     */
    private String roleId;
    
    /**
     * Role details (optional, for nested response)
     */
    private RoleDTO role;
    
    /**
     * Scope type: GLOBAL, FACULTY, DEPARTMENT
     */
    private RoleScope scopeType;
    
    /**
     * Scope ID (null if GLOBAL)
     */
    private String scopeId;
    
    /**
     * Scope name (for display purposes)
     */
    private String scopeName;
    
    /**
     * Start date of role assignment
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    
    /**
     * End date of role assignment (null if permanent)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    
    /**
     * Active status
     */
    private Boolean isActive;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
