package vn.edu.smd.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
=======
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
>>>>>>> origin/main
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
<<<<<<< HEAD
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.smd.core.security.JwtTokenProvider;
import java.util.UUID;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;
=======
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
>>>>>>> origin/main

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
<<<<<<< HEAD

        // For preflight (OPTIONS) requests, don't write CORS headers here.
        // Let the configured CORS filter (or API Gateway) handle CORS response headers.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        // Không log các request login hoặc public để đỡ rối
        if (path.contains("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("--- [DEBUG FILTER] Request đến: " + path + " ---");

        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String email;

        // 1. Kiểm tra Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[DEBUG FILTER] ❌ Không tìm thấy Header Authorization hoặc sai định dạng.");
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);
        System.out.println("[DEBUG FILTER] ✅ Đã lấy được Token: " + token.substring(0, 10) + "...");

        String userId;
        try {
            userId = tokenProvider.getUserIdFromToken(token);
            System.out.println("[DEBUG FILTER] ✅ UserId trong Token: " + userId);
        } catch (Exception e) {
            System.err.println("[DEBUG FILTER] ❌ Lỗi giải mã Token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Xác thực
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserById(UUID.fromString(userId));
                System.out.println("[DEBUG FILTER] 👤 Tìm thấy User trong DB. Quyền: " + userDetails.getAuthorities());

                if (tokenProvider.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[DEBUG FILTER] 🔓 Đã xác thực thành công! User đã đăng nhập vào Context.");
                } else {
                    System.err.println("[DEBUG FILTER] ❌ Token không hợp lệ (Hết hạn hoặc sai chữ ký).");
                }
            } catch (Exception e) {
                System.err.println("[DEBUG FILTER] ❌ Lỗi khi load user: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
=======
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userId = tokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserById(UUID.fromString(userId));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
>>>>>>> origin/main
}
