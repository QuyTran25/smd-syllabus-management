package vn.edu.smd.core.module.plo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Curriculum;
import vn.edu.smd.core.entity.PLO;
import vn.edu.smd.core.module.plo.dto.PloRequest;
import vn.edu.smd.core.module.plo.dto.PloResponse;
import vn.edu.smd.core.repository.CurriculumRepository;
import vn.edu.smd.core.repository.PLORepository;
import vn.edu.smd.shared.enums.PloCategory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PloService {

    private final PLORepository ploRepository;
    private final CurriculumRepository curriculumRepository;

    @Transactional(readOnly = true)
    public List<PloResponse> getAllPlos() {
        return ploRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PloResponse> getClosByCurriculum(UUID curriculumId) {
        if (!curriculumRepository.existsById(curriculumId)) {
            throw new ResourceNotFoundException("Curriculum", "id", curriculumId);
        }
        return ploRepository.findByCurriculumId(curriculumId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PloResponse getPloById(UUID id) {
        PLO plo = ploRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", id));
        return mapToResponse(plo);
    }

    @Transactional
    public PloResponse createPlo(PloRequest request) {
        Curriculum curriculum = curriculumRepository.findById(request.getCurriculumId())
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", request.getCurriculumId()));

        PLO plo = PLO.builder()
                .curriculum(curriculum)
                .code(request.getCode())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : PloCategory.KNOWLEDGE)
                .build();

        PLO savedPlo = ploRepository.save(plo);
        return mapToResponse(savedPlo);
    }

    @Transactional
    public PloResponse updatePlo(UUID id, PloRequest request) {
        PLO plo = ploRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", id));

        Curriculum curriculum = curriculumRepository.findById(request.getCurriculumId())
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", request.getCurriculumId()));

        plo.setCurriculum(curriculum);
        plo.setCode(request.getCode());
        plo.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            plo.setCategory(request.getCategory());
        }

        PLO updatedPlo = ploRepository.save(plo);
        return mapToResponse(updatedPlo);
    }

    @Transactional
    public void deletePlo(UUID id) {
        if (!ploRepository.existsById(id)) {
            throw new ResourceNotFoundException("PLO", "id", id);
        }
        ploRepository.deleteById(id);
    }

    private PloResponse mapToResponse(PLO plo) {
        PloResponse response = new PloResponse();
        response.setId(plo.getId());
        response.setCurriculumId(plo.getCurriculum().getId());
        response.setCurriculumCode(plo.getCurriculum().getCode());
        response.setCurriculumName(plo.getCurriculum().getName());
        response.setCode(plo.getCode());
        response.setDescription(plo.getDescription());
        response.setCategory(plo.getCategory());
        response.setCreatedAt(plo.getCreatedAt());
        response.setUpdatedAt(plo.getUpdatedAt());
        return response;
    }
}
