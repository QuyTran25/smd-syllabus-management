package vn.edu.smd.core.module.subjectcomponent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.SubjectComponent;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.subjectcomponent.dto.SubjectComponentRequest;
import vn.edu.smd.core.module.subjectcomponent.dto.SubjectComponentResponse;
import vn.edu.smd.core.repository.SubjectComponentRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectComponentService {

    private final SubjectComponentRepository subjectComponentRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public List<SubjectComponentResponse> getAllComponents() {
        return subjectComponentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SubjectComponentResponse getComponentById(UUID id) {
        SubjectComponent component = subjectComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubjectComponent", "id", id));
        return mapToResponse(component);
    }

    @Transactional
    public SubjectComponentResponse createComponent(SubjectComponentRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        SubjectComponent component = SubjectComponent.builder()
                .syllabusVersion(syllabusVersion)
                .componentType(request.getComponentType())
                .hours(request.getHours())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();

        SubjectComponent saved = subjectComponentRepository.save(component);
        return mapToResponse(saved);
    }

    @Transactional
    public SubjectComponentResponse updateComponent(UUID id, SubjectComponentRequest request) {
        SubjectComponent component = subjectComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubjectComponent", "id", id));

        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        component.setSyllabusVersion(syllabusVersion);
        component.setComponentType(request.getComponentType());
        component.setHours(request.getHours());
        component.setDescription(request.getDescription());
        component.setDisplayOrder(request.getDisplayOrder());

        SubjectComponent updated = subjectComponentRepository.save(component);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteComponent(UUID id) {
        SubjectComponent component = subjectComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubjectComponent", "id", id));
        subjectComponentRepository.delete(component);
    }

    public List<SubjectComponentResponse> getComponentsBySyllabusId(UUID syllabusId) {
        return subjectComponentRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SubjectComponentResponse mapToResponse(SubjectComponent component) {
        SubjectComponentResponse response = new SubjectComponentResponse();
        response.setId(component.getId());
        response.setSyllabusVersionId(component.getSyllabusVersion().getId());
        response.setComponentType(component.getComponentType());
        response.setHours(component.getHours());
        response.setDescription(component.getDescription());
        response.setDisplayOrder(component.getDisplayOrder());
        response.setCreatedAt(component.getCreatedAt());
        response.setUpdatedAt(component.getUpdatedAt());
        return response;
    }
}
