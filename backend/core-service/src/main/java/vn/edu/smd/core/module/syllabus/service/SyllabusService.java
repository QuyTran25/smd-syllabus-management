package vn.edu.smd.core.module.syllabus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.SyllabusStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusVersionRepository syllabusVersionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicTermRepository academicTermRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        if (statusStrings != null && !statusStrings.isEmpty()) {
            List<SyllabusStatus> statuses = statusStrings.stream()
                    .map(s -> SyllabusStatus.valueOf(s.toUpperCase()))
                    .toList();
            return syllabusVersionRepository.findByStatusInAndIsDeletedFalse(statuses, pageable)
                    .map(this::mapToResponse);
        }
        return syllabusVersionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusById(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        return mapToResponse(syllabus);
    }

    @Transactional
    public SyllabusResponse createSyllabus(SyllabusRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        AcademicTerm academicTerm = null;
        if (request.getAcademicTermId() != null) {
            academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));
        }

        User currentUser = getCurrentUser();

        SyllabusVersion syllabus = SyllabusVersion.builder()
                .subject(subject)
                .academicTerm(academicTerm)
                .versionNo(request.getVersionNo())
                .status(SyllabusStatus.DRAFT)
                .reviewDeadline(request.getReviewDeadline())
                .effectiveDate(request.getEffectiveDate())
                .keywords(request.getKeywords())
                .content(request.getContent())
                .description(request.getDescription())
                .snapSubjectCode(subject.getCode())
                .snapSubjectNameVi(subject.getCurrentNameVi())
                .snapSubjectNameEn(subject.getCurrentNameEn())
                .snapCreditCount(subject.getDefaultCredits())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .isDeleted(false)
                .build();

        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse updateSyllabus(UUID id, SyllabusRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be updated");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        syllabus.setSubject(subject);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());
        syllabus.setDescription(request.getDescription());
        syllabus.setUpdatedBy(getCurrentUser());

        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public void deleteSyllabus(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be deleted");
        }
        syllabus.setIsDeleted(true);
        syllabusVersionRepository.save(syllabus);
    }

    // --- CÁC HÀM FIX LỖI CONTROLLER ---

    @Transactional
    public SyllabusResponse submitSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be submitted");
        }
        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setUpdatedBy(getCurrentUser());
        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse approveSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        SyllabusStatus nextStatus = switch (syllabus.getStatus()) {
            case PENDING_HOD -> SyllabusStatus.PENDING_AA;
            case PENDING_AA -> SyllabusStatus.PENDING_PRINCIPAL;
            case PENDING_PRINCIPAL -> SyllabusStatus.APPROVED;
            case APPROVED -> SyllabusStatus.PUBLISHED;
            default -> throw new BadRequestException("Cannot approve in current status: " + syllabus.getStatus());
        };

        syllabus.setStatus(nextStatus);
        syllabus.setUpdatedBy(getCurrentUser());
        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse rejectSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        syllabus.setStatus(SyllabusStatus.REJECTED);
        syllabus.setUnpublishReason(request.getComment());
        syllabus.setUpdatedBy(getCurrentUser());
        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabusVersions(UUID id) {
        SyllabusVersion current = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        List<SyllabusVersion> versions = new ArrayList<>();
        while (current != null) {
            versions.add(current);
            current = current.getPreviousVersion();
        }
        return versions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SyllabusCompareResponse compareSyllabi(UUID id1, UUID id2) {
        SyllabusVersion s1 = syllabusVersionRepository.findById(id1)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id1));
        SyllabusVersion s2 = syllabusVersionRepository.findById(id2)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id2));

        SyllabusCompareResponse response = new SyllabusCompareResponse();
        response.setSyllabusA(mapToResponse(s1));
        response.setSyllabusB(mapToResponse(s2));
        // Logic so sánh đơn giản
        return response;
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        return syllabusVersionRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportSyllabusToPdf(UUID id) {
        throw new BadRequestException("PDF Export not implemented yet");
    }

    @Transactional
    public SyllabusResponse cloneSyllabus(UUID id) {
        SyllabusVersion original = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        SyllabusVersion cloned = SyllabusVersion.builder()
                .subject(original.getSubject())
                .versionNo(original.getVersionNo() + "_CLONE")
                .status(SyllabusStatus.DRAFT)
                .content(original.getContent())
                .createdBy(getCurrentUser())
                .isDeleted(false)
                .build();
        return mapToResponse(syllabusVersionRepository.save(cloned));
    }

    // --- HELPERS ---
    private SyllabusResponse mapToResponse(SyllabusVersion s) {
        SyllabusResponse r = new SyllabusResponse();
        r.setId(s.getId());
        r.setSubjectId(s.getSubject().getId());
        r.setSubjectCode(s.getSnapSubjectCode());
        r.setSubjectNameVi(s.getSnapSubjectNameVi());
        r.setVersionNo(s.getVersionNo());
        r.setStatus(s.getStatus().name());
        r.setContent(s.getContent());
        if (s.getCreatedBy() != null) r.setOwnerName(s.getCreatedBy().getFullName());
        return r;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }
}