package vn.edu.smd.core.module.subjectcomponent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.subjectcomponent.dto.SubjectComponentRequest;
import vn.edu.smd.core.module.subjectcomponent.dto.SubjectComponentResponse;
import vn.edu.smd.core.module.subjectcomponent.service.SubjectComponentService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Subject Component Management", description = "Subject component management APIs")
@RestController
@RequestMapping("/api/subject-components")
@RequiredArgsConstructor
public class SubjectComponentController {

    private final SubjectComponentService subjectComponentService;

    @Operation(summary = "Get all subject components", description = "Get list of all subject components")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectComponentResponse>>> getAllComponents() {
        List<SubjectComponentResponse> components = subjectComponentService.getAllComponents();
        return ResponseEntity.ok(ApiResponse.success(components));
    }

    @Operation(summary = "Get component by ID", description = "Get subject component details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectComponentResponse>> getComponentById(@PathVariable UUID id) {
        SubjectComponentResponse component = subjectComponentService.getComponentById(id);
        return ResponseEntity.ok(ApiResponse.success(component));
    }

    @Operation(summary = "Create subject component", description = "Create new subject component")
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectComponentResponse>> createComponent(@Valid @RequestBody SubjectComponentRequest request) {
        SubjectComponentResponse component = subjectComponentService.createComponent(request);
        return ResponseEntity.ok(ApiResponse.success("Subject component created successfully", component));
    }

    @Operation(summary = "Update subject component", description = "Update existing subject component")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectComponentResponse>> updateComponent(
            @PathVariable UUID id,
            @Valid @RequestBody SubjectComponentRequest request) {
        SubjectComponentResponse component = subjectComponentService.updateComponent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject component updated successfully", component));
    }

    @Operation(summary = "Delete subject component", description = "Delete subject component by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(@PathVariable UUID id) {
        subjectComponentService.deleteComponent(id);
        return ResponseEntity.ok(ApiResponse.success("Subject component deleted successfully", null));
    }

    @Operation(summary = "Get components by syllabus", description = "Get all components for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}/components")
    public ResponseEntity<ApiResponse<List<SubjectComponentResponse>>> getComponentsBySyllabusId(@PathVariable UUID syllabusId) {
        List<SubjectComponentResponse> components = subjectComponentService.getComponentsBySyllabusId(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(components));
    }
}
