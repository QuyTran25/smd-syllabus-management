package vn.edu.smd.core.module.semester.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Semester;
import vn.edu.smd.core.module.semester.dto.SemesterRequest;
import vn.edu.smd.core.module.semester.dto.SemesterResponse;
import vn.edu.smd.core.repository.SemesterRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public Page<SemesterResponse> getAllSemesters(Pageable pageable) {
        return semesterRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SemesterResponse getSemesterById(UUID id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
        return mapToResponse(semester);
    }

    public SemesterResponse getCurrentSemester() {
        Semester semester = semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active semester found"));
        return mapToResponse(semester);
    }

    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        if (semesterRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Semester with code " + request.getCode() + " already exists");
        }

        Semester semester = Semester.builder()
                .code(request.getCode())
                .name(request.getName())
                .semesterNumber(request.getSemesterNumber())
                .academicYear(request.getAcademicYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : false)
                .build();

        semester = semesterRepository.save(semester);
        return mapToResponse(semester);
    }

    @Transactional
    public SemesterResponse updateSemester(UUID id, SemesterRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));

        // Check code uniqueness if changed
        if (!semester.getCode().equals(request.getCode()) && 
            semesterRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Semester with code " + request.getCode() + " already exists");
        }

        semester.setCode(request.getCode());
        semester.setName(request.getName());
        semester.setSemesterNumber(request.getSemesterNumber());
        semester.setAcademicYear(request.getAcademicYear());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            semester.setIsActive(request.getIsActive());
        }

        semester = semesterRepository.save(semester);
        return mapToResponse(semester);
    }

    @Transactional
    public void deleteSemester(UUID id) {
        if (!semesterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Semester not found with id: " + id);
        }
        semesterRepository.deleteById(id);
    }

    private SemesterResponse mapToResponse(Semester semester) {
        return SemesterResponse.builder()
                .id(semester.getId())
                .code(semester.getCode())
                .name(semester.getName())
                .semesterNumber(semester.getSemesterNumber())
                .academicYear(semester.getAcademicYear())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .isActive(semester.getIsActive())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();
    }
}
