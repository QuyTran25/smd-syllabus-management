package vn.edu.smd.core.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.auth.dto.*;
import vn.edu.smd.core.repository.RoleRepository;
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
    
    /**
     * FIX: Đảm bảo tên biến là 'roleRepository' (chữ r viết thường) 
     * và kiểu dữ liệu là 'RoleRepository' (chữ R viết hoa).
     */
    private final RoleRepository roleRepository; 
    
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        String rawPassword = request.getPassword().trim();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

            log.info("User {} logged in successfully", user.getEmail());
            
            return new AuthResponse(accessToken, refreshToken, mapToUserInfo(user));

        } catch (AuthenticationException e) {
            log.error("Login failed for user {}: {}", email, e.getMessage());
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
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setStatus(UserStatus.ACTIVE);

        // FIX: Sử dụng đúng biến 'roleRepository' đã khai báo ở trên
        try {
            roleRepository.findByCode("LECTURER")
                .ifPresent(role -> user.setRoles(java.util.Set.of(role)));
        } catch (Exception e) {
            log.warn("Could not assign default role: {}", e.getMessage());
        }

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(accessToken, refreshToken, mapToUserInfo(user));
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

        return new AuthResponse(newAccessToken, newRefreshToken, mapToUserInfo(user));
    }

    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResourceNotFoundException("User", "authentication", "anonymous");
        }

        Object principal = authentication.getPrincipal();
        User user;

        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        } else if (principal instanceof String) {
            String p = (String) principal;
            user = userRepository.findByEmail(p)
                    .orElseGet(() -> userRepository.findByUsername(p).orElse(null));
            if (user == null) {
                try {
                    UUID id = UUID.fromString(p);
                    user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
                } catch (IllegalArgumentException e) {
                    throw new ResourceNotFoundException("User", "principal", p);
                }
            }
        } else {
            throw new ResourceNotFoundException("User", "principalType", principal.getClass().getName());
        }

        return mapToUserInfo(user);
    }

    private UserInfoResponse mapToUserInfo(User user) {
    // 1. Xác định Primary Role từ dữ liệu có sẵn
    String roleName = null;
    if (user.getPrimaryRole() != null) {
        roleName = user.getPrimaryRole().name();
    } else if (user.getRoles() != null && !user.getRoles().isEmpty()) {
        // Nếu primary_role trong DB trống, lấy role đầu tiên trong danh sách roles
        roleName = user.getRoles().iterator().next().getCode();
    } else {
        // Giá trị dự phòng cuối cùng để tránh lỗi Null ở Frontend
        roleName = "LECTURER"; 
    }

    return new UserInfoResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            roleName, // ⭐ KHÔNG TRẢ VỀ NULL NỮA
            user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
            user.getStatus() != null ? user.getStatus().name() : "ACTIVE"
    );
}

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}