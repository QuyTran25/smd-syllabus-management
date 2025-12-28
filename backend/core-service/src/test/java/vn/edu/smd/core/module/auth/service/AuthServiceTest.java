package vn.edu.smd.core.module.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.auth.dto.AuthResponse;
import vn.edu.smd.core.module.auth.dto.LoginRequest;
import vn.edu.smd.core.module.auth.dto.RegisterRequest;
import vn.edu.smd.core.module.auth.dto.RefreshTokenRequest;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.core.security.JwtTokenProvider;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider tokenProvider;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setup() {
        // MockitoExtension handles init
    }

    @Test
    void login_success_returnsTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(auth)).thenReturn("refresh-token");

        var resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("access-token", resp.getAccessToken());
        assertEquals("refresh-token", resp.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_success_encodesPasswordAndReturnsTokens() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("plainpass");
        req.setFullName("New User");
        req.setPhoneNumber("0123456789");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded-pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("access");
        when(tokenProvider.generateRefreshToken(auth)).thenReturn("refresh");

        var resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("access", resp.getAccessToken());
        assertEquals("refresh", resp.getRefreshToken());
        verify(passwordEncoder).encode("plainpass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void refreshToken_invalid_throwsBadRequest() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("bad-token");

        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.refreshToken(req));
    }

    @Test
    void refreshToken_valid_returnsNewTokens() {
        String userId = UUID.randomUUID().toString();
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("good-token");

        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("good-token")).thenReturn(userId);

        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setEmail("a@b.com");

        when(userRepository.findById(UUID.fromString(userId))).thenReturn(Optional.of(user));

        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("new-refresh");

        var resp = authService.refreshToken(req);

        assertNotNull(resp);
        assertEquals("new-access", resp.getAccessToken());
        assertEquals("new-refresh", resp.getRefreshToken());
    }
}
