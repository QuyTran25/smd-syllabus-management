package vn.edu.smd.shared.enums;

/**
 * Error report section classification
 * Maps to database enum: error_report_section
 */
public enum ErrorReportSection {
    /**
     * Subject information section
     */
    SUBJECT_INFO,

    /**
     * Learning objectives section
     */
    OBJECTIVES,

    /**
     * Assessment matrix section
     */
    ASSESSMENT_MATRIX,

    /**
     * CLO (Course Learning Outcomes) section
     */
    CLO,

    /**
     * CLO-PLO mapping matrix section
     */
    CLO_PLO_MATRIX,

    /**
     * Textbook section
     */
    TEXTBOOK,

    /**
     * Reference materials section
     */
    REFERENCE,

    /**
     * Other section
     */
    OTHER;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case SUBJECT_INFO -> "Thông tin môn học";
            case OBJECTIVES -> "Mục tiêu học phần";
            case ASSESSMENT_MATRIX -> "Ma trận đánh giá";
            case CLO -> "Chuẩn đầu ra học phần";
            case CLO_PLO_MATRIX -> "Ma trận CLO-PLO";
            case TEXTBOOK -> "Giáo trình";
            case REFERENCE -> "Tài liệu tham khảo";
            case OTHER -> "Khác";
        };
    }

    /**
     * Get database value (snake_case)
     */
    public String getDatabaseValue() {
        return this.name().toLowerCase();
    }
}
