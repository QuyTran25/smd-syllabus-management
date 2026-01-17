package vn.edu.smd.core.module.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.admin.service.AdminSyllabusService;
import vn.edu.smd.core.module.syllabus.service.SyllabusService;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSyllabusServiceImpl implements AdminSyllabusService {

    private final SyllabusVersionRepository syllabusRepository;
    private final SyllabusService syllabusService;

    @Override
    @Transactional
    public void publishSyllabus(UUID id, String comment) {
        SyllabusVersion syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        log.info("📌 [PublishSyllabus] Tìm thấy syllabus: {} (Status: {})", id, syllabus.getStatus());

        // 🔥 FIX: Cho phép publish từ APPROVED hoặc đã là PUBLISHED (re-publish)
        if (syllabus.getStatus() != SyllabusStatus.APPROVED && syllabus.getStatus() != SyllabusStatus.PUBLISHED) {
            throw new RuntimeException("Đề cương chưa được phê duyệt, không thể xuất hành! (Status: " + syllabus.getStatus() + ")");
        }

        syllabus.setStatus(SyllabusStatus.PUBLISHED);
        // 🔥 FIX: Chỉ set publishedAt lần đầu, không ghi đè nếu đã publish rồi
        if (syllabus.getPublishedAt() == null) {
            syllabus.setPublishedAt(LocalDateTime.now());
        }
        // Nếu muốn lưu comment vào log thì xử lý thêm ở đây
        
        SyllabusVersion savedSyllabus = syllabusRepository.save(syllabus);
        log.info("📌 [PublishSyllabus] Lưu vào DB: {} (Status: {})", id, savedSyllabus.getStatus());
        
        // Gửi thông báo cho sinh viên khi xuất hành
        try {
            log.info("📌 [PublishSyllabus] Bắt đầu gửi thông báo cho sinh viên...");
            syllabusService.notifyStudentsOnPublish(savedSyllabus);
            log.info("📌 [PublishSyllabus] Gửi thông báo xong!");
        } catch (Exception e) {
            log.error("❌ [PublishSyllabus] LỖI khi gửi thông báo: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi gửi thông báo: " + e.getMessage(), e);
        }
        
        log.info("✅ Đã xuất hành đề cương {} với comment: {}", id, comment);
    }

    @Override
    @Transactional
    public void publishSyllabus(UUID id, String comment, String effectiveDate) {
        // Call main method first
        publishSyllabus(id, comment);
        
        // Then set effective date if provided
        if (effectiveDate != null && !effectiveDate.isEmpty()) {
            try {
                updateEffectiveDate(id, effectiveDate);
                log.info("📌 [PublishSyllabus] Set effective date: {}", effectiveDate);
            } catch (Exception e) {
                log.warn("⚠️ [PublishSyllabus] Failed to set effective date: {}", e.getMessage());
                // Don't fail the whole publish if effective date is invalid
            }
        }
    }

    @Override
    @Transactional
    public void unpublishSyllabus(UUID id, String reason) {
        SyllabusVersion syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        syllabus.setUnpublishReason(reason);
        syllabus.setUnpublishedAt(LocalDateTime.now());
        // Chuyển về INACTIVE hoặc DRAFT tùy nghiệp vụ
        syllabus.setStatus(SyllabusStatus.INACTIVE); 

        SyllabusVersion savedSyllabus = syllabusRepository.save(syllabus);
        
        // Gửi thông báo cho sinh viên khi gỡ bỏ đề cương
        syllabusService.notifyStudentsOnUnpublish(savedSyllabus, reason);
        
        log.info("Đã gỡ bỏ đề cương {} với lý do: {}", id, reason);
    }

    @Override
    @Transactional
    public void updateEffectiveDate(UUID id, String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new RuntimeException("Ngày hiệu lực không được để trống");
        }
        
        SyllabusVersion syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            syllabus.setEffectiveDate(date);
            syllabusRepository.save(syllabus);
            log.info("✅ Updated effective date for syllabus {} to {}", id, dateStr);
        } catch (Exception e) {
            log.error("❌ Failed to parse date: {}", dateStr, e);
            throw new RuntimeException("Định dạng ngày không hợp lệ (YYYY-MM-DD): " + e.getMessage());
        }
    }
}