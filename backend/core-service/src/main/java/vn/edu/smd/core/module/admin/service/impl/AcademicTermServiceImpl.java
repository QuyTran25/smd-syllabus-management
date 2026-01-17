package vn.edu.smd.core.module.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.module.admin.dto.AcademicTermDto;
import vn.edu.smd.core.module.admin.service.AcademicTermService;
import vn.edu.smd.core.repository.AcademicTermRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicTermServiceImpl implements AcademicTermService {

    private final AcademicTermRepository termRepository;

    @Override
    public List<AcademicTermDto> getAllTerms() {
        return termRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AcademicTermDto createTerm(AcademicTermDto dto) {
        if (termRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã học kỳ đã tồn tại!");
        }

        AcademicTerm term = AcademicTerm.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .academicYear(dto.getAcademicYear())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : false)
                .build();

        // Nếu tạo mới là active thì tắt các cái cũ đi
        if (Boolean.TRUE.equals(term.getIsActive())) {
            termRepository.deactivateAllTerms();
        }

        return mapToDto(termRepository.save(term));
    }

    @Override
    @Transactional
    public void deleteTerm(UUID id) {
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ"));
        if (term.getIsActive()) {
            throw new RuntimeException("Không thể xóa học kỳ đang hoạt động");
        }
        termRepository.delete(term);
    }

    @Override
    @Transactional
    public void setActiveTerm(UUID id) {
        // 1. Tắt active tất cả
        termRepository.deactivateAllTerms();

        // 2. Bật active cho cái được chọn
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ"));
        term.setIsActive(true);
        termRepository.save(term);
    }

    private AcademicTermDto mapToDto(AcademicTerm entity) {
        AcademicTermDto dto = new AcademicTermDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setAcademicYear(entity.getAcademicYear());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
}