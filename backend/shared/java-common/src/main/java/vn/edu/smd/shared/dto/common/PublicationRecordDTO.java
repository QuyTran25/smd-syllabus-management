package vn.edu.smd.shared.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.dto.syllabus.SyllabusListDTO;

import java.time.LocalDateTime;

/**
 * Publication Record DTO
 * Tracks the publication history of syllabus versions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicationRecordDTO {
    
    /**
     * Publication record UUID
     */
    private String id;
    
    /**
     * Syllabus version ID
     */
    private String syllabusVersionId;
    
    /**
     * Syllabus version details (optional, for nested response)
     */
    private SyllabusListDTO syllabusVersion;
    
    /**
     * Published by user ID
     */
    private String publishedBy;
    
    /**
     * Publisher details (optional, for nested response)
     */
    private UserDTO publisher;
    
    /**
     * Publication timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;
    
    /**
     * Publication notes or announcement message
     */
    private String notes;
    
    /**
     * Is this a republish? (vs initial publish)
     */
    private Boolean isRepublish;
    
    /**
     * Previous publication record ID (if republish)
     */
    private String previousPublicationId;
    
    /**
     * Previous publication details
     */
    private PublicationRecordDTO previousPublication;
    
    /**
     * Publication version number (1, 2, 3, etc.)
     */
    private Integer publicationVersion;
    
    /**
     * Number of users notified about this publication
     */
    private Integer notificationsSent;
    
    /**
     * Publication channel (e.g., "PORTAL", "EMAIL", "BOTH")
     */
    private String channel;
    
    /**
     * View count since publication
     */
    private Integer viewCount;
    
    /**
     * Download count since publication
     */
    private Integer downloadCount;
}
