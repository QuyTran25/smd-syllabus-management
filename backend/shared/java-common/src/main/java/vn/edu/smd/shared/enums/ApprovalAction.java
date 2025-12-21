package vn.edu.smd.shared.enums;

/**
 * Actions that can be taken during syllabus approval workflow
 */
public enum ApprovalAction {
    /**
     * Approve the syllabus and move to next stage
     */
    APPROVE,

    /**
     * Reject the syllabus and send back for revision
     */
    REJECT,

    /**
     * Request changes before approval
     */
    REQUEST_CHANGES,

    /**
     * Submit syllabus for approval
     */
    SUBMIT,

    /**
     * Withdraw submission
     */
    WITHDRAW;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case APPROVE -> "Phê duyệt";
            case REJECT -> "Từ chối";
            case REQUEST_CHANGES -> "Yêu cầu chỉnh sửa";
            case SUBMIT -> "Gửi duyệt";
            case WITHDRAW -> "Rút lại";
        };
    }
}
