package vn.edu.smd.core.module.workflowstep.dto;

import lombok.Data;

@Data
public class WorkflowStepListRequest {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "stepOrder";
    private String sortDirection = "asc";
    private String status;
    private String approverRole;
}
