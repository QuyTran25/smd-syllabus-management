package vn.edu.smd.core.module.revision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Revision Session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionSessionResponse {
    
    private UUID id;
    private UUID syllabusVersionId;
    private String syllabusCode;
    private String syllabusName;
    
    private Integer sessionNumber;
    private String status;
    
    private UUID initiatedById;
    private String initiatedByName;
    private LocalDateTime initiatedAt;
    
    private String description;
    
    private UUID assignedLecturerId;
    private String assignedLecturerName;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    private UUID hodReviewedById;
    private String hodReviewedByName;
    private LocalDateTime hodReviewedAt;
    private String hodDecision;
    private String hodComment;
    
    private UUID republishedById;
    private String republishedByName;
    private LocalDateTime republishedAt;
    
    private Integer feedbackCount;
    private List<UUID> feedbackIds;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
