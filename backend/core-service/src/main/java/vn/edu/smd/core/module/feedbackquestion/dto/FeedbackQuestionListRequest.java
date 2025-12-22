package vn.edu.smd.core.module.feedbackquestion.dto;

import lombok.Data;

@Data
public class FeedbackQuestionListRequest {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "displayOrder";
    private String sortDirection = "asc";
    private String questionType;
    private Boolean isRequired;
}
