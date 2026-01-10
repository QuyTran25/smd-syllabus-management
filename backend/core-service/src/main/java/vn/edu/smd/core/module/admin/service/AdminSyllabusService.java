package vn.edu.smd.core.module.admin.service;

import java.util.UUID;

public interface AdminSyllabusService {
    void publishSyllabus(UUID id, String comment);
    void unpublishSyllabus(UUID id, String reason);
    void updateEffectiveDate(UUID id, String dateStr);
}