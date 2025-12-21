package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.SubjectRelationType;

import java.time.LocalDateTime;

/**
 * Subject Relationship DTO
 * Represents prerequisite, co-requisite, or replacement relationships between subjects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubjectRelationshipDTO {
    
    /**
     * Relationship UUID
     */
    private String id;
    
    /**
     * Subject ID (the subject that has the relationship)
     */
    private String subjectId;
    
    /**
     * Subject details (optional, for nested response)
     */
    private SubjectDTO subject;
    
    /**
     * Related subject ID
     */
    private String relatedSubjectId;
    
    /**
     * Related subject details (optional, for nested response)
     */
    private SubjectDTO relatedSubject;
    
    /**
     * Relationship type: PREREQUISITE, CO_REQUISITE, REPLACEMENT
     */
    private SubjectRelationType type;
    
    /**
     * Description or notes about the relationship
     */
    private String notes;
    
    /**
     * Created by user ID
     */
    private String createdBy;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
