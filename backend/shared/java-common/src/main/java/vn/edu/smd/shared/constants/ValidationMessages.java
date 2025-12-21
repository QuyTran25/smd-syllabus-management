package vn.edu.smd.shared.constants;

/**
 * Validation message constants
 * Centralized validation messages for consistency
 */
public final class ValidationMessages {
    
    private ValidationMessages() {
        // Prevent instantiation
    }
    
    // ==================== COMMON ====================
    public static final String REQUIRED = "{field} không được để trống";
    public static final String INVALID = "{field} không hợp lệ";
    public static final String TOO_LONG = "{field} không được vượt quá {max} ký tự";
    public static final String TOO_SHORT = "{field} phải có ít nhất {min} ký tự";
    public static final String OUT_OF_RANGE = "{field} phải trong khoảng {min} đến {max}";
    
    // ==================== EMAIL ====================
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Email không đúng định dạng";
    public static final String EMAIL_TOO_LONG = "Email không được vượt quá 255 ký tự";
    public static final String EMAIL_ALREADY_EXISTS = "Email này đã được sử dụng";
    
    // ==================== PASSWORD ====================
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String PASSWORD_TOO_SHORT = "Mật khẩu phải có ít nhất 8 ký tự";
    public static final String PASSWORD_TOO_LONG = "Mật khẩu không được vượt quá 100 ký tự";
    public static final String PASSWORD_WEAK = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số";
    public static final String PASSWORD_MISMATCH = "Mật khẩu xác nhận không khớp";
    public static final String PASSWORD_INVALID_PATTERN = "Mật khẩu không đáp ứng yêu cầu bảo mật";
    
    // ==================== NAME ====================
    public static final String FULL_NAME_REQUIRED = "Họ tên không được để trống";
    public static final String FULL_NAME_TOO_SHORT = "Họ tên phải có ít nhất 2 ký tự";
    public static final String FULL_NAME_TOO_LONG = "Họ tên không được vượt quá 255 ký tự";
    
    // ==================== PHONE ====================
    public static final String PHONE_INVALID = "Số điện thoại không hợp lệ";
    public static final String PHONE_PATTERN = "Số điện thoại phải có định dạng +84xxxxxxxxx hoặc 0xxxxxxxxx";
    
    // ==================== SYLLABUS ====================
    public static final String SUBJECT_ID_REQUIRED = "Mã môn học không được để trống";
    public static final String ACADEMIC_TERM_REQUIRED = "Học kỳ không được để trống";
    public static final String DESCRIPTION_REQUIRED = "Mô tả môn học không được để trống";
    public static final String DESCRIPTION_TOO_LONG = "Mô tả không được vượt quá 5000 ký tự";
    public static final String OBJECTIVES_TOO_LONG = "Mục tiêu không được vượt quá 3000 ký tự";
    public static final String THEORY_HOURS_REQUIRED = "Số giờ lý thuyết không được để trống";
    public static final String PRACTICE_HOURS_REQUIRED = "Số giờ thực hành không được để trống";
    public static final String SELF_STUDY_HOURS_REQUIRED = "Số giờ tự học không được để trống";
    public static final String KEYWORDS_MAX = "Không được vượt quá 20 từ khóa";
    
    // ==================== APPROVAL ====================
    public static final String ACTION_REQUIRED = "Hành động không được để trống";
    public static final String COMMENT_TOO_LONG = "Nhận xét không được vượt quá 2000 ký tự";
    public static final String COMMENT_REQUIRED_FOR_REJECTION = "Nhận xét bắt buộc khi từ chối";
    
    // ==================== FILE ====================
    public static final String FILE_TOO_LARGE = "File không được vượt quá {maxSize}MB";
    public static final String FILE_INVALID_TYPE = "File phải có định dạng: {allowedTypes}";
    
    // ==================== DATE ====================
    public static final String DATE_INVALID = "Ngày không hợp lệ";
    public static final String DATE_FUTURE_REQUIRED = "Ngày phải trong tương lai";
    public static final String DATE_PAST_REQUIRED = "Ngày phải trong quá khứ";
    public static final String START_DATE_BEFORE_END = "Ngày bắt đầu phải trước ngày kết thúc";
}
