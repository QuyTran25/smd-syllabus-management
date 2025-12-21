package vn.edu.smd.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.smd.core.entity.SystemSetting;

import java.util.Optional;

/**
 * Repository for SystemSetting entity
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    
    Optional<SystemSetting> findByKey(String key);
}
