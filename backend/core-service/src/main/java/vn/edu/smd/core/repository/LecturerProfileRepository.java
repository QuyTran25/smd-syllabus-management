package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.LecturerProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for LecturerProfile entity
 */
@Repository
public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, UUID> {

    /**
     * Find profile by user ID
     */
    Optional<LecturerProfile> findByUserId(UUID userId);

    /**
     * Find profile by lecturer code
     */
    Optional<LecturerProfile> findByLecturerCode(String lecturerCode);

    /**
     * Find all profiles by department ID
     */
    List<LecturerProfile> findByDepartmentId(UUID departmentId);

    /**
     * Find all profiles by title
     */
    List<LecturerProfile> findByTitle(String title);

    /**
     * Check if lecturer code exists
     */
    boolean existsByLecturerCode(String lecturerCode);

    /**
     * Count lecturers by department
     */
    long countByDepartmentId(UUID departmentId);
}
