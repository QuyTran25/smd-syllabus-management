package vn.edu.smd.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.academic.DepartmentDTO;

import java.time.LocalDateTime;

/**
 * Lecturer Profile DTO
 * Extended profile information for lecturers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LecturerProfileDTO {
    
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
     * Lecturer code (e.g., "L001")
     */
    private String lecturerCode;
    
    /**
     * Department ID
     */
    private String departmentId;
    
    /**
     * Department details (optional, for nested response)
     */
    private DepartmentDTO department;
    
    /**
     * Academic title (e.g., "Professor", "Associate Professor", "Lecturer")
     */
    private String title;
    
    /**
     * Field of specialization
     */
    private String specialization;
    
    /**
     * Total subjects taught
     */
    private Integer totalSubjectsTaught;
    
    /**
     * Total syllabi created
     */
    private Integer totalSyllabiCreated;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
