package vn.edu.smd.core.module.feedbackquestion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.FeedbackQuestion;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionListRequest;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionRequest;
import vn.edu.smd.core.module.feedbackquestion.dto.FeedbackQuestionResponse;
import vn.edu.smd.core.repository.FeedbackQuestionRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackQuestionService {

    private final FeedbackQuestionRepository feedbackQuestionRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public Page<FeedbackQuestionResponse> getAllFeedbackQuestions(FeedbackQuestionListRequest request) {
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<FeedbackQuestion> feedbackQuestions = feedbackQuestionRepository.findAll(pageable);
        return feedbackQuestions.map(this::mapToResponse);
    }

    public FeedbackQuestionResponse getFeedbackQuestionById(UUID id) {
        FeedbackQuestion feedbackQuestion = feedbackQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackQuestion", "id", id));
        return mapToResponse(feedbackQuestion);
    }

    public List<FeedbackQuestionResponse> getFeedbackQuestionsBySyllabus(UUID syllabusId) {
        if (!syllabusVersionRepository.existsById(syllabusId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusId);
        }
        return feedbackQuestionRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackQuestionResponse createFeedbackQuestion(FeedbackQuestionRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        FeedbackQuestion feedbackQuestion = FeedbackQuestion.builder()
                .syllabusVersion(syllabusVersion)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .options(request.getOptions())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .displayOrder(request.getDisplayOrder())
                .build();

        FeedbackQuestion savedFeedbackQuestion = feedbackQuestionRepository.save(feedbackQuestion);
        return mapToResponse(savedFeedbackQuestion);
    }

    @Transactional
    public FeedbackQuestionResponse updateFeedbackQuestion(UUID id, FeedbackQuestionRequest request) {
        FeedbackQuestion feedbackQuestion = feedbackQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackQuestion", "id", id));

        feedbackQuestion.setQuestionText(request.getQuestionText());
        feedbackQuestion.setQuestionType(request.getQuestionType());
        feedbackQuestion.setOptions(request.getOptions());
        
        if (request.getIsRequired() != null) {
            feedbackQuestion.setIsRequired(request.getIsRequired());
        }
        
        feedbackQuestion.setDisplayOrder(request.getDisplayOrder());

        FeedbackQuestion updatedFeedbackQuestion = feedbackQuestionRepository.save(feedbackQuestion);
        return mapToResponse(updatedFeedbackQuestion);
    }

    @Transactional
    public void deleteFeedbackQuestion(UUID id) {
        if (!feedbackQuestionRepository.existsById(id)) {
            throw new ResourceNotFoundException("FeedbackQuestion", "id", id);
        }
        feedbackQuestionRepository.deleteById(id);
    }

    private FeedbackQuestionResponse mapToResponse(FeedbackQuestion feedbackQuestion) {
        FeedbackQuestionResponse response = new FeedbackQuestionResponse();
        response.setId(feedbackQuestion.getId());
        response.setSyllabusVersionId(feedbackQuestion.getSyllabusVersion().getId());
        response.setQuestionText(feedbackQuestion.getQuestionText());
        response.setQuestionType(feedbackQuestion.getQuestionType());
        response.setOptions(feedbackQuestion.getOptions());
        response.setIsRequired(feedbackQuestion.getIsRequired());
        response.setDisplayOrder(feedbackQuestion.getDisplayOrder());
        response.setCreatedAt(feedbackQuestion.getCreatedAt());
        response.setUpdatedAt(feedbackQuestion.getUpdatedAt());
        
        return response;
    }
}
