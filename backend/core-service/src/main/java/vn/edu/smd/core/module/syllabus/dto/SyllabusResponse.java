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
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
