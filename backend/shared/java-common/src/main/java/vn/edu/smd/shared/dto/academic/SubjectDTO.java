package vn.edu.smd.shared.dto.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.SubjectType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Subject (Môn học) DTO
 * Subject is the identity of a course (e.g., CS101)
 * Multiple syllabus versions can exist for one subject
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubjectDTO {
    
    /**
     * Subject UUID
     */
    private String id;
    
    /**
     * Subject code (e.g., "CS101", "122042")
     */
    private String code;
    
    /**
     * Department ID
     */
    private String departmentId;
    
    /**
     * Department name
     */
    private String departmentName;
    
    /**
     * Faculty ID
     */
    private String facultyId;
    
    /**
     * Faculty name
     */
    private String facultyName;
    
    /**
     * Curriculum ID (optional)
     */
    private String curriculumId;
    
    /**
     * Curriculum name
     */
    private String curriculumName;
    
    /**
     * Current subject name in Vietnamese
     */
    private String currentNameVi;
    
    /**
     * Current subject name in English
     */
    private String currentNameEn;
    
    /**
     * Default credit count
     */
    private Integer defaultCredits;
    
    /**
     * Subject type (REQUIRED/ELECTIVE)
     */
    private SubjectType subjectType;
    
    /**
     * Default teaching hours
     */
    private Integer defaultTheoryHours;
    private Integer defaultPracticeHours;
    private Integer defaultSelfStudyHours;
    
    /**
     * Subject description
     */
    private String description;
    
    /**
     * Recommended term to take this subject
     */
    private Integer recommendedTerm;
    
    /**
     * Is this subject currently active
     */
    private Boolean isActive;
    
    /**
     * Prerequisites for this subject
     */
    private List<PrerequisiteDTO> prerequisites;
    
    /**
     * Number of published syllabi for this subject
     */
    private Integer publishedSyllabusCount;
    
    /**
     * Latest published syllabus ID
     */
    private String latestSyllabusId;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Prerequisite information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrerequisiteDTO {
        private String id;
        private String code;
        private String name;
        private String type; // PREREQUISITE, CO_REQUISITE, REPLACEMENT
    }
}
