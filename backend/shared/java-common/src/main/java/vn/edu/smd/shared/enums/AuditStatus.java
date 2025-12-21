package vn.edu.smd.shared.enums;

/**
 * Audit operation status
 * Maps to database enum: audit_status
 */
public enum AuditStatus {
    /**
     * Operation succeeded
     */
    SUCCESS,

    /**
     * Operation failed
     */
    FAILED;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case SUCCESS -> "Thành công";
            case FAILED -> "Thất bại";
        };
    }

    /**
     * Check if operation was successful
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
