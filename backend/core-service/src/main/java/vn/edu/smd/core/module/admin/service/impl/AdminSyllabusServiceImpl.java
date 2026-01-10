package vn.edu.smd.core.module.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.admin.service.AdminSyllabusService;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminSyllabusServiceImpl implements AdminSyllabusService {

    private final SyllabusVersionRepository syllabusRepository;

    @Override
    @Transactional
    public void publishSyllabus(UUID id, String comment) {
        SyllabusVersion syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));

        // Chỉ cho phép xuất bản nếu trạng thái đang là APPROVED (đã được Hiệu trưởng duyệt)
        if (syllabus.getStatus() != SyllabusStatus.APPROVED) {
            throw new RuntimeException("Đề cương chưa được phê duyệt, không thể xuất hành!");
        }

        syllabus.setStatus(SyllabusStatus.PUBLISHED);
        syllabus.setPublishedAt(LocalDateTime.now());
        // Nếu muốn lưu comment vào log thì xử lý thêm ở đây
        
        syllabusRepository.save(syllabus);
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

        syllabusRepository.save(syllabus);
    }

    @Override
    @Transactional
    public void updateEffectiveDate(UUID id, String dateStr) {
        SyllabusVersion syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found"));
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            syllabus.setEffectiveDate(date);
            syllabusRepository.save(syllabus);
        } catch (Exception e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ (YYYY-MM-DD)");
        }
    }
}