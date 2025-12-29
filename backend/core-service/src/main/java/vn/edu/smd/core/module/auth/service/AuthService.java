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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        String rawPassword = request.getPassword().trim();

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);

        try {
            System.out.println("Step 1: Calling authenticationManager.authenticate()");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );
            System.out.println("Step 2: Authentication successful");
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Step 3: SecurityContext set");
            
            String accessToken = tokenProvider.generateToken(authentication);
            System.out.println("Step 4: Access token generated");
            
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            System.out.println("Step 5: Refresh token generated");
            
            System.out.println("=== LOGIN SUCCESS ===");
            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            System.out.println("=== LOGIN ERROR ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
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

        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(accessToken, refreshToken);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
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

        // TODO: Generate reset token and send email
        // For now, just log
        log.info("Password reset requested for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // TODO: Validate token and reset password
        // For now, just throw exception
        throw new BadRequestException("Reset password feature not implemented yet");
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Use findByIdWithRoles to eager load roles and avoid LazyInitializationException
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
