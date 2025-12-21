package vn.edu.smd.shared.dto.assessment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Program Learning Outcome (PLO) DTO
 * PLO belongs to a curriculum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PLODTO {
    
    /**
     * PLO UUID
     */
    private String id;
    
    /**
     * Curriculum ID
     */
    private String curriculumId;
    
    /**
     * Curriculum name
     */
    private String curriculumName;
    
    /**
     * PLO code (e.g., "PLO1", "PLO2")
     */
    private String code;
    
    /**
     * PLO description
     */
    private String description;
    
    /**
     * PLO category (e.g., "Knowledge", "Skills", "Attitude")
     */
    private String category;
    
    /**
     * Number of CLOs mapped to this PLO
     */
    private Integer mappedCloCount;
    
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
