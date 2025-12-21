package vn.edu.smd.shared.enums;

/**
 * User account status
 * Maps to database enum: user_status
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BANNED;

    public String getDisplayName() {
        return switch (this) {
            case ACTIVE -> "Hoạt động";
            case INACTIVE -> "Không hoạt động";
            case BANNED -> "Bị cấm";
        };
    }
}
