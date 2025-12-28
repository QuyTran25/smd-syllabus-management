package vn.edu.smd.core.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
<<<<<<< HEAD
import vn.edu.smd.core.module.auth.dto.AuthResponse;
import vn.edu.smd.core.module.auth.dto.ForgotPasswordRequest;
import vn.edu.smd.core.module.auth.dto.ResetPasswordRequest;
import vn.edu.smd.core.module.auth.dto.UserInfoResponse;
import vn.edu.smd.core.module.auth.dto.LoginRequest;
import vn.edu.smd.core.module.auth.dto.RegisterRequest;
import vn.edu.smd.core.module.auth.dto.RefreshTokenRequest;
=======
import vn.edu.smd.core.module.auth.dto.*;
>>>>>>> origin/main
import vn.edu.smd.core.module.auth.service.AuthService;

@Tag(name = "Authentication", description = "Authentication & Authorization APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "User login with email and password")
    @PostMapping("/login")
<<<<<<< HEAD
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        // Prefer user embedded in AuthResponse (populated by service)
        UserInfoResponse userInfo = authResponse.getUser();

        return ResponseEntity.ok(java.util.Map.of(
            "user", userInfo,
            "accessToken", authResponse.getAccessToken(),
            "refreshToken", authResponse.getRefreshToken()
        ));
=======
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
>>>>>>> origin/main
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

    @Operation(summary = "Get Current User", description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
