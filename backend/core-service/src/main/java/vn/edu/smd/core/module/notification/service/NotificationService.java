package vn.edu.smd.core.module.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Notification;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.notification.dto.NotificationRequest;
import vn.edu.smd.core.module.notification.dto.NotificationResponse;
import vn.edu.smd.core.repository.NotificationRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.core.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final vn.edu.smd.core.service.FCMService fcmService;

    public List<NotificationResponse> getUserNotifications() {
        User currentUser = getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return mapToResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(currentUser);
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        notificationRepository.deleteById(id);
    }

    public Long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    @Transactional
    public NotificationResponse createTestNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setRelatedEntityId(request.getRelatedEntityId());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        
        // Send push notification via FCM
        try {
            java.util.Map<String, String> fcmData = new java.util.HashMap<>();
            fcmData.put("notificationId", saved.getId().toString());
            fcmData.put("type", saved.getType());
            if (saved.getRelatedEntityType() != null) {
                fcmData.put("relatedEntityType", saved.getRelatedEntityType());
            }
            if (saved.getRelatedEntityId() != null) {
                fcmData.put("relatedEntityId", saved.getRelatedEntityId().toString());
            }
            fcmData.put("actionUrl", "/notifications");
            
            String pushBody = saved.getMessage();
            if (pushBody != null && pushBody.length() > 100) {
                pushBody = pushBody.substring(0, 97) + "...";
            }
            
            fcmService.sendNotificationToUser(user, saved.getTitle(), pushBody, fcmData);
        } catch (Exception e) {
            // Log error but don't fail the notification creation
            // Notification is still saved to database even if push fails
        }
        
        return mapToResponse(saved);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUser().getId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRelatedEntityType(notification.getRelatedEntityType());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setPayload(notification.getPayload());
        response.setIsRead(notification.getIsRead());
        response.setReadAt(notification.getReadAt());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
