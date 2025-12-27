package vn.edu.smd.core.module.teachingassignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Teaching Assignment Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssignmentResponse {
    
    private String id;
    
    // Subject info
    private String subjectId;
    private String subjectCode;
    private String subjectNameVi;
    private String subjectNameEn;
    private Integer credits;
    
    // Academic term info
    private String academicTermId;
    private String semester;
    
    // Main lecturer info
    private String mainLecturerId;
    private String mainLecturerName;
    private String mainLecturerEmail;
    
    // Co-lecturers
    private List<CollaboratorInfo> coLecturers;
    private Integer coLecturerCount;
    
    // Assignment details
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    
    private AssignmentStatus status;
    
    private String syllabusId;
    
    private String assignedById;
    private String assignedByName;
    
    private String comments;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaboratorInfo {
        private String id;
        private String name;
        private String email;
    }
}
