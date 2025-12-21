package vn.edu.smd.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.academic.CurriculumDTO;

import java.time.LocalDateTime;

/**
 * Student Profile DTO
 * Extended profile information for students
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProfileDTO {
    
    /**
     * Profile UUID
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
     * Student code (e.g., "2021600001")
     */
    private String studentCode;
    
    /**
     * Curriculum ID
     */
    private String curriculumId;
    
    /**
     * Curriculum details (optional, for nested response)
     */
    private CurriculumDTO curriculum;
    
    /**
     * Enrollment year (e.g., 2021)
     */
    private Integer enrollmentYear;
    
    /**
     * Current GPA
     */
    private Double gpa;
    
    /**
     * Total credits completed
     */
    private Integer creditsCompleted;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
