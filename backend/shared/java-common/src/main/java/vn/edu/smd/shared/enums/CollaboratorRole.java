package vn.edu.smd.shared.enums;

/**
 * Collaborator role in syllabus editing
 * Maps to database enum: collaborator_role
 */
public enum CollaboratorRole {
    /**
     * Owner - full control
     */
    OWNER,

    /**
     * Editor - can edit content
     */
    EDITOR,

    /**
     * Viewer - read-only access
     */
    VIEWER;

    /**
     * Get display name in Vietnamese
     */
    public String getDisplayName() {
        return switch (this) {
            case OWNER -> "Chủ sở hữu";
            case EDITOR -> "Người chỉnh sửa";
            case VIEWER -> "Người xem";
        };
    }

    /**
     * Check if this role can edit
     */
    public boolean canEdit() {
        return this == OWNER || this == EDITOR;
    }

    /**
     * Check if this role has full control
     */
    public boolean isOwner() {
        return this == OWNER;
    }
}
