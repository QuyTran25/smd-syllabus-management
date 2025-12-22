package vn.edu.smd.core.module.feedbackquestion.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FeedbackQuestionResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String questionText;
    private String questionType;
    private String options;
    private Boolean isRequired;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
