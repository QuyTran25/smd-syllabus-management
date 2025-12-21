package vn.edu.smd.shared.enums;

/**
 * Actor role type for approval workflow
 * Maps to database enum: actor_role_type
 */
public enum ActorRoleType {
    HOD,
    AA,
    PRINCIPAL,
    ADMIN,
    LECTURER;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case HOD -> "Trưởng Bộ môn";
            case AA -> "Phòng Đào tạo";
            case PRINCIPAL -> "Hiệu trưởng";
            case ADMIN -> "Quản trị viên";
            case LECTURER -> "Giảng viên";
        };
    }
}
