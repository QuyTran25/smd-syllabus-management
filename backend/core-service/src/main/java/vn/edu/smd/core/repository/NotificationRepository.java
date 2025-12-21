package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.Notification;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserId(UUID userId);
    
    List<Notification> findByUserIdAndIsRead(UUID userId, Boolean isRead);
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    long countByUserIdAndIsRead(UUID userId, Boolean isRead);
}
