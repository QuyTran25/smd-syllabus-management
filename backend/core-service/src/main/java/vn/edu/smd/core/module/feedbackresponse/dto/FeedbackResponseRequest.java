package vn.edu.smd.core.module.feedbackresponse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackResponseRequest {
    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotNull(message = "Respondent ID is required")
    private UUID respondentId;

    private String responseText;

    private Integer rating;

    private String selectedOption;
}
