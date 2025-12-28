package vn.edu.smd.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.smd.core.common.util.JwtUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

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

        try {
            email = jwtUtils.extractUsername(token);
            System.out.println("[DEBUG FILTER] 📧 Email trong Token: " + email);
        } catch (Exception e) {
            System.err.println("[DEBUG FILTER] ❌ Lỗi giải mã Token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Xác thực
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                System.out.println("[DEBUG FILTER] 👤 Tìm thấy User trong DB. Quyền: " + userDetails.getAuthorities());

                if (jwtUtils.validateToken(token)) {
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
}
