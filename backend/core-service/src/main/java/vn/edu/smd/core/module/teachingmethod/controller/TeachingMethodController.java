package vn.edu.smd.core.module.teachingmethod.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.teachingmethod.dto.TeachingMethodRequest;
import vn.edu.smd.core.module.teachingmethod.dto.TeachingMethodResponse;
import vn.edu.smd.core.module.teachingmethod.service.TeachingMethodService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Teaching Method Management", description = "Teaching method management APIs")
@RestController
@RequestMapping("/api/teaching-methods")
@RequiredArgsConstructor
public class TeachingMethodController {

    private final TeachingMethodService teachingMethodService;

    @Operation(summary = "Get all teaching methods", description = "Get list of all teaching methods")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeachingMethodResponse>>> getAllMethods() {
        List<TeachingMethodResponse> methods = teachingMethodService.getAllMethods();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @Operation(summary = "Get method by ID", description = "Get teaching method details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingMethodResponse>> getMethodById(@PathVariable UUID id) {
        TeachingMethodResponse method = teachingMethodService.getMethodById(id);
        return ResponseEntity.ok(ApiResponse.success(method));
    }

    @Operation(summary = "Create teaching method", description = "Create new teaching method")
    @PostMapping
    public ResponseEntity<ApiResponse<TeachingMethodResponse>> createMethod(@Valid @RequestBody TeachingMethodRequest request) {
        TeachingMethodResponse method = teachingMethodService.createMethod(request);
        return ResponseEntity.ok(ApiResponse.success("Teaching method created successfully", method));
    }

    @Operation(summary = "Update teaching method", description = "Update existing teaching method")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingMethodResponse>> updateMethod(
            @PathVariable UUID id,
            @Valid @RequestBody TeachingMethodRequest request) {
        TeachingMethodResponse method = teachingMethodService.updateMethod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Teaching method updated successfully", method));
    }

    @Operation(summary = "Delete teaching method", description = "Delete teaching method by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMethod(@PathVariable UUID id) {
        teachingMethodService.deleteMethod(id);
        return ResponseEntity.ok(ApiResponse.success("Teaching method deleted successfully", null));
    }

    @Operation(summary = "Get methods by syllabus", description = "Get all teaching methods for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}/teaching-methods")
    public ResponseEntity<ApiResponse<List<TeachingMethodResponse>>> getMethodsBySyllabusId(@PathVariable UUID syllabusId) {
        List<TeachingMethodResponse> methods = teachingMethodService.getMethodsBySyllabusId(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
}
