package vn.edu.smd.core.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.common.util.AuditLogHelper;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.auth.dto.*;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.core.security.JwtTokenProvider;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.AuthProvider;
import vn.edu.smd.shared.enums.UserStatus;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogHelper auditLogHelper;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        String rawPassword = request.getPassword().trim();

        // 1. Kiểm tra User tồn tại để log login thất bại (nếu cần)
        User dbUser = userRepository.findByEmail(email).orElse(null);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            
            // 🔥 LOG LOGIN THÀNH CÔNG
            if (dbUser != null) {
                auditLogHelper.logLogin(dbUser.getId(), dbUser.getEmail(), true);
            }
            
            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            // 🔥 LOG LOGIN THẤT BẠI
            if (dbUser != null) {
                auditLogHelper.logLogin(dbUser.getId(), dbUser.getEmail(), false);
            }
            throw e;
        }
    }


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * 🔥 FIX: Cập nhật logic Logout để ghi log trước khi xóa context
     */
    public void logout() {
        try {
            // 1. Lấy thông tin người dùng hiện tại
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. Kiểm tra xem có phải người dùng hợp lệ không (để tránh lỗi null)
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                
                // 3. Ghi log Logout
                // Lưu ý: ID và Email lấy từ Principal (thông tin trong token)
                auditLogHelper.logLogout(userPrincipal.getId(), userPrincipal.getEmail());
                
                System.out.println("✅ Đã ghi log logout cho user: " + userPrincipal.getEmail());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi ghi log logout: " + e.getMessage());
            // Không throw exception để quá trình logout vẫn diễn ra bình thường ở client
        } finally {
            // 4. Luôn luôn xóa context dù có lỗi hay không
            SecurityContextHolder.clearContext();
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("Password reset requested for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        throw new BadRequestException("Reset password feature not implemented yet");
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByIdWithRoles(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
                user.getStatus().name()
        );
    }
}