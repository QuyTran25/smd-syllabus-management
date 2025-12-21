package vn.edu.smd.shared.enums;

/**
 * Role scope type in RBAC system
 * Defines the level at which a role is applied
 * Maps to database: user_roles.scope_type
 */
public enum RoleScope {
    /**
     * Global/System level - applies to entire system
     */
    GLOBAL,

    /**
     * Faculty level - applies to a specific faculty
     */
    FACULTY,

    /**
     * Department level - applies to a specific department
     */
    DEPARTMENT;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case GLOBAL -> "Toàn hệ thống";
            case FACULTY -> "Khoa";
            case DEPARTMENT -> "Bộ môn";
        };
    }
}
