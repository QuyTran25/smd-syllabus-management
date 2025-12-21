package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.ApprovalHistory;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ApprovalHistory entity
 */
@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, UUID> {
    
    List<ApprovalHistory> findBySyllabusVersionId(UUID syllabusVersionId);
    
    List<ApprovalHistory> findByActorId(UUID actorId);
    
    List<ApprovalHistory> findBySyllabusVersionIdOrderByCreatedAtDesc(UUID syllabusVersionId);
}
