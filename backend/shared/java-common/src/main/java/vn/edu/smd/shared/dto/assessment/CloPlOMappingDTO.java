package vn.edu.smd.shared.dto.assessment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CLO-PLO Mapping DTO
 * Represents the relationship between Course Learning Outcomes and Program Learning Outcomes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloPlOMappingDTO {
    
    /**
     * Mapping UUID
     */
    private String id;
    
    /**
     * CLO information
     */
    private String cloId;
    private String cloCode;
    private String cloDescription;
    
    /**
     * PLO information
     */
    private String ploId;
    private String ploCode;
    private String ploDescription;
    
    /**
     * Mapping weight/contribution
     * Indicates how much this CLO contributes to the PLO
     */
    private BigDecimal weight;
    
    /**
     * Mapping strength level
     * e.g., "HIGH", "MEDIUM", "LOW"
     */
    private String mappingLevel;
}
