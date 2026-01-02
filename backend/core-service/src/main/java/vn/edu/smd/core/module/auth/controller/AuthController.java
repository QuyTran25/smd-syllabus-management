package vn.edu.smd.core.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Authentication", description = "Authentication & Authorization APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // ==================================================
    // Debug endpoints - REMOVE or PROTECT in production
    // ==================================================

    /**
     * TEST ONLY: Check if password matches stored hash
     * REMOVE THIS ENDPOINT IN PRODUCTION!
     */
    @GetMapping("/test-password/{email}/{password}")
    public ResponseEntity<ApiResponse<String>> testPassword(@PathVariable String email, @PathVariable String password) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("User not found: " + email));
        }
        var user = userOpt.get();
        String hashInDb = user.getPasswordHash();
        boolean matches = passwordEncoder.matches(password, hashInDb);
        String result = String.format("Email: %s, Hash: %s, Password: %s, Matches: %s",
                email, hashInDb, password, matches);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * TEST ONLY: Generate and verify BCrypt hash
     * REMOVE THIS ENDPOINT IN PRODUCTION!
     */
    @GetMapping("/test-bcrypt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testBcrypt(
            @RequestParam(defaultValue = "123456") String password,
            @RequestParam(required = false) String hash) {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("newHash", encoder.encode(password));

        if (hash != null && !hash.isEmpty()) {
            result.put("providedHash", hash);
            result.put("matches", encoder.matches(password, hash));
        }

        String standardHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        result.put("standardHash", standardHash);
        result.put("matchesStandard", encoder.matches(password, standardHash));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================================================
    // Production endpoints
    // ==================================================

    @Operation(summary = "Login", description = "User login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = performLogin(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (AuthenticationException e) {
            log.error("Login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            log.error("Internal server error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @Operation(summary = "Register", description = "Register new user account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = performRegister(request);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed"));
        }
    }

    @Operation(summary = "Logout", description = "User logout (clear security context)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @Operation(summary = "Refresh Token", description = "Refresh access token using refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = performRefreshToken(request);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired refresh token"));
        }
    }

    @Operation(summary = "Forgot Password", description = "Request password reset link")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            performForgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
        } catch (ResourceNotFoundException e) {
            // Không tiết lộ email tồn tại hay không (security)
            return ResponseEntity.ok(ApiResponse.success("If email exists, reset link sent", null));
        } catch (Exception e) {
            log.error("Forgot password failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @Operation(summary = "Reset Password", description = "Reset password with token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            performResetPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
        } catch (Exception e) {
            log.error("Reset password failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired reset token"));
        }
    }

    @Operation(summary = "Get Current User", description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        try {
            UserInfoResponse response = performGetCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
    }

    // ==================== Private Business Logic Methods ====================

    @Transactional
    private AuthResponse performLogin(LoginRequest request) {
        String email = request.getEmail().trim();
        String rawPassword = request.getPassword().trim();

        log.debug("Login attempt for email: {}", email);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        log.info("User {} logged in successfully", user.getEmail());

        return new AuthResponse(accessToken, refreshToken, mapToUserInfo(user));
    }

    @Transactional
    private AuthResponse performRegister(RegisterRequest request) {
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

        // Assign default role (LECTURER or STUDENT depending on your logic)
        roleRepository.findByCode("LECTURER")
                .ifPresent(role -> user.setRoles(Set.of(role)));

        User savedUser = userRepository.save(user);

        // Authenticate after register
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(accessToken, refreshToken, mapToUserInfo(savedUser));
    }

    @Transactional
    private AuthResponse performRefreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findByIdWithRoles(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(newAccessToken, newRefreshToken, mapToUserInfo(user));
    }

    private UserInfoResponse performGetCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User", "authentication", "anonymous");
        }

        Object principal = authentication.getPrincipal();
        User user;

        if (principal instanceof UserPrincipal userPrincipal) {
            user = userRepository.findByIdWithRoles(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        } else {
            throw new ResourceNotFoundException("User", "principalType", principal.getClass().getName());
        }

        return mapToUserInfo(user);
    }

    @Transactional
    private void performForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("Processing forgot password for email: {}", user.getEmail());
        // TODO: Generate reset token, save to DB, send email
    }

    @Transactional
    private void performResetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    private UserInfoResponse mapToUserInfo(User user) {
        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            try {
                roleNames = user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet());
            } catch (Exception e) {
                log.warn("Could not load roles for user {}: {}", user.getId(), e.getMessage());
            }
        }

        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getPrimaryRole() != null ? user.getPrimaryRole().name() : null,
                roleNames,
                user.getStatus().name()
        );
    }
}