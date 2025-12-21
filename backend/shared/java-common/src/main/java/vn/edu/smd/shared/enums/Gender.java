package vn.edu.smd.shared.enums;

/**
 * Gender types
 * Maps to database enum: gender_type
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    public String getDisplayName() {
        return switch (this) {
            case MALE -> "Nam";
            case FEMALE -> "Nữ";
            case OTHER -> "Khác";
        };
    }
}
