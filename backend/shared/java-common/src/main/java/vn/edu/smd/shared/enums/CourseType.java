package vn.edu.smd.shared.enums;

/**
 * Course type classification
 * Maps to database enum: course_type
 */
public enum CourseType {
    /**
     * Required/mandatory course
     */
    REQUIRED,

    /**
     * Elective course
     */
    ELECTIVE,

    /**
     * Free elective course
     */
    FREE;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case REQUIRED -> "Bắt buộc";
            case ELECTIVE -> "Tự chọn";
            case FREE -> "Tự chọn tự do";
        };
    }
}
