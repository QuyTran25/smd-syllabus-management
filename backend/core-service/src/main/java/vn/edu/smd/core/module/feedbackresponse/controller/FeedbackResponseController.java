package vn.edu.smd.core.module.feedbackresponse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.feedbackresponse.dto.FeedbackResponseListRequest;
import vn.edu.smd.core.module.feedbackresponse.dto.FeedbackResponseRequest;
import vn.edu.smd.core.module.feedbackresponse.dto.FeedbackResponseResponse;
import vn.edu.smd.core.module.feedbackresponse.dto.FeedbackSummaryResponse;
import vn.edu.smd.core.module.feedbackresponse.service.FeedbackResponseService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Feedback Response Management", description = "Feedback response management APIs")
@RestController
@RequestMapping("/api/feedback-responses")
@RequiredArgsConstructor
public class FeedbackResponseController {

    private final FeedbackResponseService feedbackResponseService;

    @Operation(summary = "Get all feedback responses", description = "Get list of feedback responses with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedbackResponseResponse>>> getAllFeedbackResponses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        FeedbackResponseListRequest request = new FeedbackResponseListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        
        Page<FeedbackResponseResponse> feedbackResponses = feedbackResponseService.getAllFeedbackResponses(request);
        return ResponseEntity.ok(ApiResponse.success(feedbackResponses));
    }

    @Operation(summary = "Get feedback response by ID", description = "Get feedback response details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponseResponse>> getFeedbackResponseById(@PathVariable UUID id) {
        FeedbackResponseResponse feedbackResponse = feedbackResponseService.getFeedbackResponseById(id);
        return ResponseEntity.ok(ApiResponse.success(feedbackResponse));
    }

    @Operation(summary = "Create feedback response", description = "Create new feedback response")
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponseResponse>> createFeedbackResponse(@Valid @RequestBody FeedbackResponseRequest request) {
        FeedbackResponseResponse feedbackResponse = feedbackResponseService.createFeedbackResponse(request);
        return ResponseEntity.ok(ApiResponse.success("Feedback response created successfully", feedbackResponse));
    }

    @Operation(summary = "Update feedback response", description = "Update feedback response information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackResponseResponse>> updateFeedbackResponse(
            @PathVariable UUID id, 
            @Valid @RequestBody FeedbackResponseRequest request) {
        FeedbackResponseResponse feedbackResponse = feedbackResponseService.updateFeedbackResponse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback response updated successfully", feedbackResponse));
    }

    @Operation(summary = "Delete feedback response", description = "Delete feedback response by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedbackResponse(@PathVariable UUID id) {
        feedbackResponseService.deleteFeedbackResponse(id);
        return ResponseEntity.ok(ApiResponse.success("Feedback response deleted successfully", null));
    }

    @Operation(summary = "Get responses by question", description = "Get all feedback responses for a specific question")
    @GetMapping("/feedback-questions/{questionId}/responses")
    public ResponseEntity<ApiResponse<List<FeedbackResponseResponse>>> getFeedbackResponsesByQuestion(@PathVariable UUID questionId) {
        List<FeedbackResponseResponse> feedbackResponses = feedbackResponseService.getFeedbackResponsesByQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success(feedbackResponses));
    }

    @Operation(summary = "Get feedback summary by syllabus", description = "Get aggregated feedback summary for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}/feedback-summary")
    public ResponseEntity<ApiResponse<FeedbackSummaryResponse>> getFeedbackSummaryBySyllabus(@PathVariable UUID syllabusId) {
        FeedbackSummaryResponse summary = feedbackResponseService.getFeedbackSummaryBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
