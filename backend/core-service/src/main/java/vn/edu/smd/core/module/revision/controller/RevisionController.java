package vn.edu.smd.core.module.revision.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.revision.dto.*;
import vn.edu.smd.core.module.revision.service.RevisionService;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * Revision Controller
 * Handles post-publication revision workflow
 */
@Tag(name = "Revision Management", description = "APIs for post-publication revision workflow")
@RestController
@RequestMapping("/api/revisions")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;
    private final UserRepository userRepository;

    @Operation(summary = "Start revision session", description = "Admin starts a revision session to fix published syllabus")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> startRevision(
            @Valid @RequestBody StartRevisionRequest request) {
        UUID adminId = getCurrentUserId();
        RevisionSessionResponse response = revisionService.startRevisionSession(request, adminId);
        return ResponseEntity.ok(ApiResponse.success("Revision session started successfully", response));
    }

    @Operation(summary = "Submit revision", description = "Lecturer submits revision to HOD for approval")
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> submitRevision(
            @Valid @RequestBody SubmitRevisionRequest request) {
        UUID lecturerId = getCurrentUserId();
        RevisionSessionResponse response = revisionService.submitRevision(request, lecturerId);
        return ResponseEntity.ok(ApiResponse.success("Revision submitted successfully", response));
    }

    @Operation(summary = "Review revision", description = "HOD reviews and approves/rejects revision")
    @PostMapping("/review")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> reviewRevision(
            @Valid @RequestBody ReviewRevisionRequest request) {
        UUID hodId = getCurrentUserId();
        RevisionSessionResponse response = revisionService.reviewRevision(request, hodId);
        return ResponseEntity.ok(ApiResponse.success("Revision reviewed successfully", response));
    }

    @Operation(summary = "Republish syllabus", description = "Admin republishes syllabus after HOD approval")
    @PostMapping("/{sessionId}/republish")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> republishSyllabus(
            @PathVariable UUID sessionId) {
        UUID adminId = getCurrentUserId();
        RevisionSessionResponse response = revisionService.republishSyllabus(sessionId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Syllabus republished successfully", response));
    }

    @Operation(summary = "Get pending HOD reviews", description = "Get all revision sessions pending HOD review")
    @GetMapping("/pending-hod")
    public ResponseEntity<ApiResponse<List<RevisionSessionResponse>>> getPendingHodReview() {
        List<RevisionSessionResponse> sessions = revisionService.getPendingHodReview();
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "Get pending republish", description = "Get all revision sessions pending admin republish")
    @GetMapping("/pending-republish")
    public ResponseEntity<ApiResponse<List<RevisionSessionResponse>>> getPendingRepublish() {
        List<RevisionSessionResponse> sessions = revisionService.getPendingRepublish();
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "Get active revision session by syllabus", description = "Get active revision session for a syllabus version")
    @GetMapping("/syllabus/{syllabusId}/active")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> getActiveRevisionSession(
            @PathVariable UUID syllabusId) {
        RevisionSessionResponse session = revisionService.getActiveRevisionSession(syllabusId);
        if (session == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("No active revision session found"));
        }
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @Operation(summary = "Get completed revision session for republishing", description = "Get completed revision session waiting for admin to republish")
    @GetMapping("/syllabus/{syllabusId}/completed")
    public ResponseEntity<ApiResponse<RevisionSessionResponse>> getCompletedRevisionSession(
            @PathVariable UUID syllabusId) {
        RevisionSessionResponse session = revisionService.getCompletedRevisionSession(syllabusId);
        if (session == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("No completed revision session found"));
        }
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    private UUID getCurrentUserId() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("User not found in token"));
    }
}
