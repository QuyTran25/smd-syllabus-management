package vn.edu.smd.core.module.collaborationchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeListRequest;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeRequest;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeResponse;
import vn.edu.smd.core.module.collaborationchange.service.CollaborationChangeService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Collaboration Change Management", description = "Collaboration change tracking APIs")
@RestController
@RequestMapping("/api/collaboration-changes")
@RequiredArgsConstructor
public class CollaborationChangeController {

    private final CollaborationChangeService collaborationChangeService;

    @Operation(summary = "Get all collaboration changes", description = "Get list of collaboration changes with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CollaborationChangeResponse>>> getAllCollaborationChanges(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String changeType) {
        
        CollaborationChangeListRequest request = new CollaborationChangeListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        request.setChangeType(changeType);
        
        Page<CollaborationChangeResponse> changes = collaborationChangeService.getAllCollaborationChanges(request);
        return ResponseEntity.ok(ApiResponse.success(changes));
    }

    @Operation(summary = "Get collaboration change by ID", description = "Get collaboration change details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollaborationChangeResponse>> getCollaborationChangeById(@PathVariable UUID id) {
        CollaborationChangeResponse change = collaborationChangeService.getCollaborationChangeById(id);
        return ResponseEntity.ok(ApiResponse.success(change));
    }

    @Operation(summary = "Get changes by collaboration session", description = "Get all changes for a specific collaboration session")
    @GetMapping("/collaboration-sessions/{sessionId}/changes")
    public ResponseEntity<ApiResponse<List<CollaborationChangeResponse>>> getChangesByCollaborationSession(@PathVariable UUID sessionId) {
        List<CollaborationChangeResponse> changes = collaborationChangeService.getChangesByCollaborationSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(changes));
    }

    @Operation(summary = "Record collaboration change", description = "Create/record a new collaboration change")
    @PostMapping
    public ResponseEntity<ApiResponse<CollaborationChangeResponse>> createCollaborationChange(@Valid @RequestBody CollaborationChangeRequest request) {
        CollaborationChangeResponse change = collaborationChangeService.createCollaborationChange(request);
        return ResponseEntity.ok(ApiResponse.success("Collaboration change recorded successfully", change));
    }
}
