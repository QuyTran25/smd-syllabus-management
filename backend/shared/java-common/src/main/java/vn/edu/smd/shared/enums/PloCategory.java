package vn.edu.smd.shared.enums;

/**
 * PLO (Program Learning Outcome) category
 * Maps to database enum: plo_category
 */
public enum PloCategory {
    /**
     * Knowledge outcomes
     */
    KNOWLEDGE,

    /**
     * Skill outcomes
     */
    SKILLS,

    /**
     * Competence outcomes
     */
    COMPETENCE,

    /**
     * Attitude outcomes
     */
    ATTITUDE;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case KNOWLEDGE -> "Kiến thức";
            case SKILLS -> "Kỹ năng";
            case COMPETENCE -> "Năng lực";
            case ATTITUDE -> "Thái độ";
        };
    }
}
