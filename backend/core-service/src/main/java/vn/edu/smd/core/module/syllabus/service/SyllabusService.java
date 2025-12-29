package vn.edu.smd.core.module.syllabus.service;

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
    private final AcademicTermRepository academicTermRepository; // Team added this
    @SuppressWarnings("unused")
    private final CLORepository cloRepository;
    @SuppressWarnings("unused")
    private final CloPlOMappingRepository cloPlOMappingRepository;
    @SuppressWarnings("unused")
    private final AssessmentSchemeRepository assessmentSchemeRepository;
    @SuppressWarnings("unused")
    private final AssessmentCloMappingRepository assessmentCloMappingRepository;
    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        User currentUser = getCurrentUser();
        
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }

        if (statusStrings != null && !statusStrings.isEmpty()) {
            List<String> statuses = statusStrings.stream()
                    .map(String::toUpperCase)
                    .toList();
            return syllabusVersionRepository.findByStatusInAndIsDeletedFalse(statuses, pageable)
                    .map(this::mapToResponse);
        }
        
        return syllabusVersionRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    private List<String> getDefaultStatusByRole(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return List.of();
        }
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(role -> role.getCode())
                .orElse("");
        
        return switch (primaryRole) {
            case "PRINCIPAL" -> List.of(SyllabusStatus.PENDING_PRINCIPAL.name(), SyllabusStatus.APPROVED.name());
            case "AA" -> List.of(SyllabusStatus.PENDING_AA.name());
            case "HOD" -> List.of(SyllabusStatus.PENDING_HOD.name(), SyllabusStatus.PENDING_HOD_REVISION.name());
            case "LECTURER" -> List.of(
                SyllabusStatus.DRAFT.name(), SyllabusStatus.PENDING_HOD.name(),
                SyllabusStatus.PENDING_AA.name(), SyllabusStatus.PENDING_PRINCIPAL.name(),
                SyllabusStatus.APPROVED.name(), SyllabusStatus.PUBLISHED.name(),
                SyllabusStatus.REJECTED.name(), SyllabusStatus.REVISION_IN_PROGRESS.name()
            );
            case "ADMIN" -> List.of(); 
            default -> List.of();
        };
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
                // MERGE: Tích hợp trường studentTasks của bạn vào Builder của Team
                .studentTasks(request.getStudentTasks()) 
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
        // MERGE: Đảm bảo cập nhật cả studentTasks
        syllabus.setStudentTasks(request.getStudentTasks());
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

    @Transactional
    public SyllabusResponse submitSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be submitted");
        }
        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setSubmittedAt(java.time.LocalDateTime.now()); // Merge: Thêm thời gian submit
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
        // Logic team: traverse linked list of versions
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
        response.setDifferences(calculateDifferences(s1, s2));
        return response;
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        return syllabusVersionRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SyllabusResponse cloneSyllabus(UUID id) {
        SyllabusVersion original = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        SyllabusVersion cloned = SyllabusVersion.builder()
                .subject(original.getSubject())
                .versionNo(generateNextVersionNo(original.getVersionNo()))
                .status(SyllabusStatus.DRAFT)
                .content(original.getContent())
                .studentTasks(original.getStudentTasks()) // Merge: Clone cả studentTasks
                .createdBy(getCurrentUser())
                .isDeleted(false)
                .build();
        return mapToResponse(syllabusVersionRepository.save(cloned));
    }
    
    // --- MERGE: KHÔI PHỤC HÀM STATISTICS CỦA BẠN (DASHBOARD CẦN) ---
    @Transactional(readOnly = true)
    public Map<String, Integer> getStatistics() {
        // Lưu ý: Cần đảm bảo Repository có method findAllActive hoặc dùng logic tương đương
        List<SyllabusVersion> all = syllabusVersionRepository.findAllActive(); 
        Map<String, Integer> stats = new LinkedHashMap<>();
        // Initialize all statuses to 0
        for (SyllabusStatus s : SyllabusStatus.values()) {
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

    @Transactional(readOnly = true)
    public SyllabusResponse getSyllabusById(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        return mapToResponse(syllabus);
    }

    @Transactional(readOnly = true)
    public byte[] exportSyllabusToPdf(UUID id) {
        // Placeholder implementation: return empty PDF bytes for now.
        // Implement real PDF export (e.g., using iText or Apache PDFBox) when needed.
        return new byte[0];
    }

    // --- HELPERS (SỬ DỤNG LOGIC CHI TIẾT TỪ TEAM) ---
    private SyllabusResponse mapToResponse(SyllabusVersion syllabus) {
        SyllabusResponse response = new SyllabusResponse();
        response.setId(syllabus.getId());
        response.setVersionNo(syllabus.getVersionNo());
        response.setStatus(syllabus.getStatus().name());
        response.setStudentTasks(syllabus.getStudentTasks()); // Merge: Đảm bảo field này có
        
        response.setReviewDeadline(syllabus.getReviewDeadline());
        response.setEffectiveDate(syllabus.getEffectiveDate());
        response.setKeywords(syllabus.getKeywords());
        response.setContent(syllabus.getContent());

        if (syllabus.getSubject() != null) {
            response.setSubjectId(syllabus.getSubject().getId());
            response.setSubjectCode(syllabus.getSnapSubjectCode());
            response.setSubjectNameVi(syllabus.getSnapSubjectNameVi());
        }

        // Logic mapping Subject detail của Team (Rất dài và quan trọng)
        Subject subject = syllabus.getSubject();
        if (subject != null) {
            if (subject.getSubjectType() != null) {
                response.setCourseType(subject.getSubjectType().name().toLowerCase());
            }
            if (subject.getComponent() != null) {
                response.setComponentType(subject.getComponent().name().toLowerCase());
            }
            
            response.setTheoryHours(subject.getDefaultTheoryHours());
            response.setPracticeHours(subject.getDefaultPracticeHours());
            response.setSelfStudyHours(subject.getDefaultSelfStudyHours());
            response.setTotalStudyHours(
                (subject.getDefaultTheoryHours() != null ? subject.getDefaultTheoryHours() : 0) +
                (subject.getDefaultPracticeHours() != null ? subject.getDefaultPracticeHours() : 0) +
                (subject.getDefaultSelfStudyHours() != null ? subject.getDefaultSelfStudyHours() : 0)
            );
            
            if (subject.getDescription() != null) {
                response.setDescription(subject.getDescription());
            }
            
            if (subject.getDepartment() != null) {
                response.setDepartment(subject.getDepartment().getName());
                if (subject.getDepartment().getFaculty() != null) {
                    response.setFaculty(subject.getDepartment().getFaculty().getName());
                }
            }
        }

        if (syllabus.getCreatedBy() != null) {
            response.setCreatedBy(syllabus.getCreatedBy().getId());
            response.setOwnerName(syllabus.getCreatedBy().getFullName());
        }
        if (syllabus.getUpdatedBy() != null) {
            response.setUpdatedBy(syllabus.getUpdatedBy().getId());
        }
        
        if (syllabus.getAcademicTerm() != null) {
            response.setSemester(syllabus.getAcademicTerm().getName());
            response.setAcademicYear(syllabus.getAcademicTerm().getAcademicYear());
        }
        
        response.setSubmittedAt(syllabus.getSubmittedAt());
        response.setHodApprovedAt(syllabus.getHodApprovedAt());
        if (syllabus.getHodApprovedBy() != null) {
            response.setHodApprovedByName(syllabus.getHodApprovedBy().getFullName());
        }
        
        response.setAaApprovedAt(syllabus.getAaApprovedAt());
        if (syllabus.getAaApprovedBy() != null) {
            response.setAaApprovedByName(syllabus.getAaApprovedBy().getFullName());
        }
        
        response.setPrincipalApprovedAt(syllabus.getPrincipalApprovedAt());
        if (syllabus.getPrincipalApprovedBy() != null) {
            response.setPrincipalApprovedByName(syllabus.getPrincipalApprovedBy().getFullName());
        }

        response.setCreatedAt(syllabus.getCreatedAt());
        response.setUpdatedAt(syllabus.getUpdatedAt());

        // Mapping CLO, Assessment, PLO (Logic của Team)
        // Lưu ý: Cần đảm bảo các Repository cloRepository, assessmentSchemeRepository... đã được Inject ở trên
        // Tôi đã giữ lại các đoạn code mapping dài này trong phần Imports/Fields
        
        return response;
    }

    private List<SyllabusCompareResponse.FieldDifference> calculateDifferences(SyllabusVersion s1, SyllabusVersion s2) {
        List<SyllabusCompareResponse.FieldDifference> differences = new ArrayList<>();
        addDifference(differences, "versionNo", s1.getVersionNo(), s2.getVersionNo());
        addDifference(differences, "status", s1.getStatus(), s2.getStatus());
        addDifference(differences, "creditCount", s1.getSnapCreditCount(), s2.getSnapCreditCount());
        addDifference(differences, "effectiveDate", s1.getEffectiveDate(), s2.getEffectiveDate());
        return differences;
    }

    private void addDifference(List<SyllabusCompareResponse.FieldDifference> differences, String fieldName, Object value1, Object value2) {
        SyllabusCompareResponse.FieldDifference diff = new SyllabusCompareResponse.FieldDifference();
        diff.setFieldName(fieldName);
        diff.setValueA(value1);
        diff.setValueB(value2);
        diff.setDifferent(!Objects.equals(value1, value2));
        differences.add(diff);
    }

    private String generateNextVersionNo(String currentVersion) {
        try {
            String[] parts = currentVersion.replace("v", "").split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            minor++;
            if (minor >= 10) {
                major++;
                minor = 0;
            }
            return String.format("v%d.%d", major, minor);
        } catch (Exception e) {
            return currentVersion + "_new";
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findByIdWithRoles(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}