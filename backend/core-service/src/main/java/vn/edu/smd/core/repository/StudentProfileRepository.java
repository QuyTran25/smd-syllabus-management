package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.StudentProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for StudentProfile entity
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    /**
     * Find profile by user ID
     */
    Optional<StudentProfile> findByUserId(UUID userId);

    /**
     * Find profile by student code
     */
    Optional<StudentProfile> findByStudentCode(String studentCode);

    /**
     * Find all profiles by curriculum ID
     */
    List<StudentProfile> findByCurriculumId(UUID curriculumId);

    /**
     * Find all profiles by enrollment year
     */
    List<StudentProfile> findByEnrollmentYear(Integer enrollmentYear);

    /**
     * Find all profiles by curriculum and enrollment year
     */
    List<StudentProfile> findByCurriculumIdAndEnrollmentYear(UUID curriculumId, Integer enrollmentYear);

    /**
     * Check if student code exists
     */
    boolean existsByStudentCode(String studentCode);

    /**
     * Count students by curriculum
     */
    long countByCurriculumId(UUID curriculumId);
}
