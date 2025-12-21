package vn.edu.smd.shared.dto.collaboration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.dto.syllabus.SyllabusListDTO;
import vn.edu.smd.shared.enums.CollaboratorRole;

import java.time.LocalDateTime;

/**
 * Syllabus Collaborator DTO
 * Represents users who can collaborate on editing a syllabus
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyllabusCollaboratorDTO {
    
    /**
     * Collaborator assignment UUID
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
     * User ID
     */
    private String userId;
    
    /**
     * User details (optional, for nested response)
     */
    private UserDTO user;
    
    /**
     * Collaborator role: OWNER, EDITOR, VIEWER
     */
    private CollaboratorRole role;
    
    /**
     * Can this user edit the syllabus?
     */
    private Boolean canEdit;
    
    /**
     * Can this user approve the syllabus?
     */
    private Boolean canApprove;
    
    /**
     * Assigned by user ID
     */
    private String assignedBy;
    
    /**
     * Assigned by user details
     */
    private UserDTO assignedByUser;
    
    /**
     * Assignment date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assignedAt;
    
    /**
     * Last activity date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivityAt;
    
    /**
     * Number of edits made by this collaborator
     */
    private Integer editCount;
}
