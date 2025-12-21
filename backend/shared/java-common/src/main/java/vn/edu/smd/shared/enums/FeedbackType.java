package vn.edu.smd.shared.enums;

/**
 * Feedback/Error report type
 * Maps to database enum: feedback_type
 */
public enum FeedbackType {
    /**
     * Report an error in syllabus
     */
    ERROR,

    /**
     * Suggest an improvement
     */
    SUGGESTION,

    /**
     * Ask a question
     */
    QUESTION,

    /**
     * Other feedback
     */
    OTHER;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case ERROR -> "Báo lỗi";
            case SUGGESTION -> "Đề xuất";
            case QUESTION -> "Câu hỏi";
            case OTHER -> "Khác";
        };
    }
}
