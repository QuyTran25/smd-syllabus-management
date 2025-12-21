package vn.edu.smd.shared.enums;

/**
 * User roles in the system
 * Maps to database enum: user_role
 */
public enum UserRole {
    /**
     * System Administrator - Full system access
     */
    ADMIN,

    /**
     * Principal - Final approval authority
     */
    PRINCIPAL,

    /**
     * Academic Affairs - Manages curriculum and courses
     */
    AA,

    /**
     * Head of Department - Manages department syllabi
     */
    HOD,

    /**
     * Lecturer - Creates and manages syllabi
     */
    LECTURER,

    /**
     * Student - Views syllabi and provides feedback
     */
    STUDENT;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "Quản trị viên";
            case PRINCIPAL -> "Hiệu trưởng";
            case AA -> "Phòng Đào tạo";
            case HOD -> "Trưởng Bộ môn";
            case LECTURER -> "Giảng viên";
            case STUDENT -> "Sinh viên";
        };
    }

    /**
     * Check if role has administrative privileges
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == PRINCIPAL || this == AA || this == HOD;
    }

    /**
     * Check if role can create syllabi
     */
    public boolean canCreateSyllabus() {
        return this == LECTURER || this == HOD || this == AA;
    }

    /**
     * Check if role can approve syllabi
     */
    public boolean canApproveSyllabus() {
        return this == HOD || this == AA || this == PRINCIPAL;
    }
}
