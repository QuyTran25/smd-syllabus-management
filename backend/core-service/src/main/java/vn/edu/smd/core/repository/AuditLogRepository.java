package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.AuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    List<AuditLog> findByEntityNameAndEntityId(String entityName, UUID entityId);
    
    List<AuditLog> findByActorId(UUID actorId);
    
    List<AuditLog> findByEntityName(String entityName);
    
    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, UUID entityId);
    
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);
}
