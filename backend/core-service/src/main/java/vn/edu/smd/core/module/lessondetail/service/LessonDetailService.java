package vn.edu.smd.core.module.lessondetail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.LessonDetail;
import vn.edu.smd.core.entity.LessonPlan;
import vn.edu.smd.core.module.lessondetail.dto.LessonDetailRequest;
import vn.edu.smd.core.module.lessondetail.dto.LessonDetailResponse;
import vn.edu.smd.core.repository.LessonDetailRepository;
import vn.edu.smd.core.repository.LessonPlanRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonDetailService {

    private final LessonDetailRepository lessonDetailRepository;
    private final LessonPlanRepository lessonPlanRepository;

    public List<LessonDetailResponse> getAllDetails() {
        return lessonDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LessonDetailResponse getDetailById(UUID id) {
        LessonDetail detail = lessonDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonDetail", "id", id));
        return mapToResponse(detail);
    }

    @Transactional
    public LessonDetailResponse createDetail(LessonDetailRequest request) {
        LessonPlan lessonPlan = lessonPlanRepository.findById(request.getLessonPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("LessonPlan", "id", request.getLessonPlanId()));

        LessonDetail detail = LessonDetail.builder()
                .lessonPlan(lessonPlan)
                .sessionNumber(request.getSessionNumber())
                .content(request.getContent())
                .activity(request.getActivity())
                .durationMinutes(request.getDurationMinutes())
                .materials(request.getMaterials())
                .build();

        LessonDetail saved = lessonDetailRepository.save(detail);
        return mapToResponse(saved);
    }

    @Transactional
    public LessonDetailResponse updateDetail(UUID id, LessonDetailRequest request) {
        LessonDetail detail = lessonDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonDetail", "id", id));

        LessonPlan lessonPlan = lessonPlanRepository.findById(request.getLessonPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("LessonPlan", "id", request.getLessonPlanId()));

        detail.setLessonPlan(lessonPlan);
        detail.setSessionNumber(request.getSessionNumber());
        detail.setContent(request.getContent());
        detail.setActivity(request.getActivity());
        detail.setDurationMinutes(request.getDurationMinutes());
        detail.setMaterials(request.getMaterials());

        LessonDetail updated = lessonDetailRepository.save(detail);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDetail(UUID id) {
        LessonDetail detail = lessonDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LessonDetail", "id", id));
        lessonDetailRepository.delete(detail);
    }

    public List<LessonDetailResponse> getDetailsByPlanId(UUID planId) {
        return lessonDetailRepository.findByLessonPlanId(planId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LessonDetailResponse mapToResponse(LessonDetail detail) {
        LessonDetailResponse response = new LessonDetailResponse();
        response.setId(detail.getId());
        response.setLessonPlanId(detail.getLessonPlan().getId());
        response.setSessionNumber(detail.getSessionNumber());
        response.setContent(detail.getContent());
        response.setActivity(detail.getActivity());
        response.setDurationMinutes(detail.getDurationMinutes());
        response.setMaterials(detail.getMaterials());
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());
        return response;
    }
}
