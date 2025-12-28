package vn.edu.smd.core.module.reviewcomment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.ReviewComment;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.reviewcomment.dto.ReviewCommentRequest;
import vn.edu.smd.core.module.reviewcomment.dto.ReviewCommentResponse;
import vn.edu.smd.core.repository.ReviewCommentRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewCommentService {

    private final ReviewCommentRepository commentRepository;
    private final SyllabusVersionRepository syllabusRepository;

    public List<ReviewCommentResponse> getCommentsBySyllabus(UUID syllabusVersionId) {
        if (!syllabusRepository.existsById(syllabusVersionId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusVersionId);
        }
        return commentRepository.findBySyllabusVersionIdOrderByCreatedAtDesc(syllabusVersionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReviewCommentResponse getCommentById(UUID id) {
        ReviewComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewComment", "id", id));
        return mapToResponse(comment);
    }

    @Transactional
    public ReviewCommentResponse createComment(ReviewCommentRequest request) {
        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        ReviewComment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReviewComment", "id", request.getParentId()));
        }

        ReviewComment comment = ReviewComment.builder()
                .syllabusVersion(syllabus)
                .section(request.getSection())
                .content(request.getContent())
                .isResolved(request.getIsResolved() != null ? request.getIsResolved() : false)
                .parent(parent)
                .build();

        ReviewComment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Transactional
    public ReviewCommentResponse updateComment(UUID id, ReviewCommentRequest request) {
        ReviewComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewComment", "id", id));

        comment.setSection(request.getSection());
        comment.setContent(request.getContent());
        if (request.getIsResolved() != null) {
            comment.setIsResolved(request.getIsResolved());
        }

        ReviewComment updatedComment = commentRepository.save(comment);
        return mapToResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("ReviewComment", "id", id);
        }
        commentRepository.deleteById(id);
    }

    private ReviewCommentResponse mapToResponse(ReviewComment comment) {
        ReviewCommentResponse response = new ReviewCommentResponse();
        response.setId(comment.getId());
        response.setSyllabusVersionId(comment.getSyllabusVersion().getId());
        response.setSection(comment.getSection());
        response.setContent(comment.getContent());
        response.setIsResolved(comment.getIsResolved());
        if (comment.getParent() != null) {
            response.setParentId(comment.getParent().getId());
        }
        if (comment.getCreatedBy() != null) {
            response.setCreatedBy(comment.getCreatedBy().getId());
            response.setCreatedByName(comment.getCreatedBy().getFullName());
        }
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
