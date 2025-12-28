package vn.edu.smd.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod; // <--- Import quan trọng
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // <--- Import quan trọng
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.edu.smd.core.security.CustomUserDetailsService;
import vn.edu.smd.core.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration("legacySecurityConfig")
@Profile("legacy")
@EnableWebSecurity
@EnableMethodSecurity // <--- Bật tính năng phân quyền chi tiết (Production Ready)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // CORS is handled by the API Gateway (globalcors). Disable backend CORS here
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 1. --- QUAN TRỌNG: MỞ CỬA CHO CORS PRE-FLIGHT ---
                // Trình duyệt sẽ gửi request OPTIONS để check trước khi gửi request thật.
                // Nếu chặn request này, trình duyệt sẽ báo lỗi CORS/403.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 2. Cho phép Login công khai
                .requestMatchers("/api/auth/**").permitAll()

                // 3. TẤT CẢ request khác BẮT BUỘC phải có Token xịn
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        // F1 dùng NoOp, F2 nhớ đổi sang BCryptPasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder()); 
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Passwords in DB are BCrypt-hashed; use BCryptPasswordEncoder to verify them.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép Frontend (localhost:3000)
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); 
        
        // Cho phép đầy đủ các method
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Cho phép tất cả header (Authorization, Content-Type, ...)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Cho phép gửi credentials (Cookies, Authorization Header)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}