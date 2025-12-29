package vn.edu.smd.core.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        try {
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                throw new IllegalStateException("JWT secret is not configured. Set app.jwt.secret in application.properties");
            }
            
            log.info("Initializing JWT Token Provider...");
            log.info("JWT Secret length: {}", jwtSecret.length());
            
            // Kiểm tra xem secret có phải là base64 không
            if (isBase64(jwtSecret)) {
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                this.key = Keys.hmacShaKeyFor(keyBytes);
                log.info("Using base64 encoded JWT secret");
            } else {
                // Nếu không phải base64, dùng chuỗi trực tiếp
                byte[] keyBytes = jwtSecret.getBytes();
                if (keyBytes.length < 32) {
                    log.warn("JWT secret is too short ({} bytes). For HS512, at least 32 bytes are recommended.", keyBytes.length);
                }
                this.key = Keys.hmacShaKeyFor(keyBytes);
                log.info("Using plain text JWT secret");
            }
            
            log.info("JWT Token Provider initialized successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize JWT Token Provider", e);
            throw new RuntimeException("JWT initialization failed: " + e.getMessage(), e);
        }
    }

    private boolean isBase64(String str) {
        try {
            Decoders.BASE64.decode(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

            String token = Jwts.builder()
                    .setSubject(userPrincipal.getId().toString())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            
            log.debug("Generated JWT token for user: {}", userPrincipal.getEmail());
            return token;
            
        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public String generateRefreshToken(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

            return Jwts.builder()
                    .setSubject(userPrincipal.getId().toString())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating refresh token", e);
            throw new RuntimeException("Refresh token generation failed", e);
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        } catch (Exception ex) {
            log.error("Unexpected JWT validation error", ex);
        }
        return false;
    }
}