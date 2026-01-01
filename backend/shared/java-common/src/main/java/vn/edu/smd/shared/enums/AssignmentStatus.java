package vn.edu.smd.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Teaching assignment status
 * Maps to database enum: assignment_status ('pending', 'in-progress', 'submitted', 'completed')
 */
public enum AssignmentStatus {
    /**
     * Assignment created, not started yet
     */
    PENDING("PENDING"),

    /**
     * Lecturer is working on the syllabus
     */
    IN_PROGRESS("IN_PROGRESS"),

    /**
     * Syllabus submitted for approval
     */
    SUBMITTED("SUBMITTED"),

    /**
     * Syllabus approved and published
     */
    COMPLETED("COMPLETED");

    private final String dbValue;

    AssignmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static AssignmentStatus fromString(String value) {
        if (value == null) return null;
        for (AssignmentStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AssignmentStatus: " + value);
    }

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
