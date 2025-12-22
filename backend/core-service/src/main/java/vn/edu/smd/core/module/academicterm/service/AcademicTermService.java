package vn.edu.smd.core.module.academicterm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.entity.Semester;
import vn.edu.smd.core.module.academicterm.dto.AcademicTermRequest;
import vn.edu.smd.core.module.academicterm.dto.AcademicTermResponse;
import vn.edu.smd.core.module.semester.dto.SemesterResponse;
import vn.edu.smd.core.repository.AcademicTermRepository;
import vn.edu.smd.core.repository.SemesterRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicTermService {

    private final AcademicTermRepository academicTermRepository;
    private final SemesterRepository semesterRepository;

    public Page<AcademicTermResponse> getAllAcademicTerms(Pageable pageable) {
        return academicTermRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<AcademicTermResponse> getAllAcademicTerms() {
        return academicTermRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AcademicTermResponse getAcademicTermById(UUID id) {
        AcademicTerm term = academicTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", id));
        return mapToResponse(term);
    }

    public AcademicTermResponse getAcademicTermByCode(String code) {
        AcademicTerm term = academicTermRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "code", code));
        return mapToResponse(term);
    }

    public List<AcademicTermResponse> getActiveAcademicTerms() {
        return academicTermRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicTermResponse createAcademicTerm(AcademicTermRequest request) {
        if (academicTermRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Academic term code already exists");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        AcademicTerm term = AcademicTerm.builder()
                .code(request.getCode())
                .name(request.getName())
                .academicYear(request.getAcademicYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : false)
                .build();

        AcademicTerm savedTerm = academicTermRepository.save(term);
        return mapToResponse(savedTerm);
    }

    @Transactional
    public AcademicTermResponse updateAcademicTerm(UUID id, AcademicTermRequest request) {
        AcademicTerm term = academicTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", id));

        if (!term.getCode().equals(request.getCode()) 
                && academicTermRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Academic term code already exists");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        term.setCode(request.getCode());
        term.setName(request.getName());
        term.setAcademicYear(request.getAcademicYear());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            term.setIsActive(request.getIsActive());
        }

        AcademicTerm updatedTerm = academicTermRepository.save(term);
        return mapToResponse(updatedTerm);
    }

    @Transactional
    public void deleteAcademicTerm(UUID id) {
        if (!academicTermRepository.existsById(id)) {
            throw new ResourceNotFoundException("AcademicTerm", "id", id);
        }
        academicTermRepository.deleteById(id);
    }

    public AcademicTermResponse getCurrentAcademicYear() {
        AcademicTerm currentTerm = academicTermRepository.findFirstByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active academic year found"));
        return mapToResponse(currentTerm);
    }

    public List<SemesterResponse> getSemestersByAcademicYear(UUID id) {
        AcademicTerm term = academicTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", id));
        
        return semesterRepository.findByAcademicYear(term.getAcademicYear()).stream()
                .map(this::mapSemesterToResponse)
                .collect(Collectors.toList());
    }

    private AcademicTermResponse mapToResponse(AcademicTerm term) {
        AcademicTermResponse response = new AcademicTermResponse();
        response.setId(term.getId());
        response.setCode(term.getCode());
        response.setName(term.getName());
        response.setAcademicYear(term.getAcademicYear());
        response.setStartDate(term.getStartDate());
        response.setEndDate(term.getEndDate());
        response.setIsActive(term.getIsActive());
        response.setCreatedAt(term.getCreatedAt());
        response.setUpdatedAt(term.getUpdatedAt());
        return response;
    }

    private SemesterResponse mapSemesterToResponse(Semester semester) {
        SemesterResponse response = new SemesterResponse();
        response.setId(semester.getId());
        response.setCode(semester.getCode());
        response.setName(semester.getName());
        response.setSemesterNumber(semester.getSemesterNumber());
        response.setAcademicYear(semester.getAcademicYear());
        response.setStartDate(semester.getStartDate());
        response.setEndDate(semester.getEndDate());
        response.setIsActive(semester.getIsActive());
        response.setCreatedAt(semester.getCreatedAt());
        response.setUpdatedAt(semester.getUpdatedAt());
        return response;
    }
}
