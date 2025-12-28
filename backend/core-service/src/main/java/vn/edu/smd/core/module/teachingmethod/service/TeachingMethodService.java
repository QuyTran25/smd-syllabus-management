package vn.edu.smd.core.module.teachingmethod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.TeachingMethod;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.teachingmethod.dto.TeachingMethodRequest;
import vn.edu.smd.core.module.teachingmethod.dto.TeachingMethodResponse;
import vn.edu.smd.core.repository.TeachingMethodRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingMethodService {

    private final TeachingMethodRepository teachingMethodRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public List<TeachingMethodResponse> getAllMethods() {
        return teachingMethodRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeachingMethodResponse getMethodById(UUID id) {
        TeachingMethod method = teachingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeachingMethod", "id", id));
        return mapToResponse(method);
    }

    @Transactional
    public TeachingMethodResponse createMethod(TeachingMethodRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        TeachingMethod method = TeachingMethod.builder()
                .syllabusVersion(syllabusVersion)
                .methodName(request.getMethodName())
                .description(request.getDescription())
                .percentage(request.getPercentage())
                .build();

        TeachingMethod saved = teachingMethodRepository.save(method);
        return mapToResponse(saved);
    }

    @Transactional
    public TeachingMethodResponse updateMethod(UUID id, TeachingMethodRequest request) {
        TeachingMethod method = teachingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeachingMethod", "id", id));

        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        method.setSyllabusVersion(syllabusVersion);
        method.setMethodName(request.getMethodName());
        method.setDescription(request.getDescription());
        method.setPercentage(request.getPercentage());

        TeachingMethod updated = teachingMethodRepository.save(method);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMethod(UUID id) {
        TeachingMethod method = teachingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeachingMethod", "id", id));
        teachingMethodRepository.delete(method);
    }

    public List<TeachingMethodResponse> getMethodsBySyllabusId(UUID syllabusId) {
        return teachingMethodRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TeachingMethodResponse mapToResponse(TeachingMethod method) {
        TeachingMethodResponse response = new TeachingMethodResponse();
        response.setId(method.getId());
        response.setSyllabusVersionId(method.getSyllabusVersion().getId());
        response.setMethodName(method.getMethodName());
        response.setDescription(method.getDescription());
        response.setPercentage(method.getPercentage());
        response.setCreatedAt(method.getCreatedAt());
        response.setUpdatedAt(method.getUpdatedAt());
        return response;
    }
}
