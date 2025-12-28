package vn.edu.smd.core.module.prerequisite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.SubjectRelationship;
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteRequest;
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse;
import vn.edu.smd.core.repository.SubjectRelationshipRepository;
import vn.edu.smd.core.repository.SubjectRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrerequisiteService {

    private final SubjectRelationshipRepository relationshipRepository;
    private final SubjectRepository subjectRepository;

    public Page<PrerequisiteResponse> getAllPrerequisites(Pageable pageable) {
        return relationshipRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<PrerequisiteResponse> getPrerequisitesBySubject(UUID subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject", "id", subjectId);
        }
        return relationshipRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrerequisiteResponse createPrerequisite(PrerequisiteRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Subject relatedSubject = subjectRepository.findById(request.getRelatedSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Related Subject", "id", request.getRelatedSubjectId()));

        if (request.getSubjectId().equals(request.getRelatedSubjectId())) {
            throw new BadRequestException("Subject cannot have relationship with itself");
        }

        // Check if relationship already exists
        if (relationshipRepository.findBySubjectIdAndRelatedSubjectIdAndType(
                request.getSubjectId(), request.getRelatedSubjectId(), request.getType()).isPresent()) {
            throw new BadRequestException("Relationship already exists");
        }

        SubjectRelationship relationship = SubjectRelationship.builder()
                .subject(subject)
                .relatedSubject(relatedSubject)
                .type(request.getType())
                .build();

        SubjectRelationship savedRelationship = relationshipRepository.save(relationship);
        return mapToResponse(savedRelationship);
    }

    @Transactional
    public void deletePrerequisite(UUID id) {
        if (!relationshipRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prerequisite", "id", id);
        }
        relationshipRepository.deleteById(id);
    }

    private PrerequisiteResponse mapToResponse(SubjectRelationship relationship) {
        PrerequisiteResponse response = new PrerequisiteResponse();
        response.setId(relationship.getId());
        response.setSubjectId(relationship.getSubject().getId());
        response.setSubjectCode(relationship.getSubject().getCode());
        response.setSubjectName(relationship.getSubject().getCurrentNameVi());
        response.setRelatedSubjectId(relationship.getRelatedSubject().getId());
        response.setRelatedSubjectCode(relationship.getRelatedSubject().getCode());
        response.setRelatedSubjectName(relationship.getRelatedSubject().getCurrentNameVi());
        response.setType(relationship.getType());
        response.setCreatedAt(relationship.getCreatedAt());
        return response;
    }
}
