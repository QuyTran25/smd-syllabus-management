package vn.edu.smd.shared.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.smd.shared.enums.Gender;
import vn.edu.smd.shared.enums.UserRole;

/**
 * User registration request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    /**
     * Email address (will be used as username)
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;
    
    /**
     * Password with strong validation
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu phải từ 8-100 ký tự")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
    )
    private String password;
    
    /**
     * Password confirmation (must match password)
     */
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
    
    /**
     * Full name
     */
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 255, message = "Họ tên phải từ 2-255 ký tự")
    private String fullName;
    
    /**
     * Phone number (optional)
     */
    @Pattern(
        regexp = "^(\\+84|0)[0-9]{9,10}$",
        message = "Số điện thoại không hợp lệ"
    )
    private String phone;
    
    /**
     * Gender
     */
    private Gender gender;
    
    /**
     * User role (for registration by admin)
     * Default is STUDENT for self-registration
     */
    private UserRole role;
    
    /**
     * Faculty ID (if applicable)
     */
    private String facultyId;
    
    /**
     * Department ID (if applicable)
     */
    private String departmentId;
}
