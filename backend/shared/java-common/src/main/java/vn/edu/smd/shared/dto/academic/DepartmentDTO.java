package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Department (Bộ môn) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentDTO {
    
    /**
     * Department UUID
     */
    private String id;
    
    /**
     * Faculty ID
     */
    private String facultyId;
    
    /**
     * Faculty name
     */
    private String facultyName;
    
    /**
     * Department code (e.g., "KHMT", "KTPM")
     */
    private String code;
    
    /**
     * Department name
     */
    private String name;
    
    /**
     * Number of subjects in this department
     */
    private Integer subjectCount;
    
    /**
     * Number of lecturers in this department
     */
    private Integer lecturerCount;
    
    /**
     * Head of Department user ID
     */
    private String hodId;
    
    /**
     * Head of Department name
     */
    private String hodName;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
