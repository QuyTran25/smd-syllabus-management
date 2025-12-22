package vn.edu.smd.core.module.assessment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AssessmentScheme;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.assessment.dto.AssessmentRequest;
import vn.edu.smd.core.module.assessment.dto.AssessmentResponse;
import vn.edu.smd.core.repository.AssessmentSchemeRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentSchemeRepository assessmentRepository;
    private final SyllabusVersionRepository syllabusRepository;

    public List<AssessmentResponse> getAssessmentsBySyllabus(UUID syllabusVersionId) {
        if (!syllabusRepository.existsById(syllabusVersionId)) {
            throw new ResourceNotFoundException("SyllabusVersion", "id", syllabusVersionId);
        }
        return assessmentRepository.findBySyllabusVersionId(syllabusVersionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AssessmentResponse getAssessmentById(UUID id) {
        AssessmentScheme assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
        return mapToResponse(assessment);
    }

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        AssessmentScheme parent = null;
        if (request.getParentId() != null) {
            parent = assessmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", request.getParentId()));
        }

        AssessmentScheme assessment = AssessmentScheme.builder()
                .syllabusVersion(syllabus)
                .parent(parent)
                .name(request.getName())
                .weightPercent(request.getWeightPercent())
                .build();

        AssessmentScheme savedAssessment = assessmentRepository.save(assessment);
        return mapToResponse(savedAssessment);
    }

    @Transactional
    public AssessmentResponse updateAssessment(UUID id, AssessmentRequest request) {
        AssessmentScheme assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));

        SyllabusVersion syllabus = syllabusRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        AssessmentScheme parent = null;
        if (request.getParentId() != null) {
            parent = assessmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", request.getParentId()));
        }

        assessment.setSyllabusVersion(syllabus);
        assessment.setParent(parent);
        assessment.setName(request.getName());
        assessment.setWeightPercent(request.getWeightPercent());

        AssessmentScheme updatedAssessment = assessmentRepository.save(assessment);
        return mapToResponse(updatedAssessment);
    }

    @Transactional
    public void deleteAssessment(UUID id) {
        if (!assessmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assessment", "id", id);
        }
        assessmentRepository.deleteById(id);
    }

    public Boolean validateAssessmentWeight(UUID id) {
        AssessmentScheme assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
        
        // Get all assessments for the same syllabus
        List<AssessmentScheme> allAssessments = assessmentRepository
                .findBySyllabusVersionId(assessment.getSyllabusVersion().getId());
        
        // Calculate total weight for top-level assessments (those without parent)
        BigDecimal totalWeight = allAssessments.stream()
                .filter(a -> a.getParent() == null)
                .map(AssessmentScheme::getWeightPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Check if total weight equals 100%
        return totalWeight.compareTo(new BigDecimal("100.00")) == 0;
    }

    private AssessmentResponse mapToResponse(AssessmentScheme assessment) {
        AssessmentResponse response = new AssessmentResponse();
        response.setId(assessment.getId());
        response.setSyllabusVersionId(assessment.getSyllabusVersion().getId());
        if (assessment.getParent() != null) {
            response.setParentId(assessment.getParent().getId());
            response.setParentName(assessment.getParent().getName());
        }
        response.setName(assessment.getName());
        response.setWeightPercent(assessment.getWeightPercent());
        response.setCreatedAt(assessment.getCreatedAt());
        response.setUpdatedAt(assessment.getUpdatedAt());
        return response;
    }
}
