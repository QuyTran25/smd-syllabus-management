package vn.edu.smd.core.module.performanceindicator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.PLO;
import vn.edu.smd.core.entity.PerformanceIndicator;
import vn.edu.smd.core.module.performanceindicator.dto.PerformanceIndicatorRequest;
import vn.edu.smd.core.module.performanceindicator.dto.PerformanceIndicatorResponse;
import vn.edu.smd.core.repository.PLORepository;
import vn.edu.smd.core.repository.PerformanceIndicatorRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceIndicatorService {

    private final PerformanceIndicatorRepository piRepository;
    private final PLORepository ploRepository;

    public List<PerformanceIndicatorResponse> getPisByPlo(UUID ploId) {
        if (!ploRepository.existsById(ploId)) {
            throw new ResourceNotFoundException("PLO", "id", ploId);
        }
        return piRepository.findByPloId(ploId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PerformanceIndicatorResponse getPiById(UUID id) {
        PerformanceIndicator pi = piRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceIndicator", "id", id));
        return mapToResponse(pi);
    }

    @Transactional
    public PerformanceIndicatorResponse createPi(PerformanceIndicatorRequest request) {
        PLO plo = ploRepository.findById(request.getPloId())
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", request.getPloId()));

        PerformanceIndicator pi = PerformanceIndicator.builder()
                .plo(plo)
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        PerformanceIndicator savedPi = piRepository.save(pi);
        return mapToResponse(savedPi);
    }

    @Transactional
    public PerformanceIndicatorResponse updatePi(UUID id, PerformanceIndicatorRequest request) {
        PerformanceIndicator pi = piRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceIndicator", "id", id));

        PLO plo = ploRepository.findById(request.getPloId())
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", request.getPloId()));

        pi.setPlo(plo);
        pi.setCode(request.getCode());
        pi.setDescription(request.getDescription());

        PerformanceIndicator updatedPi = piRepository.save(pi);
        return mapToResponse(updatedPi);
    }

    @Transactional
    public void deletePi(UUID id) {
        if (!piRepository.existsById(id)) {
            throw new ResourceNotFoundException("PerformanceIndicator", "id", id);
        }
        piRepository.deleteById(id);
    }

    private PerformanceIndicatorResponse mapToResponse(PerformanceIndicator pi) {
        PerformanceIndicatorResponse response = new PerformanceIndicatorResponse();
        response.setId(pi.getId());
        response.setPloId(pi.getPlo().getId());
        response.setPloCode(pi.getPlo().getCode());
        response.setCode(pi.getCode());
        response.setDescription(pi.getDescription());
        response.setCreatedAt(pi.getCreatedAt());
        response.setUpdatedAt(pi.getUpdatedAt());
        return response;
    }
}
