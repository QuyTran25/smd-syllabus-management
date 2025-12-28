package vn.edu.smd.core.module.lessonplan.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.LessonPlan;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.module.lessonplan.dto.LessonPlanRequest;
import vn.edu.smd.core.module.lessonplan.dto.LessonPlanResponse;
import vn.edu.smd.core.repository.LessonPlanRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonPlanService {

    private final LessonPlanRepository lessonPlanRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;

    public List<LessonPlanResponse> getAllPlans() {
        return lessonPlanRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LessonPlanResponse getPlanById(UUID id) {
        LessonPlan plan = lessonPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonPlan", "id", id));
        return mapToResponse(plan);
    }

    @Transactional
    public LessonPlanResponse createPlan(LessonPlanRequest request) {
        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        LessonPlan plan = LessonPlan.builder()
                .syllabusVersion(syllabusVersion)
                .weekNumber(request.getWeekNumber())
                .topic(request.getTopic())
                .objectives(request.getObjectives())
                .teachingMethod(request.getTeachingMethod())
                .assessmentMethod(request.getAssessmentMethod())
                .build();

        LessonPlan saved = lessonPlanRepository.save(plan);
        return mapToResponse(saved);
    }

    @Transactional
    public LessonPlanResponse updatePlan(UUID id, LessonPlanRequest request) {
        LessonPlan plan = lessonPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonPlan", "id", id));

        SyllabusVersion syllabusVersion = syllabusVersionRepository.findById(request.getSyllabusVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusVersion", "id", request.getSyllabusVersionId()));

        plan.setSyllabusVersion(syllabusVersion);
        plan.setWeekNumber(request.getWeekNumber());
        plan.setTopic(request.getTopic());
        plan.setObjectives(request.getObjectives());
        plan.setTeachingMethod(request.getTeachingMethod());
        plan.setAssessmentMethod(request.getAssessmentMethod());

        LessonPlan updated = lessonPlanRepository.save(plan);
        return mapToResponse(updated);
    }

    @Transactional
    public void deletePlan(UUID id) {
        LessonPlan plan = lessonPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonPlan", "id", id));
        lessonPlanRepository.delete(plan);
    }

    public List<LessonPlanResponse> getPlansBySyllabusId(UUID syllabusId) {
        return lessonPlanRepository.findBySyllabusVersionId(syllabusId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LessonPlanResponse mapToResponse(LessonPlan plan) {
        LessonPlanResponse response = new LessonPlanResponse();
        response.setId(plan.getId());
        response.setSyllabusVersionId(plan.getSyllabusVersion().getId());
        response.setWeekNumber(plan.getWeekNumber());
        response.setTopic(plan.getTopic());
        response.setObjectives(plan.getObjectives());
        response.setTeachingMethod(plan.getTeachingMethod());
        response.setAssessmentMethod(plan.getAssessmentMethod());
        response.setCreatedAt(plan.getCreatedAt());
        response.setUpdatedAt(plan.getUpdatedAt());
        return response;
    }
}
