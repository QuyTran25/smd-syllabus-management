package vn.edu.smd.core.module.syllabus.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
}
