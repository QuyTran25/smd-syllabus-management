package vn.edu.smd.core.module.reviewcomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.reviewcomment.dto.ReviewCommentRequest;
import vn.edu.smd.core.module.reviewcomment.dto.ReviewCommentResponse;
import vn.edu.smd.core.module.reviewcomment.service.ReviewCommentService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Review Comment Management", description = "Review comment management APIs")
@RestController
@RequestMapping("/api/review-comments")
@RequiredArgsConstructor
public class ReviewCommentController {

    private final ReviewCommentService commentService;

    @Operation(summary = "Get comments by syllabus", description = "Get list of review comments for a syllabus version")
    @GetMapping("/syllabus/{syllabusVersionId}")
    public ResponseEntity<ApiResponse<List<ReviewCommentResponse>>> getCommentsBySyllabus(@PathVariable UUID syllabusVersionId) {
        List<ReviewCommentResponse> comments = commentService.getCommentsBySyllabus(syllabusVersionId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @Operation(summary = "Get comment by ID", description = "Get review comment details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewCommentResponse>> getCommentById(@PathVariable UUID id) {
        ReviewCommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @Operation(summary = "Create comment", description = "Create new review comment")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewCommentResponse>> createComment(@Valid @RequestBody ReviewCommentRequest request) {
        ReviewCommentResponse comment = commentService.createComment(request);
        return ResponseEntity.ok(ApiResponse.success("Review comment created successfully", comment));
    }

    @Operation(summary = "Update comment", description = "Update review comment information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewCommentResponse>> updateComment(
            @PathVariable UUID id, 
            @Valid @RequestBody ReviewCommentRequest request) {
        ReviewCommentResponse comment = commentService.updateComment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Review comment updated successfully", comment));
    }

    @Operation(summary = "Delete comment", description = "Delete review comment by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Review comment deleted successfully", null));
    }
}
