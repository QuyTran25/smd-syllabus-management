package vn.edu.smd.core.module.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordDebugController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/debug-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugPassword(
            @RequestParam String email,
            @RequestParam String password) {
        
        Map<String, Object> result = new HashMap<>();
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            result.put("error", "User not found");
            return ResponseEntity.ok(ApiResponse.success(result));
        }
        
        String storedHash = user.getPasswordHash();
        boolean matches = passwordEncoder.matches(password, storedHash);
        String newHash = passwordEncoder.encode(password);
        
        result.put("email", email);
        result.put("storedHashLength", storedHash != null ? storedHash.length() : 0);
        result.put("storedHashFirst20", storedHash != null ? storedHash.substring(0, Math.min(20, storedHash.length())) : null);
        result.put("passwordMatches", matches);
        result.put("newHashForPassword", newHash);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/reset-password-direct")
    public ResponseEntity<ApiResponse<String>> resetPasswordDirect(
            @RequestParam String email,
            @RequestParam String newPassword) {
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        
        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success("Password updated. New hash: " + newHash));
    }
}
