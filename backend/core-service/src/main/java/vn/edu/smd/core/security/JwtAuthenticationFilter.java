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

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Bỏ qua filter cho các endpoint public (khớp với SecurityConfig permitAll)
        if (isPublicEndpoint(requestPath, method)) {
            log.debug("Bypassing JWT filter for public endpoint: {} {}", method, requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Bỏ qua CORS preflight (OPTIONS) để tránh lỗi duplicate header
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT Filter: Processing request " + method + " " + requestPath);
            System.out.println("JWT Token found: " + (StringUtils.hasText(jwt) ? "YES" : "NO"));

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userIdStr = tokenProvider.getUserIdFromToken(jwt);
                System.out.println("JWT Valid. User ID: " + userIdStr);
                UUID userId = UUID.fromString(userIdStr);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user with ID: {}", userId);
                    System.out.println("User authenticated: " + userDetails.getUsername());
                } else {
                    System.out.println("User details not found for ID: " + userId);
                }
            } else {
                log.debug("No valid JWT token found for request: {} {}", method, requestPath);
                System.out.println("JWT Invalid or not found");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication for request {} {}: {}", method, requestPath, ex.getMessage(), ex);
            System.out.println("JWT Filter Error: " + ex.getMessage());
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Kiểm tra endpoint có thuộc danh sách public (không cần JWT) hay không.
     * Đồng bộ với SecurityConfig để tránh sai lệch.
     */
    private boolean isPublicEndpoint(String path, String method) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/student/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/actuator/");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}