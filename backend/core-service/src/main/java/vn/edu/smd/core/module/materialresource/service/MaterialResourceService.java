package vn.edu.smd.core.module.materialresource.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.MaterialResource;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.materialresource.dto.MaterialResourceRequest;
import vn.edu.smd.core.module.materialresource.dto.MaterialResourceResponse;
import vn.edu.smd.core.repository.MaterialResourceRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialResourceService {

    private final MaterialResourceRepository materialResourceRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public List<MaterialResourceResponse> getAllMaterials() {
        return materialResourceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MaterialResourceResponse getMaterialById(UUID id) {
        MaterialResource material = materialResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialResource", "id", id));
        return mapToResponse(material);
    }

    @Transactional
    public MaterialResourceResponse createMaterial(MaterialResourceRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        MaterialResource material = MaterialResource.builder()
                .syllabusVersion(syllabusVersion)
                .resourceType(request.getResourceType())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .year(request.getYear())
                .url(request.getUrl())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .build();

        MaterialResource saved = materialResourceRepository.save(material);
        return mapToResponse(saved);
    }

    @Transactional
    public MaterialResourceResponse updateMaterial(UUID id, MaterialResourceRequest request) {
        MaterialResource material = materialResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialResource", "id", id));

        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        material.setSyllabusVersion(syllabusVersion);
        material.setResourceType(request.getResourceType());
        material.setTitle(request.getTitle());
        material.setAuthor(request.getAuthor());
        material.setPublisher(request.getPublisher());
        material.setYear(request.getYear());
        material.setUrl(request.getUrl());
        material.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);

        MaterialResource updated = materialResourceRepository.save(material);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMaterial(UUID id) {
        MaterialResource material = materialResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialResource", "id", id));
        materialResourceRepository.delete(material);
    }

    public List<MaterialResourceResponse> getMaterialsBySyllabusId(UUID syllabusId) {
        return materialResourceRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MaterialResourceResponse mapToResponse(MaterialResource material) {
        MaterialResourceResponse response = new MaterialResourceResponse();
        response.setId(material.getId());
        response.setSyllabusVersionId(material.getSyllabusVersion().getId());
        response.setResourceType(material.getResourceType());
        response.setTitle(material.getTitle());
        response.setAuthor(material.getAuthor());
        response.setPublisher(material.getPublisher());
        response.setYear(material.getYear());
        response.setUrl(material.getUrl());
        response.setIsRequired(material.getIsRequired());
        response.setCreatedAt(material.getCreatedAt());
        response.setUpdatedAt(material.getUpdatedAt());
        return response;
    }
}
