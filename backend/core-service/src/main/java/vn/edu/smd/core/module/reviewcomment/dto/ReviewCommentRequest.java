package vn.edu.smd.core.module.reviewcomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewCommentRequest {
    @NotNull(message = "Syllabus version ID is required")
    private UUID syllabusVersionId;

    @Size(max = 50, message = "Section must not exceed 50 characters")
    private String section;

    @NotBlank(message = "Content is required")
    private String content;

    private Boolean isResolved;

    private UUID parentId;
}
