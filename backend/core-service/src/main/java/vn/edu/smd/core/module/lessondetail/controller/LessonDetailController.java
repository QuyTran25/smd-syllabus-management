package vn.edu.smd.core.module.lessondetail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.lessondetail.dto.LessonDetailRequest;
import vn.edu.smd.core.module.lessondetail.dto.LessonDetailResponse;
import vn.edu.smd.core.module.lessondetail.service.LessonDetailService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Lesson Detail Management", description = "Lesson detail management APIs")
@RestController
@RequestMapping("/api/lesson-details")
@RequiredArgsConstructor
public class LessonDetailController {

    private final LessonDetailService lessonDetailService;

    @Operation(summary = "Get all lesson details", description = "Get list of all lesson details")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonDetailResponse>>> getAllDetails() {
        List<LessonDetailResponse> details = lessonDetailService.getAllDetails();
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @Operation(summary = "Get lesson detail by ID", description = "Get lesson detail by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getDetailById(@PathVariable UUID id) {
        LessonDetailResponse detail = lessonDetailService.getDetailById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "Create lesson detail", description = "Create new lesson detail")
    @PostMapping
    public ResponseEntity<ApiResponse<LessonDetailResponse>> createDetail(@Valid @RequestBody LessonDetailRequest request) {
        LessonDetailResponse detail = lessonDetailService.createDetail(request);
        return ResponseEntity.ok(ApiResponse.success("Lesson detail created successfully", detail));
    }

    @Operation(summary = "Update lesson detail", description = "Update existing lesson detail")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> updateDetail(
            @PathVariable UUID id,
            @Valid @RequestBody LessonDetailRequest request) {
        LessonDetailResponse detail = lessonDetailService.updateDetail(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson detail updated successfully", detail));
    }

    @Operation(summary = "Delete lesson detail", description = "Delete lesson detail by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDetail(@PathVariable UUID id) {
        lessonDetailService.deleteDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson detail deleted successfully", null));
    }

    @Operation(summary = "Get details by lesson plan", description = "Get all details for a specific lesson plan")
    @GetMapping("/lesson-plans/{planId}/details")
    public ResponseEntity<ApiResponse<List<LessonDetailResponse>>> getDetailsByPlanId(@PathVariable UUID planId) {
        List<LessonDetailResponse> details = lessonDetailService.getDetailsByPlanId(planId);
        return ResponseEntity.ok(ApiResponse.success(details));
    }
}
