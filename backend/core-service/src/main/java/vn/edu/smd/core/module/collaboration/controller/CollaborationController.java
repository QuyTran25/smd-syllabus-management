package vn.edu.smd.core.module.collaboration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.collaboration.dto.CollaborationRequest;
import vn.edu.smd.core.module.collaboration.dto.CollaborationResponse;
import vn.edu.smd.core.module.collaboration.service.CollaborationService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Collaboration Session Management", description = "Syllabus collaboration session management APIs")
@RestController
@RequestMapping("/api/collaboration-sessions")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collaborationService;

    @Operation(summary = "Get all collaboration sessions", description = "Get list of all collaboration sessions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollaborationResponse>>> getAllCollaborationSessions() {
        List<CollaborationResponse> collaborations = collaborationService.getAllCollaborationSessions();
        return ResponseEntity.ok(ApiResponse.success(collaborations));
    }

    @Operation(summary = "Get collaborators by syllabus", description = "Get list of collaborators for a syllabus version")
    @GetMapping("/syllabus/{syllabusVersionId}")
    public ResponseEntity<ApiResponse<List<CollaborationResponse>>> getCollaboratorsBySyllabus(@PathVariable UUID syllabusVersionId) {
        List<CollaborationResponse> collaborators = collaborationService.getCollaboratorsBySyllabus(syllabusVersionId);
        return ResponseEntity.ok(ApiResponse.success(collaborators));
    }

    @Operation(summary = "Get collaborations by user", description = "Get list of syllabi where user is a collaborator")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CollaborationResponse>>> getCollaborationsByUser(@PathVariable UUID userId) {
        List<CollaborationResponse> collaborations = collaborationService.getCollaborationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(collaborations));
    }

    @Operation(summary = "Get collaborations for syllabus", description = "Get list of collaboration sessions for a syllabus")
    @GetMapping("/syllabi/{syllabusId}/collaborations")
    public ResponseEntity<ApiResponse<List<CollaborationResponse>>> getCollaborationsForSyllabus(@PathVariable UUID syllabusId) {
        List<CollaborationResponse> collaborations = collaborationService.getCollaboratorsBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(collaborations));
    }

    @Operation(summary = "Get collaboration by ID", description = "Get collaboration details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollaborationResponse>> getCollaborationById(@PathVariable UUID id) {
        CollaborationResponse collaboration = collaborationService.getCollaborationById(id);
        return ResponseEntity.ok(ApiResponse.success(collaboration));
    }

    @Operation(summary = "Create collaboration", description = "Add new collaborator to syllabus")
    @PostMapping
    public ResponseEntity<ApiResponse<CollaborationResponse>> createCollaboration(@Valid @RequestBody CollaborationRequest request) {
        CollaborationResponse collaboration = collaborationService.createCollaboration(request);
        return ResponseEntity.ok(ApiResponse.success("Collaborator added successfully", collaboration));
    }

    @Operation(summary = "Update collaboration", description = "Update collaboration information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollaborationResponse>> updateCollaboration(
            @PathVariable UUID id, 
            @Valid @RequestBody CollaborationRequest request) {
        CollaborationResponse collaboration = collaborationService.updateCollaboration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Collaboration updated successfully", collaboration));
    }

    @Operation(summary = "Delete collaboration", description = "Remove collaborator from syllabus")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCollaboration(@PathVariable UUID id) {
        collaborationService.deleteCollaboration(id);
        return ResponseEntity.ok(ApiResponse.success("Collaborator removed successfully", null));
    }

    @Operation(summary = "Close collaboration", description = "Close a collaboration session")
    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<CollaborationResponse>> closeCollaboration(@PathVariable UUID id) {
        CollaborationResponse collaboration = collaborationService.closeCollaboration(id);
        return ResponseEntity.ok(ApiResponse.success("Collaboration session closed successfully", collaboration));
    }
}
