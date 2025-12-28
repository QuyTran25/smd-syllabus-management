package vn.edu.smd.core.module.prerequisite.controller;

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
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteRequest;
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse;
import vn.edu.smd.core.module.prerequisite.service.PrerequisiteService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Prerequisite Management", description = "Subject prerequisite management APIs")
@RestController
@RequestMapping("/api/prerequisites")
@RequiredArgsConstructor
public class PrerequisiteController {

    private final PrerequisiteService prerequisiteService;

    @Operation(summary = "Get all prerequisites with pagination", description = "Get list of all prerequisites with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PrerequisiteResponse>>> getAllPrerequisites(Pageable pageable) {
        Page<PrerequisiteResponse> prerequisites = prerequisiteService.getAllPrerequisites(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(prerequisites)));
    }

    @Operation(summary = "Get prerequisites by subject", description = "Get list of prerequisites for a subject")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<PrerequisiteResponse>>> getPrerequisitesBySubject(@PathVariable UUID subjectId) {
        List<PrerequisiteResponse> prerequisites = prerequisiteService.getPrerequisitesBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(prerequisites));
    }

    @Operation(summary = "Create prerequisite", description = "Create new subject prerequisite relationship")
    @PostMapping
    public ResponseEntity<ApiResponse<PrerequisiteResponse>> createPrerequisite(@Valid @RequestBody PrerequisiteRequest request) {
        PrerequisiteResponse prerequisite = prerequisiteService.createPrerequisite(request);
        return ResponseEntity.ok(ApiResponse.success("Prerequisite created successfully", prerequisite));
    }

    @Operation(summary = "Delete prerequisite", description = "Delete prerequisite relationship by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrerequisite(@PathVariable UUID id) {
        prerequisiteService.deletePrerequisite(id);
        return ResponseEntity.ok(ApiResponse.success("Prerequisite deleted successfully", null));
    }
}
