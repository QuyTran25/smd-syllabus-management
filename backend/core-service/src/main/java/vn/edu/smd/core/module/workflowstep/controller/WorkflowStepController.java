package vn.edu.smd.core.module.workflowstep.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepListRequest;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepRequest;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepResponse;
import vn.edu.smd.core.module.workflowstep.service.WorkflowStepService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Workflow Step Management", description = "Workflow step management APIs")
@RestController
@RequestMapping("/api/workflow-steps")
@RequiredArgsConstructor
public class WorkflowStepController {

    private final WorkflowStepService workflowStepService;

    @Operation(summary = "Get all workflow steps", description = "Get list of workflow steps with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WorkflowStepResponse>>> getAllWorkflowSteps(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "stepOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approverRole) {
        
        WorkflowStepListRequest request = new WorkflowStepListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        request.setStatus(status);
        request.setApproverRole(approverRole);
        
        Page<WorkflowStepResponse> workflowSteps = workflowStepService.getAllWorkflowSteps(request);
        return ResponseEntity.ok(ApiResponse.success(workflowSteps));
    }

    @Operation(summary = "Get workflow step by ID", description = "Get workflow step details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> getWorkflowStepById(@PathVariable UUID id) {
        WorkflowStepResponse workflowStep = workflowStepService.getWorkflowStepById(id);
        return ResponseEntity.ok(ApiResponse.success(workflowStep));
    }

    @Operation(summary = "Get workflow by syllabus", description = "Get workflow steps for a specific syllabus version")
    @GetMapping("/syllabus/{syllabusId}/workflow")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> getWorkflowBySyllabus(@PathVariable UUID syllabusId) {
        List<WorkflowStepResponse> workflowSteps = workflowStepService.getWorkflowBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(workflowSteps));
    }

    @Operation(summary = "Create workflow step", description = "Create new workflow step")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> createWorkflowStep(@Valid @RequestBody WorkflowStepRequest request) {
        WorkflowStepResponse workflowStep = workflowStepService.createWorkflowStep(request);
        return ResponseEntity.ok(ApiResponse.success("Workflow step created successfully", workflowStep));
    }

    @Operation(summary = "Update workflow step", description = "Update workflow step information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> updateWorkflowStep(
            @PathVariable UUID id, 
            @Valid @RequestBody WorkflowStepRequest request) {
        WorkflowStepResponse workflowStep = workflowStepService.updateWorkflowStep(id, request);
        return ResponseEntity.ok(ApiResponse.success("Workflow step updated successfully", workflowStep));
    }

    @Operation(summary = "Delete workflow step", description = "Delete workflow step by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflowStep(@PathVariable UUID id) {
        workflowStepService.deleteWorkflowStep(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow step deleted successfully", null));
    }
}
