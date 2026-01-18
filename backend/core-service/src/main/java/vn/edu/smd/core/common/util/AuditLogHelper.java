package vn.edu.smd.core.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.smd.core.entity.AuditLog;
import vn.edu.smd.core.repository.AuditLogRepository;
import vn.edu.smd.shared.enums.AuditStatus;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogHelper {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String entityName, UUID entityId, String action, UUID actorId, String description) {
        logAction(entityName, entityId, action, actorId, description, AuditStatus.SUCCESS);
    }

    public void logAction(String entityName, UUID entityId, String action, UUID actorId, String description, AuditStatus status) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();

            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .actorId(actorId)
                    .description(description)
                    .status(status)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't fail the main operation if audit logging fails
            System.err.println("❌ Failed to create audit log: " + e.getMessage());
        }
    }

    public void logLogin(UUID userId, String userEmail, boolean success) {
        String description = success 
                ? String.format("Đăng nhập thành công (user: %s)", userEmail)
                : String.format("Đăng nhập thất bại (user: %s)", userEmail);
        
        logAction("System", null, "LOGIN", userId, description, 
                success ? AuditStatus.SUCCESS : AuditStatus.FAILED);
    }

    public void logLogout(UUID userId, String userEmail) {
        logAction("System", null, "LOGOUT", userId, 
                String.format("Đăng xuất hệ thống (user: %s)", userEmail));
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            System.err.println("Failed to get IP address: " + e.getMessage());
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            System.err.println("Failed to get User-Agent: " + e.getMessage());
        }
        return null;
    }
}