package vn.edu.smd.shared.dto.feedback;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Student Feedback DTO
 * For students to report errors or provide feedback on syllabi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedbackDTO {
    
    /**
     * Feedback UUID
     */
    private String id;
    
    /**
     * Syllabus version ID
     */
    private String syllabusVersionId;
    
    /**
     * Syllabus subject code
     */
    private String subjectCode;
    
    /**
     * Syllabus subject name
     */
    private String subjectName;
    
    /**
     * Student user ID
     */
    private String studentId;
    
    /**
     * Student name (optional, can be anonymous)
     */
    private String studentName;
    
    /**
     * Feedback section (where the error is)
     * e.g., "subject_info", "objectives", "clo", "assessment_matrix"
     */
    private String section;
    
    /**
     * Error type/category
     * e.g., "TYPO", "INCORRECT_INFO", "MISSING_INFO", "UNCLEAR", "OTHER"
     */
    private String errorType;
    
    /**
     * Detailed feedback message
     */
    private String message;
    
    /**
     * Is this an error report or suggestion
     */
    private Boolean isErrorReport;
    
    /**
     * Feedback status
     * "NEW", "ACKNOWLEDGED", "RESOLVED", "REJECTED"
     */
    private String status;
    
    /**
     * Admin/Lecturer response
     */
    private String response;
    
    /**
     * Responded by user ID
     */
    private String respondedBy;
    
    /**
     * Response timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime respondedAt;
    
    /**
     * Creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
