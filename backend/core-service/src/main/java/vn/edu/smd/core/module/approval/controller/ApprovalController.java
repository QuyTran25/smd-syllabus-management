package vn.edu.smd.core.module.approval.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.approval.dto.ApprovalRequest;
import vn.edu.smd.core.module.approval.dto.ApprovalResponse;
import vn.edu.smd.core.module.approval.service.ApprovalService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Approval History Management", description = "Approval history management APIs")
@RestController
@RequestMapping("/api/approval-history")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "Get all approval history with pagination", description = "Get list of all approval history with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApprovalResponse>>> getAllApprovals(Pageable pageable) {
        Page<ApprovalResponse> approvals = approvalService.getAllApprovals(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(approvals)));
    }

    @Operation(summary = "Get approval history of syllabus", description = "Get list of approval history for a syllabus")
    @GetMapping("/syllabi/{syllabusId}/approvals")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getApprovalHistoryOfSyllabus(@PathVariable UUID syllabusId) {
        List<ApprovalResponse> approvals = approvalService.getApprovalHistoryOfSyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(approvals));
    }

    @Operation(summary = "Get approvals by syllabus", description = "Get list of approval history for a syllabus version")
    @GetMapping("/syllabus/{syllabusVersionId}")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getApprovalsBySyllabus(@PathVariable UUID syllabusVersionId) {
        List<ApprovalResponse> approvals = approvalService.getApprovalsBySyllabus(syllabusVersionId);
        return ResponseEntity.ok(ApiResponse.success(approvals));
    }

    @Operation(summary = "Get approval by ID", description = "Get approval history details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalResponse>> getApprovalById(@PathVariable UUID id) {
        ApprovalResponse approval = approvalService.getApprovalById(id);
        return ResponseEntity.ok(ApiResponse.success(approval));
    }

    @Operation(summary = "Create approval", description = "Create new approval history record")
    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalResponse>> createApproval(@Valid @RequestBody ApprovalRequest request) {
        ApprovalResponse approval = approvalService.createApproval(request);
        return ResponseEntity.ok(ApiResponse.success("Approval created successfully", approval));
    }

    @Operation(summary = "Delete approval", description = "Delete approval history by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApproval(@PathVariable UUID id) {
        approvalService.deleteApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Approval deleted successfully", null));
    }

    @Operation(summary = "Get pending approvals for user", description = "Get list of pending approvals assigned to a user")
    @GetMapping("/users/{userId}/pending-approvals")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingApprovalsForUser(@PathVariable UUID userId) {
        List<ApprovalResponse> approvals = approvalService.getPendingApprovalsForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(approvals));
    }
}
