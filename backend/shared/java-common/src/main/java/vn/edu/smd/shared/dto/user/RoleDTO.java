package vn.edu.smd.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Role DTO for RBAC system
 * Represents system roles (ADMIN, DEAN, HOD, LECTURER, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    
    /**
     * Role UUID
     */
    private String id;
    
    /**
     * Role code (e.g., "ADMIN", "DEAN", "HOD", "LECTURER")
     */
    private String code;
    
    /**
     * Role display name
     */
    private String name;
    
    /**
     * Role description
     */
    private String description;
    
    /**
     * System role flag - cannot be deleted if true
     */
    private Boolean isSystem;
    
    /**
     * Number of users assigned to this role
     */
    private Integer userCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
