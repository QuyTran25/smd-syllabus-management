package vn.edu.smd.shared.enums;

/**
 * Feedback status
 * Maps to database enum: feedback_status
 */
public enum FeedbackStatus {
    /**
     * Feedback received, waiting for review
     */
    PENDING,

    /**
     * Feedback is being reviewed
     */
    IN_REVIEW,

    /**
     * Feedback has been resolved
     */
    RESOLVED,

    /**
     * Feedback rejected/not applicable
     */
    REJECTED,

    /**
     * Feedback closed without action
     */
    CLOSED,

    /**
     * Feedback approved, waiting for lecturer to start revision
     */
    AWAITING_REVISION,

    /**
     * Feedback is being fixed in current revision session
     */
    IN_REVISION;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Chờ xử lý";
            case IN_REVIEW -> "Đang xem xét";
            case RESOLVED -> "Đã giải quyết";
            case REJECTED -> "Từ chối";
            case CLOSED -> "Đã đóng";
            case AWAITING_REVISION -> "Chờ chỉnh sửa";
            case IN_REVISION -> "Đang chỉnh sửa";
        };
    }

    /**
     * Check if feedback is still open
     */
    public boolean isOpen() {
        return this == PENDING || this == IN_REVIEW || this == AWAITING_REVISION || this == IN_REVISION;
    }

    /**
     * Check if feedback is closed
     */
    public boolean isClosed() {
        return this == RESOLVED || this == REJECTED || this == CLOSED;
    }
}
