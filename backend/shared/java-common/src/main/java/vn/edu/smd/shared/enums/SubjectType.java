package vn.edu.smd.shared.enums;

/**
 * Subject type classification
 * Maps to database enum: subject_type
 */
public enum SubjectType {
    /**
     * Required/Mandatory course
     */
    REQUIRED,

    /**
     * Elective course
     */
    ELECTIVE;

    public String getDisplayName() {
        return switch (this) {
            case REQUIRED -> "Bắt buộc";
            case ELECTIVE -> "Tự chọn";
        };
    }
}
