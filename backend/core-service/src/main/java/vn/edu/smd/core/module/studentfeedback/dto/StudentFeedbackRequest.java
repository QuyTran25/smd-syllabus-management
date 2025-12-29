package vn.edu.smd.core.module.studentfeedback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.smd.shared.enums.ErrorReportSection;
import vn.edu.smd.shared.enums.FeedbackType;

import java.util.UUID;

@Data
public class StudentFeedbackRequest {
    @NotNull(message = "Syllabus ID is required")
    private UUID syllabusId;
    
    private FeedbackType type = FeedbackType.ERROR;
    
    private ErrorReportSection section = ErrorReportSection.OTHER;
    
    private String title;
    
    @NotNull(message = "Description is required")
    private String description;
}
