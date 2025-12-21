package vn.edu.smd.shared.dto.syllabus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed syllabus DTO with full content
 * Used for syllabus detail view and editing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyllabusDetailDTO {
    
    /**
     * Syllabus version UUID
     */
    private String id;
    
    /**
     * Subject information
     */
    private SubjectInfo subject;
    
    /**
     * Version information
     */
    private String versionNo;
    private String previousVersionId;
    
    /**
     * Status and workflow
     */
    private SyllabusStatus status;
    private LocalDateTime reviewDeadline;
    
    /**
     * Academic term
     */
    private String academicTermId;
    private String academicTermName;
    
    /**
     * Content - stored as JSONB in database
     * Flexible structure to accommodate different syllabus formats
     */
    private SyllabusContent content;
    
    /**
     * Keywords for search
     */
    private List<String> keywords;
    
    /**
     * Metadata
     */
    private String createdBy;
    private String createdByName;
    private String updatedBy;
    private String updatedByName;
    private String approvedBy;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Subject basic information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubjectInfo {
        private String id;
        private String code;
        private String nameVi;
        private String nameEn;
        private Integer credits;
        private String departmentId;
        private String departmentName;
        private String facultyId;
        private String facultyName;
    }
    
    /**
     * Syllabus content structure
     * This is flexible and can be extended
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SyllabusContent {
        // Basic information
        private String description;
        private String objectives;
        
        // Prerequisites
        private List<String> prerequisiteIds;
        private List<PrerequisiteInfo> prerequisites;
        
        // Learning hours
        private Integer theoryHours;
        private Integer practiceHours;
        private Integer selfStudyHours;
        
        // Course outcomes
        private List<String> cloIds;
        
        // PLO mapping
        private List<String> ploIds;
        
        // Teaching plan
        private List<WeeklyPlan> weeklyPlans;
        
        // Assessment methods
        private List<String> assessmentSchemeIds;
        
        // Materials
        private List<Material> textbooks;
        private List<Material> references;
        
        // Additional sections (flexible)
        private JsonNode additionalInfo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrerequisiteInfo {
        private String id;
        private String code;
        private String name;
        private String type; // PREREQUISITE, CO_REQUISITE
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPlan {
        private Integer weekNumber;
        private String topic;
        private String content;
        private String teachingMethod;
        private String materials;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Material {
        private String title;
        private String author;
        private String publisher;
        private Integer year;
        private String type; // REQUIRED, REFERENCE
    }
}
