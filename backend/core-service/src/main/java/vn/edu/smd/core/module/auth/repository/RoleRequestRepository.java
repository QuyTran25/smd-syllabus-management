package vn.edu.smd.core.module.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smd.core.module.auth.entity.RoleRequest;

import java.util.List;
import java.util.UUID;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, UUID> {
    List<RoleRequest> findByStatus(String status);
}
