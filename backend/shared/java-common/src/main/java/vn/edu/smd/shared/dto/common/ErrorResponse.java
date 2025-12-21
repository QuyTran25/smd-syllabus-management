package vn.edu.smd.shared.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Detailed error response for API errors
 * Used in ApiResponse.error field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Error code for programmatic error handling
     */
    private String errorCode;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Detailed error description
     */
    private String details;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * Validation errors (field-level)
     */
    private Map<String, String> validationErrors;
    
    /**
     * Stack trace (only in development)
     */
    private List<String> stackTrace;
    
    /**
     * Timestamp when error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Create simple error response
     */
    public static ErrorResponse of(String errorCode, int status, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create validation error response
     */
    public static ErrorResponse validationError(
            int status, 
            String message, 
            Map<String, String> validationErrors
    ) {
        return ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .status(status)
                .message(message)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
