package vn.edu.smd.core.module.curriculum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Curriculum;
import vn.edu.smd.core.entity.Faculty;
import vn.edu.smd.core.module.curriculum.dto.CurriculumRequest;
import vn.edu.smd.core.module.curriculum.dto.CurriculumResponse;
import vn.edu.smd.core.repository.CurriculumRepository;
import vn.edu.smd.core.repository.FacultyRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final FacultyRepository facultyRepository;

    public Page<CurriculumResponse> getAllCurriculums(Pageable pageable) {
        return curriculumRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<CurriculumResponse> getAllCurriculums() {
        return curriculumRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CurriculumResponse getCurriculumById(UUID id) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", id));
        return mapToResponse(curriculum);
    }

    public CurriculumResponse getCurriculumByCode(String code) {
        Curriculum curriculum = curriculumRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "code", code));
        return mapToResponse(curriculum);
    }

    public List<CurriculumResponse> getCurriculumsByFaculty(UUID facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty", "id", facultyId);
        }
        return curriculumRepository.findByFacultyId(facultyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CurriculumResponse createCurriculum(CurriculumRequest request) {
        if (curriculumRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Curriculum code already exists");
        }

        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
        }

        Curriculum curriculum = Curriculum.builder()
                .code(request.getCode())
                .name(request.getName())
                .faculty(faculty)
                .totalCredits(request.getTotalCredits())
                .build();

        Curriculum savedCurriculum = curriculumRepository.save(curriculum);
        return mapToResponse(savedCurriculum);
    }

    @Transactional
    public CurriculumResponse updateCurriculum(UUID id, CurriculumRequest request) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", id));

        if (!curriculum.getCode().equals(request.getCode()) 
                && curriculumRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Curriculum code already exists");
        }

        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
        }

        curriculum.setCode(request.getCode());
        curriculum.setName(request.getName());
        curriculum.setFaculty(faculty);
        curriculum.setTotalCredits(request.getTotalCredits());

        Curriculum updatedCurriculum = curriculumRepository.save(curriculum);
        return mapToResponse(updatedCurriculum);
    }

    @Transactional
    public void deleteCurriculum(UUID id) {
        if (!curriculumRepository.existsById(id)) {
            throw new ResourceNotFoundException("Curriculum", "id", id);
        }
        curriculumRepository.deleteById(id);
    }

    private CurriculumResponse mapToResponse(Curriculum curriculum) {
        CurriculumResponse response = new CurriculumResponse();
        response.setId(curriculum.getId());
        response.setCode(curriculum.getCode());
        response.setName(curriculum.getName());
        if (curriculum.getFaculty() != null) {
            response.setFacultyId(curriculum.getFaculty().getId());
            response.setFacultyCode(curriculum.getFaculty().getCode());
            response.setFacultyName(curriculum.getFaculty().getName());
        }
        response.setTotalCredits(curriculum.getTotalCredits());
        response.setCreatedAt(curriculum.getCreatedAt());
        response.setUpdatedAt(curriculum.getUpdatedAt());
        return response;
    }
}
