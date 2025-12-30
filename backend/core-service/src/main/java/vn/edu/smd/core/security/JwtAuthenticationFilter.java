package vn.edu.smd.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Giữ logic xử lý OPTIONS để tránh lỗi CORS (Cần thiết cho Frontend)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bypass những endpoint Auth để tránh check token thừa
            String requestPath = request.getRequestURI();
            if (requestPath.contains("/api/auth/login") || 
                requestPath.contains("/api/auth/register") ||
                requestPath.contains("/api/auth/forgot-password") ||
                requestPath.contains("/api/auth/reset-password") ||
                requestPath.contains("/api/auth/refresh") ||
                requestPath.contains("/api/auth/debug-password")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userId = tokenProvider.getUserIdFromToken(jwt);
                
                // ƯU TIÊN MAIN: Xóa comment thừa, giữ code sạch
                UserDetails userDetails = customUserDetailsService.loadUserById(UUID.fromString(userId));

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Lưu thông tin xác thực vào Security Context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user with ID: {}", userId);
                }
            }
        } catch (Exception ex) {
            // Log lỗi nhưng không chặn request, để SecurityConfig xử lý
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
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
}