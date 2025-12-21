package vn.edu.smd.shared.enums;

/**
 * Component type in curriculum structure
 * Maps to database enum: component_type
 */
public enum ComponentType {
    /**
     * Major/specialized courses
     */
    MAJOR,

    /**
     * Foundation/basic courses
     */
    FOUNDATION,

    /**
     * General education courses
     */
    GENERAL,

    /**
     * Thesis/capstone project
     */
    THESIS;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case MAJOR -> "Chuyên ngành";
            case FOUNDATION -> "Cơ sở ngành";
            case GENERAL -> "Đại cương";
            case THESIS -> "Luận văn/Khóa luận";
        };
    }
}
