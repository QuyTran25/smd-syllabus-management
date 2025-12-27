package vn.edu.smd.core.module.syllabus.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class SyllabusResponse {
    private UUID id;
    private UUID subjectId;
    private String subjectCode;
    private String subjectNameVi;
    private String subjectNameEn;
    private Integer creditCount;
    
    private UUID academicTermId;
    private String academicTermCode;
    
    private String versionNo;
    private String status;
    private UUID previousVersionId;
    
    private LocalDateTime reviewDeadline;
    private LocalDate effectiveDate;
    
    private String[] keywords;
    private Map<String, Object> content;
    
    // Subject type and component
    private String courseType;        // REQUIRED, ELECTIVE
    private String componentType;     // THEORY, PRACTICE, BOTH
    
    // Faculty and academic info
    private String faculty;
    private String academicYear;
    
    // Description and objectives (from content or subject)
    private String description;
    private List<String> objectives;
    
    // Time allocation
    private Integer theoryHours;
    private Integer practiceHours;
    private Integer selfStudyHours;
    private Integer totalStudyHours;
    
    // CLOs and Assessments
    private List<CLOResponse> clos;
    private List<AssessmentResponse> assessmentMethods;
    
    // CLO-PLO mappings
    private List<CLOPLOMappingResponse> ploMappings;
    
    // Ownership and department info
    private String ownerName;
    private String department;
    private String semester;
    
    // Approval workflow tracking
    private LocalDateTime submittedAt;
    private LocalDateTime hodApprovedAt;
    private String hodApprovedByName;
    private LocalDateTime aaApprovedAt;
    private String aaApprovedByName;
    private LocalDateTime principalApprovedAt;
    private String principalApprovedByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    
    // Inner classes for CLO and Assessment
    @Data
    public static class CLOResponse {
        private UUID id;
        private String code;
        private String description;
        private String bloomLevel;
        private BigDecimal weight;
    }
    
    @Data
    public static class AssessmentResponse {
        private UUID id;
        private String name;
        private String method;
        private String form;
        private String criteria;
        private BigDecimal weight;
        private List<String> clos;  // CLO codes linked to this assessment
    }
    
    @Data
    public static class CLOPLOMappingResponse {
        private String cloCode;
        private String ploCode;
        private String contributionLevel;  // H, M, L
    }
}
