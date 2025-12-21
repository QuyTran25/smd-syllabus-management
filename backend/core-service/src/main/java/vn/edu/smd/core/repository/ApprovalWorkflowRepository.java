package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.ApprovalWorkflow;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ApprovalWorkflow entity
 */
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID> {
    
    List<ApprovalWorkflow> findByIsActive(Boolean isActive);
}
