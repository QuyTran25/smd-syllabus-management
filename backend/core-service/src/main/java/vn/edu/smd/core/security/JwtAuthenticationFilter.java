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

        // Bỏ qua filter cho các request OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ⭐ QUAN TRỌNG (Theo Main): Chỉ bypass những endpoint không cần auth
            // Việc này giúp giảm tải cho hệ thống khi không phải parse Token cho các request công khai
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
                
                // Sử dụng CustomUserDetailsService để load user từ DB dựa trên ID trong Token
                UserDetails userDetails = customUserDetailsService.loadUserById(UUID.fromString(userId));

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Lưu thông tin xác thực vào SecurityContext của Spring
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user with ID: {}", userId);
                }
            }
        } catch (Exception ex) {
            // Nếu có lỗi xác thực, log lại và vẫn cho request đi tiếp
            // SecurityConfig sẽ chặn lại ở bước sau nếu cần thiết (Tránh treo request)
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