package vn.edu.smd.shared.enums;

/**
 * Decision type for approval history
 * Maps to database enum: decision_type
 */
public enum DecisionType {
    /**
     * Approved
     */
    APPROVED,

    /**
     * Rejected
     */
    REJECTED;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case APPROVED -> "Đã phê duyệt";
            case REJECTED -> "Đã từ chối";
        };
    }
}
