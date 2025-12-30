package vn.edu.smd.shared.enums;

/**
 * Syllabus status throughout the approval workflow
 * Maps to database enum: syllabus_status
 */
public enum SyllabusStatus {
    /**
     * Initial state - being edited by lecturer
     */
    DRAFT,

    /**
     * Submitted to Head of Department for review
     */
    PENDING_HOD,

    /**
     * Submitted to Academic Affairs for review
     */
    PENDING_AA,

    /**
     * Submitted to Principal for final approval
     */
    PENDING_PRINCIPAL,

    /**
     * Approved by Principal, ready for publication
     */
    APPROVED,

    /**
     * Published and accessible to students
     */
    PUBLISHED,

    /**
     * Rejected at any stage - needs revision
     */
    REJECTED,

    /**
     * Currently being revised after rejection
     */
    REVISION_IN_PROGRESS,

    /**
     * Revision submitted to HOD for re-review
     */
    PENDING_HOD_REVISION,

    /**
     * Approved revision, pending admin to republish
     */
    PENDING_ADMIN_REPUBLISH,

    /**
     * No longer in use
     */
    INACTIVE,

    /**
     * Old version archived after new version published
     */
    ARCHIVED;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case DRAFT -> "Bản nháp";
            case PENDING_HOD -> "Chờ Trưởng BM";
            case PENDING_AA -> "Chờ Phòng ĐT";
            case PENDING_PRINCIPAL -> "Chờ Hiệu trưởng duyệt";
            case APPROVED -> "Đã phê duyệt";
            case PUBLISHED -> "Đã xuất bản";
            case REJECTED -> "Bị từ chối";
            case REVISION_IN_PROGRESS -> "Đang chỉnh sửa";
            case PENDING_HOD_REVISION -> "Chờ TBM duyệt lại";
            case PENDING_ADMIN_REPUBLISH -> "Chờ xuất bản lại";
            case INACTIVE -> "Không hoạt động";
            case ARCHIVED -> "Đã lưu trữ";
        };
    }

    /**
     * Check if status indicates pending approval
     */
    public boolean isPending() {
        return this == PENDING_HOD || 
               this == PENDING_AA || 
               this == PENDING_PRINCIPAL ||
               this == PENDING_HOD_REVISION ||
               this == PENDING_ADMIN_REPUBLISH;
    }

    /**
     * Check if status allows editing
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED || this == REVISION_IN_PROGRESS;
    }

    /**
     * Check if status is final (published or archived)
     */
    public boolean isFinal() {
        return this == PUBLISHED || this == ARCHIVED;
    }
}
