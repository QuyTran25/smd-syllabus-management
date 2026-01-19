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

/**
 * Repository for User entity
 */
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
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);
    
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "faculty", "department"})
    @Query("SELECT u FROM User u")
    Page<User> findAllWithRoles(Pageable pageable);
    
    /**
     * Tìm Trưởng bộ môn (HOD) theo department_id
     * HOD được xác định qua:
     * 1. User có department_id trùng với department cần tìm
     * 2. User có role HOD
     * Note: Nếu có nhiều HOD trong cùng department, chỉ lấy 1 người đầu tiên
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE u.department.id = :departmentId AND r.code = 'HOD' " +
           "ORDER BY u.createdAt ASC")
    Optional<User> findHodByDepartmentId(@Param("departmentId") UUID departmentId);
    
    /**
     * Tìm HOD qua user_roles với scope_type = DEPARTMENT
     * Note: Nếu có nhiều HOD, chỉ lấy 1 người đầu tiên
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE ur.scopeType = 'DEPARTMENT' AND ur.scopeId = :departmentId AND r.code = 'HOD' " +
           "ORDER BY u.createdAt ASC")
    Optional<User> findHodByScopeId(@Param("departmentId") UUID departmentId);
    
    /**
     * Đếm số lượng HOD trong department (để kiểm tra data integrity)
     */
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE u.department.id = :departmentId AND r.code = 'HOD'")
    long countHodByDepartmentId(@Param("departmentId") UUID departmentId);
    
    /**
     * Tìm tất cả users theo role code (VD: "ADMIN", "LECTURER", "STUDENT")
     * Dùng cho việc gửi notification đến tất cả users có role cụ thể
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "WHERE r.code = :roleCode")
    List<User> findByRoleName(@Param("roleCode") String roleCode);
}
