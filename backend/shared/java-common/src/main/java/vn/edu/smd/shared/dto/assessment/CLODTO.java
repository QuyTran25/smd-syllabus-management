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
 * Course Learning Outcome (CLO) DTO
 * CLO belongs to a specific syllabus version
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CLODTO {
    
    /**
     * CLO UUID
     */
    private String id;
    
    /**
     * Syllabus version ID
     */
    private String syllabusVersionId;
    
    /**
     * CLO code (e.g., "CLO1", "CLO2")
     */
    private String code;
    
    /**
     * CLO description
     */
    private String description;
    
    /**
     * Bloom's Taxonomy level
     * (e.g., "Remember", "Understand", "Apply", "Analyze", "Evaluate", "Create")
     */
    private String bloomLevel;
    
    /**
     * PLO mappings for this CLO
     */
    private List<PLOMappingInfo> ploMappings;
    
    /**
     * Creation metadata
     */
    private String createdBy;
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * PLO mapping information for a CLO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PLOMappingInfo {
        private String ploId;
        private String ploCode;
        private String ploDescription;
        private BigDecimal weight; // Weight/contribution percentage
    }
}
