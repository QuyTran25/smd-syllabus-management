package vn.edu.smd.shared.enums;

/**
 * Subject component type
 * Maps to database enum: subject_component
 */
public enum SubjectComponent {
    /**
     * Theory only
     */
    THEORY,

    /**
     * Practice/Lab only
     */
    PRACTICE,

    /**
     * Both theory and practice
     */
    BOTH;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case THEORY -> "Lý thuyết";
            case PRACTICE -> "Thực hành";
            case BOTH -> "Lý thuyết + Thực hành";
        };
    }
}
