package vn.edu.smd.core.module.feedbackresponse.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FeedbackResponseResponse {
    private UUID id;
    private UUID questionId;
    private String questionText;
    private UUID respondentId;
    private String respondentName;
    private String responseText;
    private Integer rating;
    private String selectedOption;
    private LocalDateTime createdAt;
}
