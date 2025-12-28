package vn.edu.smd.core.module.lessonplan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.lessonplan.dto.LessonPlanRequest;
import vn.edu.smd.core.module.lessonplan.dto.LessonPlanResponse;
import vn.edu.smd.core.module.lessonplan.service.LessonPlanService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Lesson Plan Management", description = "Lesson plan management APIs")
@RestController
@RequestMapping("/api/lesson-plans")
@RequiredArgsConstructor
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;

    @Operation(summary = "Get all lesson plans", description = "Get list of all lesson plans")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonPlanResponse>>> getAllPlans() {
        List<LessonPlanResponse> plans = lessonPlanService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @Operation(summary = "Get lesson plan by ID", description = "Get lesson plan details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonPlanResponse>> getPlanById(@PathVariable UUID id) {
        LessonPlanResponse plan = lessonPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @Operation(summary = "Create lesson plan", description = "Create new lesson plan")
    @PostMapping
    public ResponseEntity<ApiResponse<LessonPlanResponse>> createPlan(@Valid @RequestBody LessonPlanRequest request) {
        LessonPlanResponse plan = lessonPlanService.createPlan(request);
        return ResponseEntity.ok(ApiResponse.success("Lesson plan created successfully", plan));
    }

    @Operation(summary = "Update lesson plan", description = "Update existing lesson plan")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonPlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody LessonPlanRequest request) {
        LessonPlanResponse plan = lessonPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson plan updated successfully", plan));
    }

    @Operation(summary = "Delete lesson plan", description = "Delete lesson plan by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        lessonPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson plan deleted successfully", null));
    }

    @Operation(summary = "Get plans by syllabus", description = "Get all lesson plans for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}/lesson-plans")
    public ResponseEntity<ApiResponse<List<LessonPlanResponse>>> getPlansBySyllabusId(@PathVariable UUID syllabusId) {
        List<LessonPlanResponse> plans = lessonPlanService.getPlansBySyllabusId(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
