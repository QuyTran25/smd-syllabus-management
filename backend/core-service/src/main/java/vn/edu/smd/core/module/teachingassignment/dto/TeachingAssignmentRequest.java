package vn.edu.smd.core.module.teachingassignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class TeachingAssignmentRequest {
    
    @NotNull(message = "Subject ID is required")
    private UUID subjectId;
    
    @NotNull(message = "Academic Term ID is required")
    private UUID academicTermId;
    
    @NotNull(message = "Main Lecturer ID is required")
    private UUID mainLecturerId;
    
    private List<UUID> collaboratorIds; // List of co-lecturer IDs
    
    @NotNull(message = "Deadline is required")
    private LocalDate deadline;
    
    private String comments; // Notes for the assignment
}
