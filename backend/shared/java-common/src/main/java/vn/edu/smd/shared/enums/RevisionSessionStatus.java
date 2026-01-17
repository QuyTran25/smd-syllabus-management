package vn.edu.smd.shared.enums;

/**
 * Revision Session Status
 * Maps to database enum: revision_session_status
 */
public enum RevisionSessionStatus {
    /**
     * Session opened, collecting feedback
     */
    OPEN,

    /**
     * Lecturer is working on fixes
     */
    IN_PROGRESS,

    /**
     * Submitted to HOD for review
     */
    PENDING_HOD,

    /**
     * Session completed and republished
     */
    COMPLETED,

    /**
     * Session cancelled
     */
    CANCELLED;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case OPEN -> "Đang mở";
            case IN_PROGRESS -> "Đang xử lý";
            case PENDING_HOD -> "Chờ TBM duyệt";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    /**
     * Check if session is active
     */
    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS || this == PENDING_HOD;
    }

    /**
     * Check if session is closed
     */
    public boolean isClosed() {
        return this == COMPLETED || this == CANCELLED;
    }
}
