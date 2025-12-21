package vn.edu.smd.shared.constants;

/**
 * Business logic constants
 * Timeouts, limits, defaults, etc.
 */
public final class BusinessConstants {
    
    private BusinessConstants() {
        // Prevent instantiation
    }
    
    // ==================== JWT ====================
    public static final long JWT_ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour in milliseconds
    public static final long JWT_REFRESH_TOKEN_EXPIRATION = 2592000000L; // 30 days in milliseconds
    public static final long JWT_REMEMBER_ME_EXPIRATION = 7776000000L; // 90 days in milliseconds
    
    // ==================== PASSWORD ====================
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
    
    // ==================== SYLLABUS ====================
    public static final int MAX_KEYWORDS = 20;
    public static final int MAX_CLO_COUNT = 20;
    public static final int MAX_PLO_MAPPINGS_PER_CLO = 5;
    public static final int DEFAULT_SYLLABUS_VERSION_INCREMENT = 1;
    
    // ==================== WORKFLOW ====================
    public static final int APPROVAL_REMINDER_DAYS_BEFORE = 3;
    public static final int DEFAULT_REVIEW_DEADLINE_DAYS = 14; // 2 weeks
    public static final int MAX_REVISION_CYCLES = 3;
    
    // ==================== NOTIFICATION ====================
    public static final int NOTIFICATION_RETENTION_DAYS = 90;
    public static final int MAX_NOTIFICATIONS_PER_PAGE = 50;
    
    // ==================== AUDIT LOG ====================
    public static final int AUDIT_LOG_RETENTION_DAYS = 365; // 1 year
    
    // ==================== RATE LIMITING ====================
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOGIN_LOCKOUT_DURATION_MINUTES = 15;
    public static final int MAX_API_CALLS_PER_MINUTE = 60;
    
    // ==================== CACHE ====================
    public static final int CACHE_TTL_SECONDS_SHORT = 300; // 5 minutes
    public static final int CACHE_TTL_SECONDS_MEDIUM = 1800; // 30 minutes
    public static final int CACHE_TTL_SECONDS_LONG = 3600; // 1 hour
    
    // ==================== FILE ====================
    public static final long MAX_UPLOAD_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"pdf", "doc", "docx"};
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
}
