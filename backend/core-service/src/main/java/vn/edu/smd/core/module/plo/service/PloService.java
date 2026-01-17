package vn.edu.smd.core.module.plo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.PLO;
import vn.edu.smd.core.module.plo.dto.PloRequest;
import vn.edu.smd.core.module.plo.dto.PloResponse;
import vn.edu.smd.core.repository.SubjectRepository;
import vn.edu.smd.core.repository.PLORepository;
import vn.edu.smd.shared.enums.PloCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PloService {

    private final PLORepository ploRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<PloResponse> getAllPlos() {
        return ploRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PloResponse> getPlosBySubject(UUID subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject", "id", subjectId);
        }
        return ploRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PloResponse getPloById(UUID id) {
        PLO plo = ploRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", id));
        return mapToResponse(plo);
    }

    @Transactional
    public PloResponse createPlo(PloRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        // Check for duplicate PLO code within the same subject
        if (ploRepository.existsBySubjectIdAndCode(request.getSubjectId(), request.getCode())) {
            throw new BadRequestException("Mã PLO đã tồn tại trong môn học này.");
        }

        PLO plo = PLO.builder()
                .subject(subject)
                .code(request.getCode())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : PloCategory.KNOWLEDGE)
                .build();

        PLO savedPlo = ploRepository.save(plo);
        return mapToResponse(savedPlo);
    }

    @Transactional
    public PloResponse updatePlo(UUID id, PloRequest request) {
        PLO plo = ploRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PLO", "id", id));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        // Check if the new code is being used by another PLO in the same subject
        Optional<PLO> existingPloWithSameCode = ploRepository.findBySubjectIdAndCode(request.getSubjectId(), request.getCode());
        if (existingPloWithSameCode.isPresent() && !existingPloWithSameCode.get().getId().equals(id)) {
            throw new BadRequestException("Mã PLO đã tồn tại trong môn học này.");
        }

        plo.setSubject(subject);
        plo.setCode(request.getCode());
        plo.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            plo.setCategory(request.getCategory());
        }

        PLO updatedPlo = ploRepository.save(plo);
        return mapToResponse(updatedPlo);
    }

    @Transactional
    public void deletePlo(UUID id) {
        if (!ploRepository.existsById(id)) {
            throw new ResourceNotFoundException("PLO", "id", id);
        }
        ploRepository.deleteById(id);
    }

    private PloResponse mapToResponse(PLO plo) {
        PloResponse response = new PloResponse();
        response.setId(plo.getId());
        response.setSubjectId(plo.getSubject().getId());
        response.setSubjectCode(plo.getSubject().getCode());
        response.setSubjectName(plo.getSubject().getCurrentNameVi());
        response.setCode(plo.getCode());
        response.setDescription(plo.getDescription());
        response.setCategory(plo.getCategory());
        response.setCreatedAt(plo.getCreatedAt());
        response.setUpdatedAt(plo.getUpdatedAt());
        return response;
    }
}
