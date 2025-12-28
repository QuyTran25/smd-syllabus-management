package vn.edu.smd.core.module.clo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.CLO;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.clo.dto.CloRequest;
import vn.edu.smd.core.module.clo.dto.CloResponse;
import vn.edu.smd.core.repository.CLORepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloService {

    private final CLORepository cloRepository;
    private final SyllabusVersionRepository syllabusRepository;

    public List<CloResponse> getAllCourseOutcomes() {
        return cloRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CloResponse> getClosBySyllabus(UUID syllabusVersionId) {
        if (!syllabusRepository.existsById(syllabusVersionId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusVersionId);
        }
        return cloRepository.findBySyllabusVersionId(syllabusVersionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CloResponse getCloById(UUID id) {
        CLO clo = cloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CLO", "id", id));
        return mapToResponse(clo);
    }

    @Transactional
    public CloResponse createClo(CloRequest request) {
        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        if (cloRepository.findBySyllabusVersionIdAndCode(request.getSyllabusVersionId(), request.getCode()).isPresent()) {
            throw new BadRequestException("CLO code already exists for this syllabus");
        }

        CLO clo = CLO.builder()
                .syllabusVersion(syllabus)
                .code(request.getCode())
                .description(request.getDescription())
                .bloomLevel(request.getBloomLevel())
                .weight(request.getWeight() != null ? request.getWeight() : BigDecimal.ZERO)
                .build();

        CLO savedClo = cloRepository.save(clo);
        return mapToResponse(savedClo);
    }

    @Transactional
    public CloResponse updateClo(UUID id, CloRequest request) {
        CLO clo = cloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CLO", "id", id));

        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        if (!clo.getCode().equals(request.getCode()) 
                && cloRepository.findBySyllabusVersionIdAndCode(request.getSyllabusVersionId(), request.getCode()).isPresent()) {
            throw new BadRequestException("CLO code already exists for this syllabus");
        }

        clo.setSyllabusVersion(syllabus);
        clo.setCode(request.getCode());
        clo.setDescription(request.getDescription());
        clo.setBloomLevel(request.getBloomLevel());
        clo.setWeight(request.getWeight() != null ? request.getWeight() : BigDecimal.ZERO);

        CLO updatedClo = cloRepository.save(clo);
        return mapToResponse(updatedClo);
    }

    @Transactional
    public void deleteClo(UUID id) {
        if (!cloRepository.existsById(id)) {
            throw new ResourceNotFoundException("CLO", "id", id);
        }
        cloRepository.deleteById(id);
    }

    private CloResponse mapToResponse(CLO clo) {
        CloResponse response = new CloResponse();
        response.setId(clo.getId());
        response.setSyllabusVersionId(clo.getSyllabusVersion().getId());
        response.setCode(clo.getCode());
        response.setDescription(clo.getDescription());
        response.setBloomLevel(clo.getBloomLevel());
        response.setWeight(clo.getWeight());
        response.setCreatedAt(clo.getCreatedAt());
        response.setUpdatedAt(clo.getUpdatedAt());
        return response;
    }
}
