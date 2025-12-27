package vn.edu.smd.core.module.teachingassignment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.teachingassignment.dto.TeachingAssignmentResponse;
import vn.edu.smd.core.module.teachingassignment.service.TeachingAssignmentService;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Teaching Assignment management
 */
@Tag(name = "Teaching Assignment", description = "Teaching assignment management APIs for HoD")
@RestController
@RequestMapping("/api/teaching-assignments")
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @Operation(summary = "Get all teaching assignments", 
               description = "Get list of teaching assignments with pagination and filtering")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TeachingAssignmentResponse>>> getAllAssignments(
            Pageable pageable,
            @RequestParam(required = false) List<String> status) {
        Page<TeachingAssignmentResponse> assignments = 
            teachingAssignmentService.getAllAssignments(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(assignments)));
    }

    @Operation(summary = "Get teaching assignment by ID", 
               description = "Get teaching assignment details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> getAssignmentById(
            @PathVariable UUID id) {
        TeachingAssignmentResponse assignment = teachingAssignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    @Operation(summary = "Get assignments by lecturer", 
               description = "Get all assignments for a specific lecturer")
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<ApiResponse<List<TeachingAssignmentResponse>>> getAssignmentsByLecturer(
            @PathVariable UUID lecturerId) {
        List<TeachingAssignmentResponse> assignments = 
            teachingAssignmentService.getAssignmentsByLecturer(lecturerId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
}
