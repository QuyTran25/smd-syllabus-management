package vn.edu.smd.core.module.feedbackresponse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.FeedbackQuestion;
import vn.edu.smd.core.entity.FeedbackResponse;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.feedbackresponse.dto.*;
import vn.edu.smd.core.repository.FeedbackQuestionRepository;
import vn.edu.smd.core.repository.FeedbackResponseRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackResponseService {

    private final FeedbackResponseRepository feedbackResponseRepository;
    private final FeedbackQuestionRepository feedbackQuestionRepository;
    private final UserRepository userRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public Page<FeedbackResponseResponse> getAllFeedbackResponses(FeedbackResponseListRequest request) {
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<FeedbackResponse> feedbackResponses = feedbackResponseRepository.findAll(pageable);
        return feedbackResponses.map(this::mapToResponse);
    }

    public FeedbackResponseResponse getFeedbackResponseById(UUID id) {
        FeedbackResponse feedbackResponse = feedbackResponseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackResponse", "id", id));
        return mapToResponse(feedbackResponse);
    }

    @Transactional
    public FeedbackResponseResponse createFeedbackResponse(FeedbackResponseRequest request) {
        FeedbackQuestion question = feedbackQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackQuestion", "id", request.getQuestionId()));

        User respondent = userRepository.findById(request.getRespondentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRespondentId()));

        FeedbackResponse feedbackResponse = FeedbackResponse.builder()
                .question(question)
                .respondent(respondent)
                .responseText(request.getResponseText())
                .rating(request.getRating())
                .selectedOption(request.getSelectedOption())
                .build();

        FeedbackResponse savedFeedbackResponse = feedbackResponseRepository.save(feedbackResponse);
        return mapToResponse(savedFeedbackResponse);
    }

    @Transactional
    public FeedbackResponseResponse updateFeedbackResponse(UUID id, FeedbackResponseRequest request) {
        FeedbackResponse feedbackResponse = feedbackResponseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackResponse", "id", id));

        feedbackResponse.setResponseText(request.getResponseText());
        feedbackResponse.setRating(request.getRating());
        feedbackResponse.setSelectedOption(request.getSelectedOption());

        FeedbackResponse updatedFeedbackResponse = feedbackResponseRepository.save(feedbackResponse);
        return mapToResponse(updatedFeedbackResponse);
    }

    @Transactional
    public void deleteFeedbackResponse(UUID id) {
        if (!feedbackResponseRepository.existsById(id)) {
            throw new ResourceNotFoundException("FeedbackResponse", "id", id);
        }
        feedbackResponseRepository.deleteById(id);
    }

    public List<FeedbackResponseResponse> getFeedbackResponsesByQuestion(UUID questionId) {
        if (!feedbackQuestionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("FeedbackQuestion", "id", questionId);
        }
        return feedbackResponseRepository.findByQuestionId(questionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FeedbackSummaryResponse getFeedbackSummaryBySyllabus(UUID syllabusId) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(syllabusId)
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", syllabusId));

        List<FeedbackResponse> responses = feedbackResponseRepository.findBySyllabusVersionId(syllabusId);
        List<FeedbackQuestion> questions = feedbackQuestionRepository.findBySyllabusVersionId(syllabusId);

        // Calculate overall statistics
        int totalResponses = responses.size();
        int totalQuestions = questions.size();
        
        // Calculate average rating across all responses
        Double averageRating = responses.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(FeedbackResponse::getRating)
                .average()
                .orElse(0.0);

        // Group responses by question
        Map<UUID, List<FeedbackResponse>> responsesByQuestion = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getId()));

        // Build question summaries
        List<FeedbackSummaryResponse.QuestionSummary> questionSummaries = questions.stream()
                .map(question -> {
                    List<FeedbackResponse> questionResponses = responsesByQuestion.getOrDefault(
                            question.getId(), Collections.emptyList());

                    // Calculate average rating for this question
                    Double questionAvgRating = questionResponses.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(FeedbackResponse::getRating)
                            .average()
                            .orElse(0.0);

                    // Build option distribution for multiple choice questions
                    Map<String, Integer> optionDistribution = null;
                    if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
                        optionDistribution = questionResponses.stream()
                                .filter(r -> r.getSelectedOption() != null)
                                .collect(Collectors.groupingBy(
                                        FeedbackResponse::getSelectedOption,
                                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                                ));
                    }

                    // Collect text responses
                    List<String> textResponses = null;
                    if ("TEXT".equals(question.getQuestionType())) {
                        textResponses = questionResponses.stream()
                                .filter(r -> r.getResponseText() != null && !r.getResponseText().isEmpty())
                                .map(FeedbackResponse::getResponseText)
                                .collect(Collectors.toList());
                    }

                    return FeedbackSummaryResponse.QuestionSummary.builder()
                            .questionId(question.getId())
                            .questionText(question.getQuestionText())
                            .questionType(question.getQuestionType())
                            .responseCount(questionResponses.size())
                            .averageRating(questionAvgRating)
                            .optionDistribution(optionDistribution)
                            .textResponses(textResponses)
                            .build();
                })
                .collect(Collectors.toList());

        return FeedbackSummaryResponse.builder()
                .syllabusId(syllabusId)
                .syllabusName(syllabusVersion.getSubject() != null ? syllabusVersion.getSubject().getCurrentNameVi() : "")
                .totalResponses(totalResponses)
                .totalQuestions(totalQuestions)
                .averageRating(averageRating)
                .questionSummaries(questionSummaries)
                .build();
    }

    private FeedbackResponseResponse mapToResponse(FeedbackResponse feedbackResponse) {
        FeedbackResponseResponse response = new FeedbackResponseResponse();
        response.setId(feedbackResponse.getId());
        response.setQuestionId(feedbackResponse.getQuestion().getId());
        response.setQuestionText(feedbackResponse.getQuestion().getQuestionText());
        response.setRespondentId(feedbackResponse.getRespondent().getId());
        response.setRespondentName(feedbackResponse.getRespondent().getFullName());
        response.setResponseText(feedbackResponse.getResponseText());
        response.setRating(feedbackResponse.getRating());
        response.setSelectedOption(feedbackResponse.getSelectedOption());
        response.setCreatedAt(feedbackResponse.getCreatedAt());
        
        return response;
    }
}
