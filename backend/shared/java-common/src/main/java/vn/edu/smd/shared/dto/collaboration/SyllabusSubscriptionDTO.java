package vn.edu.smd.shared.dto.collaboration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.dto.academic.SubjectDTO;
import vn.edu.smd.shared.dto.syllabus.SyllabusListDTO;

import java.time.LocalDateTime;

/**
 * Syllabus Subscription DTO
 * Represents users subscribed to receive notifications about syllabus changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyllabusSubscriptionDTO {
    
    /**
     * Subscription UUID
     */
    private String id;
    
    /**
     * User ID (subscriber)
     */
    private String userId;
    
    /**
     * User details (optional, for nested response)
     */
    private UserDTO user;
    
    /**
     * Subject ID (subscribe to all versions of this subject)
     */
    private String subjectId;
    
    /**
     * Subject details (optional, for nested response)
     */
    private SubjectDTO subject;
    
    /**
     * Specific syllabus version ID (optional, to subscribe to a specific version)
     */
    private String syllabusVersionId;
    
    /**
     * Syllabus version details (optional, for nested response)
     */
    private SyllabusListDTO syllabusVersion;
    
    /**
     * Is this subscription active?
     */
    private Boolean isActive;
    
    /**
     * Notification preferences
     */
    private Boolean notifyOnPublish;
    
    private Boolean notifyOnUpdate;
    
    private Boolean notifyOnApproval;
    
    private Boolean notifyOnComment;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
