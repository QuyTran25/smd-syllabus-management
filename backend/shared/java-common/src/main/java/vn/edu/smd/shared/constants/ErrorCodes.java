package vn.edu.smd.shared.constants;

/**
 * Error codes for consistent error handling
 * Format: <DOMAIN>_<ERROR_TYPE>_<SPECIFIC_ERROR>
 */
public final class ErrorCodes {
    
    private ErrorCodes() {
        // Prevent instantiation
    }
    
    // ==================== GENERAL ERRORS ====================
    public static final String INTERNAL_SERVER_ERROR = "GENERAL_INTERNAL_ERROR";
    public static final String BAD_REQUEST = "GENERAL_BAD_REQUEST";
    public static final String NOT_FOUND = "GENERAL_NOT_FOUND";
    public static final String VALIDATION_ERROR = "GENERAL_VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "GENERAL_UNAUTHORIZED";
    public static final String FORBIDDEN = "GENERAL_FORBIDDEN";
    
    // ==================== AUTHENTICATION ERRORS ====================
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
    public static final String AUTH_TOKEN_MISSING = "AUTH_TOKEN_MISSING";
    public static final String AUTH_REFRESH_TOKEN_INVALID = "AUTH_REFRESH_TOKEN_INVALID";
    public static final String AUTH_USER_DISABLED = "AUTH_USER_DISABLED";
    public static final String AUTH_USER_NOT_FOUND = "AUTH_USER_NOT_FOUND";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "AUTH_EMAIL_ALREADY_EXISTS";
    public static final String AUTH_PASSWORD_MISMATCH = "AUTH_PASSWORD_MISMATCH";
    public static final String AUTH_WEAK_PASSWORD = "AUTH_WEAK_PASSWORD";
    
    // ==================== USER ERRORS ====================
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_INACTIVE = "USER_INACTIVE";
    public static final String USER_PERMISSION_DENIED = "USER_PERMISSION_DENIED";
    
    // ==================== SYLLABUS ERRORS ====================
    public static final String SYLLABUS_NOT_FOUND = "SYLLABUS_NOT_FOUND";
    public static final String SYLLABUS_ALREADY_EXISTS = "SYLLABUS_ALREADY_EXISTS";
    public static final String SYLLABUS_NOT_EDITABLE = "SYLLABUS_NOT_EDITABLE";
    public static final String SYLLABUS_INVALID_STATUS = "SYLLABUS_INVALID_STATUS";
    public static final String SYLLABUS_PERMISSION_DENIED = "SYLLABUS_PERMISSION_DENIED";
    public static final String SYLLABUS_VERSION_CONFLICT = "SYLLABUS_VERSION_CONFLICT";
    public static final String SYLLABUS_MISSING_CLO = "SYLLABUS_MISSING_CLO";
    public static final String SYLLABUS_MISSING_PLO_MAPPING = "SYLLABUS_MISSING_PLO_MAPPING";
    public static final String SYLLABUS_INVALID_ASSESSMENT = "SYLLABUS_INVALID_ASSESSMENT";
    
    // ==================== SUBJECT ERRORS ====================
    public static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";
    public static final String SUBJECT_CODE_DUPLICATE = "SUBJECT_CODE_DUPLICATE";
    public static final String SUBJECT_HAS_PUBLISHED_SYLLABUS = "SUBJECT_HAS_PUBLISHED_SYLLABUS";
    
    // ==================== WORKFLOW ERRORS ====================
    public static final String WORKFLOW_INVALID_TRANSITION = "WORKFLOW_INVALID_TRANSITION";
    public static final String WORKFLOW_NOT_AUTHORIZED = "WORKFLOW_NOT_AUTHORIZED";
    public static final String WORKFLOW_DEADLINE_PASSED = "WORKFLOW_DEADLINE_PASSED";
    public static final String WORKFLOW_ALREADY_APPROVED = "WORKFLOW_ALREADY_APPROVED";
    public static final String WORKFLOW_COMMENT_REQUIRED = "WORKFLOW_COMMENT_REQUIRED";
    
    // ==================== FILE ERRORS ====================
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String FILE_INVALID_TYPE = "FILE_INVALID_TYPE";
    public static final String FILE_UPLOAD_FAILED = "FILE_UPLOAD_FAILED";
    public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
    
    // ==================== VALIDATION ERRORS ====================
    public static final String VALIDATION_FIELD_REQUIRED = "VALIDATION_FIELD_REQUIRED";
    public static final String VALIDATION_FIELD_INVALID = "VALIDATION_FIELD_INVALID";
    public static final String VALIDATION_EMAIL_INVALID = "VALIDATION_EMAIL_INVALID";
    public static final String VALIDATION_PASSWORD_WEAK = "VALIDATION_PASSWORD_WEAK";
    public static final String VALIDATION_DATE_INVALID = "VALIDATION_DATE_INVALID";
    public static final String VALIDATION_RANGE_INVALID = "VALIDATION_RANGE_INVALID";
}
