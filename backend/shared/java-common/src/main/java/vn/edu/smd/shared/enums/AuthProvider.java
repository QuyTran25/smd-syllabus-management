package vn.edu.smd.shared.enums;

/**
 * Authentication provider types
 * Maps to database enum: auth_provider
 */
public enum AuthProvider {
    /**
     * Local authentication with email/password
     */
    LOCAL,

    /**
     * Google OAuth2
     */
    GOOGLE,

    /**
     * Microsoft OAuth2
     */
    MICROSOFT;

    public String getDisplayName() {
        return switch (this) {
            case LOCAL -> "Email/Password";
            case GOOGLE -> "Google";
            case MICROSOFT -> "Microsoft";
        };
    }
}
