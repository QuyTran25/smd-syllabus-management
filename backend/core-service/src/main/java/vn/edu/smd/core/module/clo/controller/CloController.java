package vn.edu.smd.core.module.clo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.clo.dto.CloRequest;
import vn.edu.smd.core.module.clo.dto.CloResponse;
import vn.edu.smd.core.module.clo.service.CloService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Course Outcome Management", description = "Course Learning Outcome management APIs")
@RestController
@RequestMapping("/api/course-outcomes")
@RequiredArgsConstructor
public class CloController {

    private final CloService cloService;

    @Operation(summary = "Get all course outcomes", description = "Get list of all course outcomes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CloResponse>>> getAllCourseOutcomes() {
        List<CloResponse> clos = cloService.getAllCourseOutcomes();
        return ResponseEntity.ok(ApiResponse.success(clos));
    }

    @Operation(summary = "Get CLOs by syllabus", description = "Get list of CLOs for a syllabus version")
    @GetMapping("/syllabus/{syllabusVersionId}")
    public ResponseEntity<ApiResponse<List<CloResponse>>> getClosBySyllabus(@PathVariable UUID syllabusVersionId) {
        List<CloResponse> clos = cloService.getClosBySyllabus(syllabusVersionId);
        return ResponseEntity.ok(ApiResponse.success(clos));
    }

    @Operation(summary = "Get CLO by ID", description = "Get CLO details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CloResponse>> getCloById(@PathVariable UUID id) {
        CloResponse clo = cloService.getCloById(id);
        return ResponseEntity.ok(ApiResponse.success(clo));
    }

    @Operation(summary = "Create CLO", description = "Create new course learning outcome")
    @PostMapping
    public ResponseEntity<ApiResponse<CloResponse>> createClo(@Valid @RequestBody CloRequest request) {
        CloResponse clo = cloService.createClo(request);
        return ResponseEntity.ok(ApiResponse.success("CLO created successfully", clo));
    }

    @Operation(summary = "Update CLO", description = "Update CLO information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CloResponse>> updateClo(
            @PathVariable UUID id, 
            @Valid @RequestBody CloRequest request) {
        CloResponse clo = cloService.updateClo(id, request);
        return ResponseEntity.ok(ApiResponse.success("CLO updated successfully", clo));
    }

    @Operation(summary = "Delete CLO", description = "Delete CLO by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClo(@PathVariable UUID id) {
        cloService.deleteClo(id);
        return ResponseEntity.ok(ApiResponse.success("CLO deleted successfully", null));
    }

    @Operation(summary = "Get outcomes for syllabus", description = "Get list of course outcomes for a syllabus")
    @GetMapping("/syllabi/{syllabusId}/outcomes")
    public ResponseEntity<ApiResponse<List<CloResponse>>> getOutcomesForSyllabus(@PathVariable UUID syllabusId) {
        List<CloResponse> outcomes = cloService.getClosBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(outcomes));
    }
}
