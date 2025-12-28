package vn.edu.smd.core.module.approval.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.ApprovalHistory;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.approval.dto.ApprovalRequest;
import vn.edu.smd.core.module.approval.dto.ApprovalResponse;
import vn.edu.smd.core.repository.ApprovalHistoryRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalHistoryRepository approvalRepository;
    private final SyllabusVersionRepository syllabusRepository;

    public Page<ApprovalResponse> getAllApprovals(Pageable pageable) {
        return approvalRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<ApprovalResponse> getApprovalsBySyllabus(UUID syllabusVersionId) {
        if (!syllabusRepository.existsById(syllabusVersionId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusVersionId);
        }
        return approvalRepository.findBySyllabusVersionIdOrderByCreatedAtDesc(syllabusVersionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ApprovalResponse> getApprovalHistoryOfSyllabus(UUID syllabusId) {
        if (!syllabusRepository.existsById(syllabusId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusId);
        }
        return approvalRepository.findBySyllabusVersionIdOrderByCreatedAtDesc(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ApprovalResponse> getPendingApprovalsForUser(UUID userId) {
        // Get all approvals where the user is the actor
        // Filter to include only pending approvals or use repository query
        return approvalRepository.findByActorId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ApprovalResponse getApprovalById(UUID id) {
        ApprovalHistory approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalHistory", "id", id));
        return mapToResponse(approval);
    }

    @Transactional
    public ApprovalResponse createApproval(ApprovalRequest request) {
        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        ApprovalHistory approval = ApprovalHistory.builder()
                .syllabusVersion(syllabus)
                .action(request.getAction())
                .comment(request.getComment())
                .build();

        ApprovalHistory savedApproval = approvalRepository.save(approval);
        return mapToResponse(savedApproval);
    }

    @Transactional
    public void deleteApproval(UUID id) {
        if (!approvalRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApprovalHistory", "id", id);
        }
        approvalRepository.deleteById(id);
    }

    private ApprovalResponse mapToResponse(ApprovalHistory approval) {
        ApprovalResponse response = new ApprovalResponse();
        response.setId(approval.getId());
        response.setSyllabusVersionId(approval.getSyllabusVersion().getId());
        if (approval.getActor() != null) {
            response.setActorId(approval.getActor().getId());
            response.setActorName(approval.getActor().getFullName());
        }
        response.setAction(approval.getAction());
        response.setComment(approval.getComment());
        response.setBatchId(approval.getBatchId());
        response.setStepNumber(approval.getStepNumber());
        response.setRoleCode(approval.getRoleCode());
        response.setActorRole(approval.getActorRole());
        response.setCreatedAt(approval.getCreatedAt());
        return response;
    }
}
