package vn.edu.smd.shared.constants;

/**
 * API-related constants
 * URL paths, header names, parameter names
 */
public final class ApiConstants {
    
    private ApiConstants() {
        // Prevent instantiation
    }
    
    // ==================== API VERSION ====================
    public static final String API_VERSION = "v1";
    public static final String API_PREFIX = "/api/" + API_VERSION;
    
    // ==================== BASE PATHS ====================
    public static final String AUTH_BASE = API_PREFIX + "/auth";
    public static final String USERS_BASE = API_PREFIX + "/users";
    public static final String SYLLABUS_BASE = API_PREFIX + "/syllabi";
    public static final String SUBJECTS_BASE = API_PREFIX + "/subjects";
    public static final String FACULTIES_BASE = API_PREFIX + "/faculties";
    public static final String DEPARTMENTS_BASE = API_PREFIX + "/departments";
    public static final String ACADEMIC_TERMS_BASE = API_PREFIX + "/academic-terms";
    public static final String PLO_BASE = API_PREFIX + "/plos";
    public static final String NOTIFICATIONS_BASE = API_PREFIX + "/notifications";
    public static final String FEEDBACK_BASE = API_PREFIX + "/feedback";
    public static final String AUDIT_LOGS_BASE = API_PREFIX + "/audit-logs";
    public static final String AI_BASE = API_PREFIX + "/ai";
    
    // ==================== HEADER NAMES ====================
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_X_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    
    // ==================== TOKEN ====================
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";
    
    // ==================== PAGINATION ====================
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_SORT_ORDER = "sortOrder";
    
    // ==================== SEARCH & FILTER ====================
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_FACULTY = "facultyId";
    public static final String PARAM_DEPARTMENT = "departmentId";
    public static final String PARAM_ACADEMIC_TERM = "academicTermId";
    public static final String PARAM_OWNER = "ownerId";
    
    // ==================== DATE FORMATS ====================
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATETIME_FORMAT_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    
    // ==================== FILE UPLOAD ====================
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_FILE_EXTENSIONS = {".pdf", ".doc", ".docx"};
    
    // ==================== CORS ====================
    public static final String[] ALLOWED_ORIGINS = {
        "http://localhost:3000", // Frontend dev
        "http://localhost:3001", // Student portal
        "https://smd.edu.vn"     // Production
    };
    
    public static final String[] ALLOWED_METHODS = {
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    };
    
    public static final String[] ALLOWED_HEADERS = {
        "Authorization", "Content-Type", "Accept", "X-Request-ID"
    };
}
