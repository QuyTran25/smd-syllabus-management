package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.UserRole;
import vn.edu.smd.shared.enums.RoleScope;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UserRole entity
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    
    List<UserRole> findByUserId(UUID userId);
    
    List<UserRole> findByRoleId(UUID roleId);
    
    List<UserRole> findByUserIdAndScopeType(UUID userId, RoleScope scopeType);
    
    List<UserRole> findByUserIdAndScopeTypeAndScopeId(UUID userId, RoleScope scopeType, UUID scopeId);
    
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
}
