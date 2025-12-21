package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Curriculum (Chương trình đào tạo) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurriculumDTO {
    
    /**
     * Curriculum UUID
     */
    private String id;
    
    /**
     * Curriculum code (e.g., "CS2024", "SE2023")
     */
    private String code;
    
    /**
     * Curriculum name
     */
    private String name;
    
    /**
     * Faculty ID
     */
    private String facultyId;
    
    /**
     * Faculty name
     */
    private String facultyName;
    
    /**
     * Total credits required for graduation
     */
    private Integer totalCredits;
    
    /**
     * Number of subjects in this curriculum
     */
    private Integer subjectCount;
    
    /**
     * Number of PLOs defined for this curriculum
     */
    private Integer ploCount;
    
    /**
     * Creation metadata
     */
    private String createdBy;
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
