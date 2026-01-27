package vn.edu.smd.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.shared.enums.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    List<User> findByStatus(UserStatus status);
    List<User> findByFacultyId(UUID facultyId);
    List<User> findByDepartmentId(UUID departmentId);
    
    // Hàm xem chi tiết (có fetch các bảng con)
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "faculty", "department"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);
    
    // 🔥 HÀM GỐC GIÚP HIỂN THỊ DANH SÁCH (Không lọc, không search, nhưng chạy 100% OK)
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "faculty", "department"})
    @Query("SELECT u FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);
    
    // --- Các hàm HOD ---
    @Query(value = "SELECT u.* FROM core_service.users u " +
           "JOIN core_service.user_roles ur ON u.id = ur.user_id " +
           "JOIN core_service.roles r ON ur.role_id = r.id " +
           "WHERE u.department_id = :departmentId AND r.code = 'HOD' " +
           "ORDER BY u.created_at ASC " +
           "LIMIT 1", nativeQuery = true)
    Optional<User> findHodByDepartmentId(@Param("departmentId") UUID departmentId);
    
    @Query(value = "SELECT u.* FROM core_service.users u " +
           "JOIN core_service.user_roles ur ON u.id = ur.user_id " +
           "JOIN core_service.roles r ON ur.role_id = r.id " +
           "WHERE ur.scope_type = 'DEPARTMENT' AND ur.scope_id = :departmentId AND r.code = 'HOD' " +
           "ORDER BY u.created_at ASC " +
           "LIMIT 1", nativeQuery = true)
    Optional<User> findHodByScopeId(@Param("departmentId") UUID departmentId);
    
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE u.department.id = :departmentId AND r.code = 'HOD'")
    long countHodByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE r.code = :roleCode")
    List<User> findByRoleName(@Param("roleCode") String roleCode);
}
