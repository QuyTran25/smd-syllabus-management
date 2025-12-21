package vn.edu.smd.shared.enums;

/**
 * Notification type
 */
public enum NotificationType {
    /**
     * System notification
     */
    SYSTEM,

    /**
     * Approval workflow notification
     */
    APPROVAL,

    /**
     * Deadline reminder
     */
    DEADLINE,

    /**
     * Syllabus published
     */
    PUBLICATION,

    /**
     * Comment/feedback notification
     */
    COMMENT,

    /**
     * Assignment notification
     */
    ASSIGNMENT,

    /**
     * Collaboration notification
     */
    COLLABORATION,

    /**
     * Error report notification
     */
    ERROR_REPORT;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case SYSTEM -> "Hệ thống";
            case APPROVAL -> "Phê duyệt";
            case DEADLINE -> "Nhắc hạn";
            case PUBLICATION -> "Xuất bản";
            case COMMENT -> "Bình luận";
            case ASSIGNMENT -> "Phân công";
            case COLLABORATION -> "Cộng tác";
            case ERROR_REPORT -> "Báo lỗi";
        };
    }

    /**
     * Get icon class for UI
     */
    public String getIconClass() {
        return switch (this) {
            case SYSTEM -> "icon-system";
            case APPROVAL -> "icon-check-circle";
            case DEADLINE -> "icon-clock";
            case PUBLICATION -> "icon-publish";
            case COMMENT -> "icon-message";
            case ASSIGNMENT -> "icon-clipboard";
            case COLLABORATION -> "icon-users";
            case ERROR_REPORT -> "icon-alert";
        };
    }
}
