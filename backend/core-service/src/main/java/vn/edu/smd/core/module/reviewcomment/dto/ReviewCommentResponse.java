package vn.edu.smd.core.module.reviewcomment.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewCommentResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String section;
    private String content;
    private Boolean isResolved;
    private UUID parentId;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
