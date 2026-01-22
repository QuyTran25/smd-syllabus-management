package vn.edu.smd.core.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging Service
 * Handles sending realtime push notifications to users
 * 
 * ⚠️ LƯU Ý: Service này chỉ GỬI push notification
 * Notification vẫn được LƯU VÀO DATABASE như cũ trong NotificationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final UserRepository userRepository;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    /**
     * Gửi push notification cho 1 user
     * 
     * @param user User nhận notification
     * @param title Tiêu đề (GIỮ NGUYÊN từ notification hiện tại)
     * @param body Nội dung rút gọn (100 chars đầu của message)
     * @param data Payload data (notificationId, type, actionUrl, etc.)
     */
    public void sendNotificationToUser(
            User user,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (!firebaseEnabled) {
            log.debug("Firebase disabled, skipping push notification for user {}", user.getId());
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized, cannot send push notification");
            return;
        }

        String fcmToken = user.getFcmToken();
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            log.debug("User {} has no FCM token, skipping push notification", user.getId());
            return;
        }

        try {
            // Build notification message
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/logo.png")
                                    .setBadge("/badge.png")
                                    .setRequireInteraction(true)
                                    .build())
                            .setFcmOptions(WebpushFcmOptions.builder()
                                    .setLink(data != null ? data.get("actionUrl") : "/")
                                    .build())
                            .build())
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            
            log.info("✅ Sent FCM to user {} ({}): {}", 
                    user.getId(), user.getFullName(), response);

        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to send FCM to user {} ({}): {} - {}", 
                    user.getId(), user.getFullName(), e.getErrorCode(), e.getMessage());

            // Handle invalid/expired tokens
            if (isInvalidToken(e)) {
                log.warn("⚠️  Invalid FCM token for user {}, clearing from database", user.getId());
                user.setFcmToken(null);
                user.setFcmTokenUpdatedAt(null);
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("❌ Unexpected error sending FCM to user {}: {}", 
                    user.getId(), e.getMessage(), e);
        }
    }

    /**
     * Gửi push notification cho nhiều users cùng lúc
     * Dùng cho trường hợp gửi cho multiple AA/Principal/Admin
     */
    public void sendNotificationToUsers(
            List<User> users,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (users == null || users.isEmpty()) {
            return;
        }

        log.info("📤 Sending FCM to {} users", users.size());
        
        users.forEach(user -> 
            sendNotificationToUser(user, title, body, data)
        );
    }

    /**
     * Kiểm tra error code có phải token invalid không
     */
    private boolean isInvalidToken(FirebaseMessagingException e) {
        String errorCode = e.getErrorCode().name();
        return "INVALID_ARGUMENT".equals(errorCode) 
            || "UNREGISTERED".equals(errorCode)
            || "REGISTRATION_TOKEN_NOT_REGISTERED".equals(errorCode);
    }

    /**
     * Tạo data payload từ notification entity
     * Helper method để tạo Map<String, String> data
     */
    public Map<String, String> buildDataPayload(
            String notificationId,
            String type,
            String actionUrl,
            Map<String, Object> additionalData
    ) {
        Map<String, String> data = new HashMap<>();
        
        if (notificationId != null) {
            data.put("notificationId", notificationId);
        }
        if (type != null) {
            data.put("type", type);
        }
        if (actionUrl != null) {
            data.put("actionUrl", actionUrl);
        }
        
        // Add additional data (convert to String)
        if (additionalData != null) {
            additionalData.forEach((key, value) -> {
                if (value != null) {
                    data.put(key, value.toString());
                }
            });
        }
        
        return data;
    }

    /**
     * Rút gọn message body để hiển thị trong push notification
     * Push notification nên ngắn gọn (100 chars)
     */
    public String shortenBody(String fullMessage, int maxLength) {
        if (fullMessage == null) {
            return "";
        }
        
        if (fullMessage.length() <= maxLength) {
            return fullMessage;
        }
        
        return fullMessage.substring(0, maxLength) + "...";
    }
}
