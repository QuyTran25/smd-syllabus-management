package vn.edu.smd.core.module.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.notification.dto.NotificationRequest;
import vn.edu.smd.core.module.notification.dto.NotificationResponse;
import vn.edu.smd.core.module.notification.service.NotificationService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notification Management", description = "Notification management APIs")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get user notifications", description = "Get all notifications for current user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications() {
        List<NotificationResponse> notifications = notificationService.getUserNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @Operation(summary = "Get notification by ID", description = "Get notification details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable UUID id) {
        NotificationResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @Operation(summary = "Mark notification as read", description = "Mark specific notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for current user")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @Operation(summary = "Delete notification", description = "Delete notification by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "Create test notification", description = "Create test notification (development only)")
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<NotificationResponse>> createTestNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse notification = notificationService.createTestNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Test notification created", notification));
    }
}
