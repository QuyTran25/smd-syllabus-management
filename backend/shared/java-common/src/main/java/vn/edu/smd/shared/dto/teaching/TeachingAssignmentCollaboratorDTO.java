package vn.edu.smd.shared.dto.teaching;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;

import java.time.LocalDateTime;

/**
 * Teaching Assignment Collaborator DTO
 * Represents co-lecturers assigned to help with a teaching assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeachingAssignmentCollaboratorDTO {
    
    /**
     * Collaborator assignment UUID
     */
    private String id;
    
    /**
     * Teaching assignment ID
     */
    private String assignmentId;
    
    /**
     * Teaching assignment details (optional, to avoid circular reference)
     */
    private String assignmentName;
    
    /**
     * Lecturer (co-lecturer) ID
     */
    private String lecturerId;
    
    /**
     * Lecturer details (optional, for nested response)
     */
    private UserDTO lecturer;
    
    /**
     * Role or responsibility of this co-lecturer
     */
    private String role;
    
    /**
     * Percentage of contribution (if applicable)
     */
    private Integer contributionPercent;
    
    /**
     * Is this collaborator active?
     */
    private Boolean isActive;
    
    /**
     * Notes about this collaborator's responsibilities
     */
    private String notes;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
