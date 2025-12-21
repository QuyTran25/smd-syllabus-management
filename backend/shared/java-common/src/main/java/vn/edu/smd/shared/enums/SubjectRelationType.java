package vn.edu.smd.shared.enums;

/**
 * Subject relationship types
 * Maps to database enum: subject_relation_type
 */
public enum SubjectRelationType {
    /**
     * Must complete before taking this subject
     */
    PREREQUISITE,

    /**
     * Must take simultaneously with this subject
     */
    CO_REQUISITE,

    /**
     * Can replace this subject (equivalent course)
     */
    REPLACEMENT;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case PREREQUISITE -> "Học trước";
            case CO_REQUISITE -> "Học song hành";
            case REPLACEMENT -> "Thay thế";
        };
    }
}
