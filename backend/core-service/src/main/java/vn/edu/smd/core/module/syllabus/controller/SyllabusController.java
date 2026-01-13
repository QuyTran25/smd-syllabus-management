package vn.edu.smd.core.module.syllabus.controller;

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
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.module.syllabus.service.SyllabusService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Syllabus Management", description = "Syllabus version management APIs")
@RestController
@RequestMapping("/api/syllabi")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    @Operation(summary = "Get all syllabi", description = "Get list of syllabi with pagination and filtering")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SyllabusResponse>>> getAllSyllabi(
            Pageable pageable,
            @RequestParam(required = false) List<String> status) {
        Page<SyllabusResponse> syllabi = syllabusService.getAllSyllabi(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(syllabi)));
    }

    @Operation(summary = "Get syllabus by ID", description = "Get syllabus details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SyllabusResponse>> getSyllabusById(@PathVariable UUID id) {
        SyllabusResponse syllabus = syllabusService.getSyllabusById(id);
        return ResponseEntity.ok(ApiResponse.success(syllabus));
    }

    @Operation(summary = "Create syllabus", description = "Create new syllabus")
    @PostMapping
    public ResponseEntity<ApiResponse<SyllabusResponse>> createSyllabus(@Valid @RequestBody SyllabusRequest request) {
        SyllabusResponse syllabus = syllabusService.createSyllabus(request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus created successfully", syllabus));
    }

    @Operation(summary = "Create syllabus from teaching assignment", 
               description = "Create syllabus draft from teaching assignment with auto-filled basic info")
    @PostMapping("/from-assignment")
    public ResponseEntity<ApiResponse<SyllabusResponse>> createSyllabusFromAssignment(
            @Valid @RequestBody CreateSyllabusFromAssignmentRequest request) {
        SyllabusResponse syllabus = syllabusService.createSyllabusFromAssignment(request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus draft created from assignment", syllabus));
    }

    @Operation(summary = "Update syllabus", description = "Update syllabus (only DRAFT status)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SyllabusResponse>> updateSyllabus(@PathVariable UUID id, @Valid @RequestBody SyllabusRequest request) {
        SyllabusResponse syllabus = syllabusService.updateSyllabus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus updated successfully", syllabus));
    }

    @Operation(summary = "Delete syllabus", description = "Delete syllabus (only DRAFT status)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSyllabus(@PathVariable UUID id) {
        syllabusService.deleteSyllabus(id);
        return ResponseEntity.ok(ApiResponse.success("Syllabus deleted successfully", null));
    }

    @Operation(summary = "Submit syllabus for approval", description = "Submit syllabus to approval workflow")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<SyllabusResponse>> submitSyllabus(
            @PathVariable UUID id, 
            @RequestBody(required = false) SyllabusApprovalRequest request) {
        SyllabusResponse syllabus = syllabusService.submitSyllabus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus submitted for approval", syllabus));
    }

    // --- ĐÃ XÓA HÀM publishSyllabus ĐỂ TRÁNH XUNG ĐỘT VỚI ADMIN CONTROLLER ---
    // Frontend gọi vào /api/syllabi/{id}/publish sẽ được Spring tự động chuyển 
    // đến AdminSyllabusController (nơi đã có sẵn API này).
    // --------------------------------------------------------------------------

    @Operation(summary = "Approve syllabus", description = "Approve syllabus (moves to next approval stage)")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SyllabusResponse>> approveSyllabus(
            @PathVariable UUID id, 
            @RequestBody(required = false) SyllabusApprovalRequest request) {
        SyllabusResponse syllabus = syllabusService.approveSyllabus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus approved successfully", syllabus));
    }

    @Operation(summary = "Reject syllabus", description = "Reject syllabus with reason")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SyllabusResponse>> rejectSyllabus(
            @PathVariable UUID id, 
            @RequestBody SyllabusApprovalRequest request) {
        SyllabusResponse syllabus = syllabusService.rejectSyllabus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Syllabus rejected", syllabus));
    }

    @Operation(summary = "Clone syllabus", description = "Create new version by cloning existing syllabus")
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<SyllabusResponse>> cloneSyllabus(@PathVariable UUID id) {
        SyllabusResponse syllabus = syllabusService.cloneSyllabus(id);
        return ResponseEntity.ok(ApiResponse.success("Syllabus cloned successfully", syllabus));
    }

    @Operation(summary = "Get syllabus versions", description = "Get all versions of a syllabus")
    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<SyllabusResponse>>> getSyllabusVersions(@PathVariable UUID id) {
        List<SyllabusResponse> versions = syllabusService.getSyllabusVersions(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @Operation(summary = "Compare two syllabi", description = "Compare two syllabus versions")
    @GetMapping("/{id}/compare/{otherId}")
    public ResponseEntity<ApiResponse<SyllabusCompareResponse>> compareSyllabi(
            @PathVariable UUID id, 
            @PathVariable UUID otherId) {
        SyllabusCompareResponse comparison = syllabusService.compareSyllabi(id, otherId);
        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    @Operation(summary = "Get syllabi by subject", description = "Get all syllabi for a specific subject")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<SyllabusResponse>>> getSyllabiBySubject(@PathVariable UUID subjectId) {
        List<SyllabusResponse> syllabi = syllabusService.getSyllabiBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(syllabi));
    }

    @Operation(summary = "Export syllabus to PDF", description = "Export syllabus to PDF format")
    @PostMapping("/{id}/export/pdf")
    public ResponseEntity<ApiResponse<byte[]>> exportSyllabusToPdf(@PathVariable UUID id) {
        byte[] pdfData = syllabusService.exportSyllabusToPdf(id);
        return ResponseEntity.ok(ApiResponse.success("PDF exported successfully", pdfData));
    }
}