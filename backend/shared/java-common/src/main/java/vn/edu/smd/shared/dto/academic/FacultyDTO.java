package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Faculty (Khoa) DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacultyDTO {
    
    /**
     * Faculty UUID
     */
    private String id;
    
    /**
     * Faculty code (e.g., "FIT", "CNTT")
     */
    private String code;
    
    /**
     * Faculty name
     */
    private String name;
    
    /**
     * Number of departments under this faculty
     */
    private Integer departmentCount;
    
    /**
     * Number of subjects under this faculty
     */
    private Integer subjectCount;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
