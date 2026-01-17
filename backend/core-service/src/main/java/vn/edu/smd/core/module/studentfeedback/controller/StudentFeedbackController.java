package vn.edu.smd.core.module.studentfeedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.studentfeedback.dto.AdminResponseRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackRequest;
import vn.edu.smd.core.module.studentfeedback.dto.StudentFeedbackResponse;
import vn.edu.smd.core.module.studentfeedback.service.StudentFeedbackService;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Tag(name = "Student Feedback Management", description = "APIs for managing student feedback on syllabi")
@RestController
@RequestMapping("/api/student-feedbacks")
@RequiredArgsConstructor
public class StudentFeedbackController {

    private final StudentFeedbackService feedbackService;
    private final UserRepository userRepository;

    @Operation(summary = "Get all feedbacks", description = "Get list of all student feedbacks with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentFeedbackResponse>>> getAllFeedbacks(Pageable pageable) {
        Page<StudentFeedbackResponse> feedbacks = feedbackService.getAllFeedbacks(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(feedbacks)));
    }

    @Operation(summary = "Get feedbacks by status", description = "Get feedbacks filtered by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<StudentFeedbackResponse>>> getFeedbacksByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Page<StudentFeedbackResponse> feedbacks = feedbackService.getFeedbacksByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(feedbacks)));
    }

    @Operation(summary = "Get feedback by ID", description = "Get feedback details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentFeedbackResponse>> getFeedbackById(@PathVariable UUID id) {
        StudentFeedbackResponse feedback = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @Operation(summary = "Get feedbacks by syllabus", description = "Get all feedbacks for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}")
    public ResponseEntity<ApiResponse<List<StudentFeedbackResponse>>> getFeedbacksBySyllabus(
            @PathVariable UUID syllabusId) {
        List<StudentFeedbackResponse> feedbacks = feedbackService.getFeedbacksBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    @Operation(summary = "Create feedback", description = "Student creates a new feedback")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentFeedbackResponse>> createFeedback(
            @Valid @RequestBody StudentFeedbackRequest request) {
        UUID studentId = getCurrentUserId();
        StudentFeedbackResponse feedback = feedbackService.createFeedback(request, studentId);
        return ResponseEntity.ok(ApiResponse.success("Feedback created successfully", feedback));
    }

    @Operation(summary = "Respond to feedback", description = "Admin responds to a student feedback")
    @PostMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<StudentFeedbackResponse>> respondToFeedback(
            @PathVariable UUID id,
            @RequestBody AdminResponseRequest request) {
        UUID adminId = getCurrentUserId();
        StudentFeedbackResponse feedback = feedbackService.respondToFeedback(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success("Response sent successfully", feedback));
    }

    @Operation(summary = "Enable edit for lecturer", description = "Admin enables editing for lecturer to fix the syllabus")
    @PostMapping("/{id}/enable-edit")
    public ResponseEntity<ApiResponse<StudentFeedbackResponse>> enableEditForLecturer(
            @PathVariable UUID id) {
        UUID adminId = getCurrentUserId();
        StudentFeedbackResponse feedback = feedbackService.enableEditForLecturer(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Edit enabled for lecturer", feedback));
    }

    @Operation(summary = "Update feedback status", description = "Update the status of a feedback")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StudentFeedbackResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        StudentFeedbackResponse feedback = feedbackService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", feedback));
    }

    private UUID getCurrentUserId() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("User not found in token"));
    }}