package vn.edu.smd.core.module.feedbackresponse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSummaryResponse {
    private UUID syllabusId;
    private String syllabusName;
    private Integer totalResponses;
    private Integer totalQuestions;
    private Double averageRating;
    private List<QuestionSummary> questionSummaries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionSummary {
        private UUID questionId;
        private String questionText;
        private String questionType;
        private Integer responseCount;
        private Double averageRating;
        private Map<String, Integer> optionDistribution;
        private List<String> textResponses;
    }
}
