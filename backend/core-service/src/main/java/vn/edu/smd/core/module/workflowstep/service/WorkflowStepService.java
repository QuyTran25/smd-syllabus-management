package vn.edu.smd.core.module.workflowstep.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.WorkflowStep;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepListRequest;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepRequest;
import vn.edu.smd.core.module.workflowstep.dto.WorkflowStepResponse;
import vn.edu.smd.core.repository.WorkflowStepRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowStepService {

    private final WorkflowStepRepository workflowStepRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final UserRepository userRepository;

    public Page<WorkflowStepResponse> getAllWorkflowSteps(WorkflowStepListRequest request) {
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<WorkflowStep> workflowSteps = workflowStepRepository.findAll(pageable);
        return workflowSteps.map(this::mapToResponse);
    }

    public WorkflowStepResponse getWorkflowStepById(UUID id) {
        WorkflowStep workflowStep = workflowStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", "id", id));
        return mapToResponse(workflowStep);
    }

    public List<WorkflowStepResponse> getWorkflowBySyllabus(UUID syllabusId) {
        if (!syllabusVersionRepository.existsById(syllabusId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusId);
        }
        return workflowStepRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkflowStepResponse createWorkflowStep(WorkflowStepRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        User approver = null;
        if (request.getApproverId() != null) {
            approver = userRepository.findById(request.getApproverId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getApproverId()));
        }

        WorkflowStep workflowStep = WorkflowStep.builder()
                .syllabusVersion(syllabusVersion)
                .stepOrder(request.getStepOrder())
                .stepName(request.getStepName())
                .approverRole(request.getApproverRole())
                .approver(approver)
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .comments(request.getComments())
                .build();

        WorkflowStep savedWorkflowStep = workflowStepRepository.save(workflowStep);
        return mapToResponse(savedWorkflowStep);
    }

    @Transactional
    public WorkflowStepResponse updateWorkflowStep(UUID id, WorkflowStepRequest request) {
        WorkflowStep workflowStep = workflowStepRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", "id", id));

        workflowStep.setStepOrder(request.getStepOrder());
        workflowStep.setStepName(request.getStepName());
        workflowStep.setApproverRole(request.getApproverRole());
        
        if (request.getApproverId() != null) {
            User approver = userRepository.findById(request.getApproverId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getApproverId()));
            workflowStep.setApprover(approver);
        }

        if (request.getStatus() != null) {
            workflowStep.setStatus(request.getStatus());
            if ("APPROVED".equals(request.getStatus()) || "REJECTED".equals(request.getStatus())) {
                workflowStep.setCompletedAt(LocalDateTime.now());
            }
        }
        
        workflowStep.setComments(request.getComments());

        WorkflowStep updatedWorkflowStep = workflowStepRepository.save(workflowStep);
        return mapToResponse(updatedWorkflowStep);
    }

    @Transactional
    public void deleteWorkflowStep(UUID id) {
        if (!workflowStepRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkflowStep", "id", id);
        }
        workflowStepRepository.deleteById(id);
    }

    private WorkflowStepResponse mapToResponse(WorkflowStep workflowStep) {
        WorkflowStepResponse response = new WorkflowStepResponse();
        response.setId(workflowStep.getId());
        response.setSyllabusVersionId(workflowStep.getSyllabusVersion().getId());
        response.setStepOrder(workflowStep.getStepOrder());
        response.setStepName(workflowStep.getStepName());
        response.setApproverRole(workflowStep.getApproverRole());
        
        if (workflowStep.getApprover() != null) {
            response.setApproverId(workflowStep.getApprover().getId());
            response.setApproverName(workflowStep.getApprover().getFullName());
        }
        
        response.setStatus(workflowStep.getStatus());
        response.setComments(workflowStep.getComments());
        response.setCompletedAt(workflowStep.getCompletedAt());
        response.setCreatedAt(workflowStep.getCreatedAt());
        response.setUpdatedAt(workflowStep.getUpdatedAt());
        
        return response;
    }
}
