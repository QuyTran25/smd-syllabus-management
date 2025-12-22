package vn.edu.smd.core.module.feedbackquestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackQuestionRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Question type is required")
    @Size(max = 20, message = "Question type must not exceed 20 characters")
    private String questionType;

    private String options;

    private Boolean isRequired;

    private Integer displayOrder;
}
