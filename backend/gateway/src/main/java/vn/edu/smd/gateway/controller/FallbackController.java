package vn.edu.smd.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 * Provides friendly error messages when services are unavailable
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    /**
     * Fallback for Core Service
     */
    @GetMapping("/core-service")
    public ResponseEntity<Map<String, Object>> coreServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("service", "core-service");
        response.put("message", "Core Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "30 seconds");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Fallback for AI Service
     */
    @GetMapping("/ai-service")
    public ResponseEntity<Map<String, Object>> aiServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("service", "ai-service");
        response.put("message", "AI Analysis service is temporarily under maintenance. Other features are still available.");
        response.put("timestamp", LocalDateTime.now());
        response.put("retryAfter", "60 seconds");
        response.put("alternatives", "You can view syllabus details without AI analysis");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
