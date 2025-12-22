package vn.edu.smd.core.module.collaborationchange.dto;

import lombok.Data;

@Data
public class CollaborationChangeListRequest {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
    private String changeType;
}
