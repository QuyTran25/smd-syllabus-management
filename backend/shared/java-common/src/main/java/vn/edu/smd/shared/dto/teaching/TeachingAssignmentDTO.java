package vn.edu.smd.shared.dto.teaching;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.dto.academic.SubjectDTO;
import vn.edu.smd.shared.dto.academic.AcademicTermDTO;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.dto.syllabus.SyllabusListDTO;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Teaching Assignment DTO
 * Represents assignment of a lecturer to teach a subject in a specific term
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeachingAssignmentDTO {
    
    /**
     * Assignment UUID
     */
    private String id;
    
    /**
     * Subject ID
     */
    private String subjectId;
    
    /**
     * Subject details (optional, for nested response)
     */
    private SubjectDTO subject;
    
    /**
     * Academic term ID
     */
    private String academicTermId;
    
    /**
     * Academic term details (optional, for nested response)
     */
    private AcademicTermDTO academicTerm;
    
    /**
     * Main lecturer ID
     */
    private String mainLecturerId;
    
    /**
     * Main lecturer details (optional, for nested response)
     */
    private UserDTO mainLecturer;
    
    /**
     * Co-lecturers (optional, for nested response)
     */
    private List<TeachingAssignmentCollaboratorDTO> coLecturers;
    
    /**
     * Number of co-lecturers
     */
    private Integer coLecturerCount;
    
    /**
     * Syllabus submission deadline
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    
    /**
     * Days until deadline (calculated)
     */
    private Integer daysUntilDeadline;
    
    /**
     * Assignment status: pending, in-progress, submitted, completed
     */
    private AssignmentStatus status;
    
    /**
     * Syllabus version ID (once submitted)
     */
    private String syllabusVersionId;
    
    /**
     * Syllabus version details (optional, for nested response)
     */
    private SyllabusListDTO syllabusVersion;
    
    /**
     * Assigned by user ID
     */
    private String assignedBy;
    
    /**
     * Assigned by user details
     */
    private UserDTO assignedByUser;
    
    /**
     * Submission date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;
    
    /**
     * Completion date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
    
    /**
     * Notes or special instructions
     */
    private String notes;
    
    /**
     * Creation metadata
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
