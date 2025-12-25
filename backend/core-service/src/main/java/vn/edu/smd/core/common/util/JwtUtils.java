package vn.edu.smd.core.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    // --- QUAN TRỌNG: CỐ ĐỊNH SECRET KEY ---
    // Phải là một chuỗi dài (ít nhất 256-bit / 32 ký tự) để đảm bảo an toàn
    // Đừng dùng random key nữa
    private static final String SECRET_KEY = "DayLaMotChuoiBiMatRatDaiVaKhoDoanDeDamBaoAnToanChoTokenCuaBan123456"; 

    private static final long EXPIRATION_TIME = 86400000; // 1 ngày (ms)

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // In lỗi ra terminal để debug nếu token sai
            System.err.println("JWT Validation Error: " + e.getMessage());
            return false;
        }
    }
}