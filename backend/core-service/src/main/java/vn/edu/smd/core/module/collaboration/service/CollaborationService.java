package vn.edu.smd.core.module.collaboration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.SyllabusCollaborator;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.collaboration.dto.CollaborationRequest;
import vn.edu.smd.core.module.collaboration.dto.CollaborationResponse;
import vn.edu.smd.core.repository.SyllabusCollaboratorRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final SyllabusCollaboratorRepository collaboratorRepository;
    private final SyllabusVersionRepository syllabusRepository;
    private final UserRepository userRepository;

    public List<CollaborationResponse> getAllCollaborationSessions() {
        return collaboratorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CollaborationResponse> getCollaboratorsBySyllabus(UUID syllabusVersionId) {
        if (!syllabusRepository.existsById(syllabusVersionId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusVersionId);
        }
        return collaboratorRepository.findBySyllabusVersionId(syllabusVersionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CollaborationResponse getCollaborationById(UUID id) {
        SyllabusCollaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration", "id", id));
        return mapToResponse(collaborator);
    }

    @Transactional
    public CollaborationResponse createCollaboration(CollaborationRequest request) {
        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (collaboratorRepository.findBySyllabusVersionIdAndUserId(request.getSyllabusVersionId(), request.getUserId()).isPresent()) {
            throw new BadRequestException("User is already a collaborator for this syllabus");
        }

        SyllabusCollaborator collaborator = SyllabusCollaborator.builder()
                .syllabusVersion(syllabus)
                .user(user)
                .role(request.getRole())
                .build();

        SyllabusCollaborator savedCollaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(savedCollaborator);
    }

    @Transactional
    public CollaborationResponse updateCollaboration(UUID id, CollaborationRequest request) {
        SyllabusCollaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration", "id", id));

        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        collaborator.setSyllabusVersion(syllabus);
        collaborator.setUser(user);
        collaborator.setRole(request.getRole());

        SyllabusCollaborator updatedCollaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(updatedCollaborator);
    }

    @Transactional
    public void deleteCollaboration(UUID id) {
        if (!collaboratorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Collaboration", "id", id);
        }
        collaboratorRepository.deleteById(id);
    }

    @Transactional
    public CollaborationResponse closeCollaboration(UUID id) {
        SyllabusCollaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration", "id", id));
        
        // Mark collaboration as closed by removing it
        collaboratorRepository.delete(collaborator);
        return mapToResponse(collaborator);
    }

    private CollaborationResponse mapToResponse(SyllabusCollaborator collaborator) {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(collaborator.getId());
        response.setSyllabusVersionId(collaborator.getSyllabusVersion().getId());
        response.setUserId(collaborator.getUser().getId());
        response.setUserName(collaborator.getUser().getFullName());
        response.setUserEmail(collaborator.getUser().getEmail());
        response.setRole(collaborator.getRole());
        response.setAssignedAt(collaborator.getAssignedAt());
        return response;
    }
}
