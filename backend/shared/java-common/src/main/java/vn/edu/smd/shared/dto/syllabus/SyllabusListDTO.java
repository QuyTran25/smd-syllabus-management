package vn.edu.smd.shared.dto.syllabus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDateTime;

/**
 * Syllabus list item DTO
 * Used for syllabus listings with pagination
 * Contains minimal information for performance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyllabusListDTO {
    
    /**
     * Syllabus version UUID
     */
    private String id;
    
    /**
     * Subject ID
     */
    private String subjectId;
    
    /**
     * Subject code (snapshot)
     */
    private String subjectCode;
    
    /**
     * Subject name in Vietnamese (snapshot)
     */
    private String subjectNameVi;
    
    /**
     * Subject name in English (snapshot)
     */
    private String subjectNameEn;
    
    /**
     * Credit count (snapshot)
     */
    private Integer credits;
    
    /**
     * Version number (e.g., "v1.0", "v2.1")
     */
    private String versionNo;
    
    /**
     * Current status in workflow
     */
    private SyllabusStatus status;
    
    /**
     * Academic term ID
     */
    private String academicTermId;
    
    /**
     * Academic term name (e.g., "HK1 2024-2025")
     */
    private String academicTermName;
    
    /**
     * Department name
     */
    private String departmentName;
    
    /**
     * Faculty name
     */
    private String facultyName;
    
    /**
     * Owner/Creator user ID
     */
    private String ownerId;
    
    /**
     * Owner full name
     */
    private String ownerName;
    
    /**
     * Approver user ID (if approved)
     */
    private String approvedBy;
    
    /**
     * Approver full name
     */
    private String approvedByName;
    
    /**
     * Review deadline
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewDeadline;
    
    /**
     * Published date (if published)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;
    
    /**
     * Creation date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Last update date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Check if editable
     */
    public boolean isEditable() {
        return status != null && status.isEditable();
    }
    
    /**
     * Check if pending approval
     */
    public boolean isPending() {
        return status != null && status.isPending();
    }
}
