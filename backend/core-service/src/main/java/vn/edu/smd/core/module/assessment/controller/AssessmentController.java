package vn.edu.smd.core.module.assessment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.assessment.dto.AssessmentRequest;
import vn.edu.smd.core.module.assessment.dto.AssessmentResponse;
import vn.edu.smd.core.module.assessment.service.AssessmentService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Assessment Method Management", description = "Assessment method management APIs")
@RestController
@RequestMapping("/api/assessment-methods")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @Operation(summary = "Get assessments by syllabus", description = "Get list of assessments for a syllabus version")
    @GetMapping("/syllabus/{syllabusVersionId}")
    public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getAssessmentsBySyllabus(@PathVariable UUID syllabusVersionId) {
        List<AssessmentResponse> assessments = assessmentService.getAssessmentsBySyllabus(syllabusVersionId);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @Operation(summary = "Get assessment by ID", description = "Get assessment details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessmentById(@PathVariable UUID id) {
        AssessmentResponse assessment = assessmentService.getAssessmentById(id);
        return ResponseEntity.ok(ApiResponse.success(assessment));
    }

    @Operation(summary = "Create assessment", description = "Create new assessment scheme")
    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(@Valid @RequestBody AssessmentRequest request) {
        AssessmentResponse assessment = assessmentService.createAssessment(request);
        return ResponseEntity.ok(ApiResponse.success("Assessment created successfully", assessment));
    }

    @Operation(summary = "Update assessment", description = "Update assessment information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> updateAssessment(
            @PathVariable UUID id, 
            @Valid @RequestBody AssessmentRequest request) {
        AssessmentResponse assessment = assessmentService.updateAssessment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Assessment updated successfully", assessment));
    }

    @Operation(summary = "Delete assessment", description = "Delete assessment by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssessment(@PathVariable UUID id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment deleted successfully", null));
    }

    @Operation(summary = "Validate assessment weight", description = "Validate that total weight of assessments for a syllabus equals 100%")
    @GetMapping("/{id}/validate-weight")
    public ResponseEntity<ApiResponse<Boolean>> validateAssessmentWeight(@PathVariable UUID id) {
        Boolean isValid = assessmentService.validateAssessmentWeight(id);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}
