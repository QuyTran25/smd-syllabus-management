package vn.edu.smd.shared.enums;

/**
 * Teaching assignment status
 * Maps to database enum: assignment_status ('pending', 'in-progress', 'submitted', 'completed')
 */
public enum AssignmentStatus {
    /**
     * Assignment created, not started yet
     */
    PENDING,

    /**
     * Lecturer is working on the syllabus
     */
    IN_PROGRESS,

    /**
     * Syllabus submitted for approval
     */
    SUBMITTED,

    /**
     * Syllabus approved and published
     */
    COMPLETED;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Chưa bắt đầu";
            case IN_PROGRESS -> "Đang thực hiện";
            case SUBMITTED -> "Đã nộp";
            case COMPLETED -> "Hoàn thành";
        };
    }

    /**
     * Check if assignment is active (not completed)
     */
    public boolean isActive() {
        return this != COMPLETED;
    }

    /**
     * Check if assignment needs attention
     */
    public boolean needsAttention() {
        return this == PENDING;
    }
}
