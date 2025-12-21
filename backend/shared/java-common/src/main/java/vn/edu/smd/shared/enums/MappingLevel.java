package vn.edu.smd.shared.enums;

/**
 * CLO-PLO mapping level
 * Maps to database enum: mapping_level
 */
public enum MappingLevel {
    /**
     * High level of contribution/mapping
     */
    H,

    /**
     * Medium level of contribution/mapping
     */
    M,

    /**
     * Low level of contribution/mapping
     */
    L;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case H -> "Cao";
            case M -> "Trung bình";
            case L -> "Thấp";
        };
    }

    /**
     * Get full description
     */
    public String getDescription() {
        return switch (this) {
            case H -> "High - Mức độ đóng góp cao";
            case M -> "Medium - Mức độ đóng góp trung bình";
            case L -> "Low - Mức độ đóng góp thấp";
        };
    }
}
