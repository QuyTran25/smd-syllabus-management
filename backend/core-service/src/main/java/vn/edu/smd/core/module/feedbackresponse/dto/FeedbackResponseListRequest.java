package vn.edu.smd.core.module.feedbackresponse.dto;

import lombok.Data;

@Data
public class FeedbackResponseListRequest {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
