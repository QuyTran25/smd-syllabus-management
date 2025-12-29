package vn.edu.smd.core.module.syllabus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusVersionRepository syllabusVersionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        User currentUser = getCurrentUser();
        
        // Nếu không có status từ frontend, lấy mặc định theo vai trò (Role-based filtering)
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }

        if (statusStrings != null && !statusStrings.isEmpty()) {
            String[] statusArray = statusStrings.toArray(new String[0]);
            return syllabusVersionRepository.findByStatusInWithPage(statusArray, pageable)
                    .map(this::mapToResponse);
        }

        return syllabusVersionRepository.findAll(pageable).map(this::mapToResponse);
    }

    private List<String> getDefaultStatusByRole(User user) {
        if (user == null || user.getRoles() == null) return List.of();
        String primaryRole = user.getRoles().stream().findFirst().map(Role::getCode).orElse("");

        return switch (primaryRole) {
            case "PRINCIPAL" -> List.of(SyllabusStatus.PENDING_PRINCIPAL.name(), SyllabusStatus.APPROVED.name());
            case "AA" -> List.of(SyllabusStatus.PENDING_AA.name());
            case "HOD" -> List.of(SyllabusStatus.PENDING_HOD.name());
            default -> List.of(SyllabusStatus.DRAFT.name(), SyllabusStatus.PUBLISHED.name());
        };
    }

    @Transactional
    public SyllabusResponse createSyllabus(SyllabusRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        User currentUser = getCurrentUser();
        SyllabusVersion syllabus = new SyllabusVersion();
        
        syllabus.setSubject(subject);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setStudentTasks(request.getStudentTasks()); // Lưu trường quan trọng của bạn
        syllabus.setStatus(SyllabusStatus.DRAFT);
        
        // Lưu trữ Snapshot tại thời điểm tạo
        syllabus.setSnapSubjectCode(subject.getCode());
        syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
        syllabus.setSnapCreditCount(subject.getDefaultCredits());
        
        syllabus.setCreatedBy(currentUser);
        syllabus.setUpdatedBy(currentUser);

        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse updateSyllabus(UUID id, SyllabusRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        if (!syllabus.getStatus().isEditable()) {
            throw new BadRequestException("Syllabus hiện tại không thể chỉnh sửa");
        }

        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setStudentTasks(request.getStudentTasks());
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());

        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public void deleteSyllabus(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể xóa bản nháp");
        }
        syllabus.setIsDeleted(true);
        syllabusVersionRepository.save(syllabus);
    }

    @Transactional
    public SyllabusResponse submitSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setSubmittedAt(java.time.LocalDateTime.now());
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
            default -> syllabus.getStatus();
        };
        
        syllabus.setStatus(nextStatus);
        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse rejectSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        syllabus.setStatus(SyllabusStatus.REJECTED);
        return mapToResponse(syllabusVersionRepository.save(syllabus));
    }

    @Transactional
    public SyllabusResponse cloneSyllabus(UUID id) {
        SyllabusVersion original = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        SyllabusVersion clone = new SyllabusVersion();
        clone.setSubject(original.getSubject());
        clone.setVersionNo(original.getVersionNo() + "_COPY");
        clone.setContent(original.getContent());
        clone.setStudentTasks(original.getStudentTasks());
        clone.setStatus(SyllabusStatus.DRAFT);
        clone.setCreatedBy(getCurrentUser());
        
        return mapToResponse(syllabusVersionRepository.save(clone));
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabusVersions(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        return syllabusVersionRepository.findBySubjectId(syllabus.getSubject().getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        return syllabusVersionRepository.findBySubjectId(subjectId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SyllabusCompareResponse compareSyllabi(UUID id, UUID otherId) {
        SyllabusResponse a = getSyllabusById(id);
        SyllabusResponse b = getSyllabusById(otherId);
        
        SyllabusCompareResponse compare = new SyllabusCompareResponse();
        compare.setSyllabusA(a);
        compare.setSyllabusB(b);
        return compare;
    }

    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusById(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        return mapToResponse(syllabus);
    }

    private SyllabusResponse mapToResponse(SyllabusVersion syllabus) {
        SyllabusResponse response = new SyllabusResponse();
        response.setId(syllabus.getId());
        response.setVersionNo(syllabus.getVersionNo());
        response.setStatus(syllabus.getStatus().name());
        response.setStudentTasks(syllabus.getStudentTasks());
        if (syllabus.getSubject() != null) {
            response.setSubjectId(syllabus.getSubject().getId());
            response.setSubjectCode(syllabus.getSnapSubjectCode());
            response.setSubjectNameVi(syllabus.getSnapSubjectNameVi());
        }
        return response;
    }

    public byte[] exportSyllabusToPdf(UUID id) {
        return new byte[0]; // Placeholder cho tính năng xuất PDF
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getStatistics() {
        List<SyllabusVersion> all = syllabusVersionRepository.findAllActive();
        Map<String, Integer> stats = new LinkedHashMap<>();
        // Initialize all statuses to 0 to ensure consistent keys
        for (vn.edu.smd.shared.enums.SyllabusStatus s : vn.edu.smd.shared.enums.SyllabusStatus.values()) {
            stats.put(s.name(), 0);
        }

        for (SyllabusVersion v : all) {
            if (v.getStatus() != null) {
                String key = v.getStatus().name();
                stats.put(key, stats.getOrDefault(key, 0) + 1);
            }
        }

        return stats;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return userRepository.findById(principal.getId()).orElse(null);
        }
        return null;
    }
}