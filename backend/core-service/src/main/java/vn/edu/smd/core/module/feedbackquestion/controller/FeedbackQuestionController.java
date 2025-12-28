package vn.edu.smd.core.module.feedbackquestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionListRequest;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionRequest;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionResponse;
import vn.edu.smd.core.module.feedbackquestion.service.FeedbackQuestionService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Feedback Question Management", description = "Feedback question management APIs")
@RestController
@RequestMapping("/api/feedback-questions")
@RequiredArgsConstructor
public class FeedbackQuestionController {

    private final FeedbackQuestionService feedbackQuestionService;

    @Operation(summary = "Get all feedback questions", description = "Get list of feedback questions with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedbackQuestionResponse>>> getAllFeedbackQuestions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Boolean isRequired) {
        
        FeedbackQuestionListRequest request = new FeedbackQuestionListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        request.setQuestionType(questionType);
        request.setIsRequired(isRequired);
        
        Page<FeedbackQuestionResponse> feedbackQuestions = feedbackQuestionService.getAllFeedbackQuestions(request);
        return ResponseEntity.ok(ApiResponse.success(feedbackQuestions));
    }

    @Operation(summary = "Get feedback question by ID", description = "Get feedback question details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackQuestionResponse>> getFeedbackQuestionById(@PathVariable UUID id) {
        FeedbackQuestionResponse feedbackQuestion = feedbackQuestionService.getFeedbackQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success(feedbackQuestion));
    }

    @Operation(summary = "Get feedback questions by syllabus", description = "Get all feedback questions for a specific syllabus version")
    @GetMapping("/syllabus/{syllabusId}/feedback-questions")
    public ResponseEntity<ApiResponse<List<FeedbackQuestionResponse>>> getFeedbackQuestionsBySyllabus(@PathVariable UUID syllabusId) {
        List<FeedbackQuestionResponse> feedbackQuestions = feedbackQuestionService.getFeedbackQuestionsBySyllabus(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(feedbackQuestions));
    }

    @Operation(summary = "Create feedback question", description = "Create new feedback question")
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackQuestionResponse>> createFeedbackQuestion(@Valid @RequestBody FeedbackQuestionRequest request) {
        FeedbackQuestionResponse feedbackQuestion = feedbackQuestionService.createFeedbackQuestion(request);
        return ResponseEntity.ok(ApiResponse.success("Feedback question created successfully", feedbackQuestion));
    }

    @Operation(summary = "Update feedback question", description = "Update feedback question information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackQuestionResponse>> updateFeedbackQuestion(
            @PathVariable UUID id, 
            @Valid @RequestBody FeedbackQuestionRequest request) {
        FeedbackQuestionResponse feedbackQuestion = feedbackQuestionService.updateFeedbackQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback question updated successfully", feedbackQuestion));
    }

    @Operation(summary = "Delete feedback question", description = "Delete feedback question by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedbackQuestion(@PathVariable UUID id) {
        feedbackQuestionService.deleteFeedbackQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("Feedback question deleted successfully", null));
    }
}
