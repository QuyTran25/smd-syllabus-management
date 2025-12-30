package vn.edu.smd.core.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.auth.dto.*;
import vn.edu.smd.core.module.auth.service.AuthService;
import vn.edu.smd.core.repository.UserRepository;

@Tag(name = "Authentication", description = "Authentication & Authorization APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @GetMapping("/test-password/{email}/{password}")
    public ResponseEntity<ApiResponse<String>> testPassword(@PathVariable String email, @PathVariable String password) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("User not found: " + email));
        }
        var user = userOpt.get();
        String hashInDb = user.getPassword();
        boolean matches = passwordEncoder.matches(password, hashInDb);
        String result = String.format("Email: %s, Hash: %s, Password: %s, Matches: %s", 
            email, hashInDb, password, matches);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Login", description = "User login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(summary = "Register", description = "Register new user account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @Operation(summary = "Logout", description = "User logout")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @Operation(summary = "Refresh Token", description = "Refresh access token using refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @Operation(summary = "Forgot Password", description = "Request password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @Operation(summary = "Reset Password", description = "Reset password with token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @Operation(summary = "Test BCrypt", description = "Test BCrypt hash generation and verification")
    @GetMapping("/test-bcrypt")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> testBcrypt(
            @RequestParam(defaultValue = "123456") String password,
            @RequestParam(required = false) String hash) {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("password", password);
        result.put("newHash", encoder.encode(password));
        
        if (hash != null && !hash.isEmpty()) {
            result.put("providedHash", hash);
            result.put("matches", encoder.matches(password, hash));
        }
        
        // Test với hash chuẩn
        String standardHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        result.put("standardHash", standardHash);
        result.put("matchesStandard", encoder.matches(password, standardHash));
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get Current User", description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
