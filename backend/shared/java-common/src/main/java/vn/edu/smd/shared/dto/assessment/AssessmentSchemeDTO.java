package vn.edu.smd.shared.dto.assessment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Assessment Scheme DTO
 * Defines how student performance is evaluated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentSchemeDTO {
    
    /**
     * Assessment scheme UUID
     */
    private String id;
    
    /**
     * Syllabus version ID
     */
    private String syllabusVersionId;
    
    /**
     * Parent assessment ID (for hierarchical structure)
     * e.g., "Midterm Exam" can have children "Theory" and "Practice"
     */
    private String parentId;
    
    /**
     * Assessment name (e.g., "Midterm Exam", "Final Project", "Attendance")
     */
    private String name;
    
    /**
     * Weight percentage (must sum to 100% for top-level assessments)
     */
    private BigDecimal weightPercent;
    
    /**
     * Assessment type (e.g., "EXAM", "PROJECT", "ASSIGNMENT", "PARTICIPATION")
     */
    private String assessmentType;
    
    /**
     * Description of assessment method
     */
    private String description;
    
    /**
     * CLOs evaluated by this assessment
     */
    private List<String> evaluatedCloIds;
    
    /**
     * Sub-assessments (children)
     */
    private List<AssessmentSchemeDTO> children;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
