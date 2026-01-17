package vn.edu.smd.core.module.studentfeedback.dto;

import lombok.Data;
import vn.edu.smd.shared.enums.ErrorReportSection;
import vn.edu.smd.shared.enums.FeedbackType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StudentFeedbackResponse {
    private UUID id;
    
    // Syllabus info
    private UUID syllabusId;
    private String syllabusCode;
    private String syllabusName;
    
    // Lecturer info
    private UUID lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    
    // Student info
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    
    // Feedback details
    private FeedbackType type;
    private ErrorReportSection section;
    private String sectionDisplay; // Display name in Vietnamese
    private String title;
    private String description;
    private String status;
    
    // Admin response
    private String adminResponse;
    private UUID respondedById;
    private String respondedByName;
    private LocalDateTime respondedAt;
    
    // Edit enabled
    private Boolean editEnabled;
    private LocalDateTime editEnabledAt;
    private String editEnabledBy;
    
    // Resolution
    private UUID resolvedById;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
