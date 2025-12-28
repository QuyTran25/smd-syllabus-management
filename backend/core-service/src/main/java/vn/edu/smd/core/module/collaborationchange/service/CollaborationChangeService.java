package vn.edu.smd.core.module.collaborationchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.CollaborationChange;
import vn.edu.smd.core.entity.SyllabusCollaborator;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeListRequest;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeRequest;
import vn.edu.smd.core.module.collaborationchange.dto.CollaborationChangeResponse;
import vn.edu.smd.core.repository.CollaborationChangeRepository;
import vn.edu.smd.core.repository.SyllabusCollaboratorRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborationChangeService {

    private final CollaborationChangeRepository collaborationChangeRepository;
    private final SyllabusCollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;

    public Page<CollaborationChangeResponse> getAllCollaborationChanges(CollaborationChangeListRequest request) {
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<CollaborationChange> changes = collaborationChangeRepository.findAll(pageable);
        return changes.map(this::mapToResponse);
    }

    public CollaborationChangeResponse getCollaborationChangeById(UUID id) {
        CollaborationChange change = collaborationChangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollaborationChange", "id", id));
        return mapToResponse(change);
    }

    public List<CollaborationChangeResponse> getChangesByCollaborationSession(UUID sessionId) {
        if (!collaboratorRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("SyllabusCollaborator", "id", sessionId);
        }
        return collaborationChangeRepository.findByCollaborationSessionId(sessionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CollaborationChangeResponse createCollaborationChange(CollaborationChangeRequest request) {
        SyllabusCollaborator collaborationSession = collaboratorRepository.findById(request.getCollaborationSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusCollaborator", "id", request.getCollaborationSessionId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        CollaborationChange change = CollaborationChange.builder()
                .collaborationSession(collaborationSession)
                .user(user)
                .fieldName(request.getFieldName())
                .oldValue(request.getOldValue())
                .newValue(request.getNewValue())
                .changeType(request.getChangeType() != null ? request.getChangeType() : "MODIFY")
                .build();

        CollaborationChange savedChange = collaborationChangeRepository.save(change);
        return mapToResponse(savedChange);
    }

    private CollaborationChangeResponse mapToResponse(CollaborationChange change) {
        CollaborationChangeResponse response = new CollaborationChangeResponse();
        response.setId(change.getId());
        response.setCollaborationSessionId(change.getCollaborationSession().getId());
        response.setUserId(change.getUser().getId());
        response.setUserName(change.getUser().getFullName());
        response.setFieldName(change.getFieldName());
        response.setOldValue(change.getOldValue());
        response.setNewValue(change.getNewValue());
        response.setChangeType(change.getChangeType());
        response.setCreatedAt(change.getCreatedAt());
        
        return response;
    }
}
