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
    private final CLORepository cloRepository;
    private final AssessmentSchemeRepository assessmentSchemeRepository;
    private final CloPlOMappingRepository cloPlOMappingRepository;
    private final AssessmentCloMappingRepository assessmentCloMappingRepository;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        User currentUser = getCurrentUser();
        
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }
        
        if (statusStrings != null && !statusStrings.isEmpty()) {
            List<SyllabusStatus> statuses = statusStrings.stream()
                    .map(s -> SyllabusStatus.valueOf(s.toUpperCase()))
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
        response.setDifferences(calculateDifferences(s1, s2));
        return response;
    }

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        return syllabusVersionRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportSyllabusToPdf(UUID id) {
        return new byte[0];
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
                .createdBy(getCurrentUser())
                .isDeleted(false)
                .build();
        return mapToResponse(syllabusVersionRepository.save(cloned));
    }

    // --- HELPERS (⭐ DÙNG LOGIC CHI TIẾT TỪ MAIN) ---
    private SyllabusResponse mapToResponse(SyllabusVersion syllabus) {
        SyllabusResponse response = new SyllabusResponse();
        response.setId(syllabus.getId());
        response.setSubjectId(syllabus.getSubject().getId());
        response.setSubjectCode(syllabus.getSnapSubjectCode());
        response.setSubjectNameVi(syllabus.getSnapSubjectNameVi());
        response.setSubjectNameEn(syllabus.getSnapSubjectNameEn());
        response.setCreditCount(syllabus.getSnapCreditCount());

        if (syllabus.getAcademicTerm() != null) {
            response.setAcademicTermId(syllabus.getAcademicTerm().getId());
            response.setAcademicTermCode(syllabus.getAcademicTerm().getCode());
        }

        response.setVersionNo(syllabus.getVersionNo());
        response.setStatus(syllabus.getStatus().name());
        
        if (syllabus.getPreviousVersion() != null) {
            response.setPreviousVersionId(syllabus.getPreviousVersion().getId());
        }

        response.setReviewDeadline(syllabus.getReviewDeadline());
        response.setEffectiveDate(syllabus.getEffectiveDate());
        response.setKeywords(syllabus.getKeywords());
        response.setContent(syllabus.getContent());

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

        // Load CLOs
        List<CLO> clos = cloRepository.findBySyllabusVersionId(syllabus.getId());
        Map<UUID, String> cloCodeMap = new HashMap<>();
        response.setClos(clos.stream().map(clo -> {
            cloCodeMap.put(clo.getId(), clo.getCode());
            SyllabusResponse.CLOResponse cloResponse = new SyllabusResponse.CLOResponse();
            cloResponse.setId(clo.getId());
            cloResponse.setCode(clo.getCode());
            cloResponse.setDescription(clo.getDescription());
            cloResponse.setBloomLevel(clo.getBloomLevel());
            cloResponse.setWeight(clo.getWeight());
            return cloResponse;
        }).collect(Collectors.toList()));

        // Load CLO-PLO Mappings
        List<SyllabusResponse.CLOPLOMappingResponse> ploMappings = new ArrayList<>();
        for (CLO clo : clos) {
            List<CloPlOMapping> mappings = cloPlOMappingRepository.findByCloId(clo.getId());
            for (CloPlOMapping mapping : mappings) {
                SyllabusResponse.CLOPLOMappingResponse mapResponse = new SyllabusResponse.CLOPLOMappingResponse();
                mapResponse.setCloCode(clo.getCode());
                mapResponse.setPloCode(mapping.getPlo().getCode());
                mapResponse.setContributionLevel(mapping.getMappingLevel());
                ploMappings.add(mapResponse);
            }
        }
        response.setPloMappings(ploMappings);

        // Load Assessment Schemes
        List<AssessmentScheme> assessments = assessmentSchemeRepository.findBySyllabusVersionId(syllabus.getId());
        response.setAssessmentMethods(assessments.stream().map(as -> {
            SyllabusResponse.AssessmentResponse asResponse = new SyllabusResponse.AssessmentResponse();
            asResponse.setId(as.getId());
            asResponse.setName(as.getName());
            asResponse.setWeight(as.getWeightPercent());
            
            String name = as.getName().toLowerCase();
            if (name.contains("chuyên cần") || name.contains("điểm danh")) {
                asResponse.setMethod("Đánh giá quá trình");
                asResponse.setForm("Điểm danh + tham gia lớp học");
                asResponse.setCriteria("Có mặt đầy đủ, tích cực tham gia thảo luận");
            } else if (name.contains("bài tập") || name.contains("thực hành")) {
                asResponse.setMethod("Đánh giá thường xuyên");
                asResponse.setForm("Bài tập + Báo cáo thực hành");
                asResponse.setCriteria("Hoàn thành bài tập đúng hạn, chất lượng tốt");
            } else if (name.contains("giữa kỳ")) {
                asResponse.setMethod("Kiểm tra giữa kỳ");
                asResponse.setForm("Thi viết (60 phút)");
                asResponse.setCriteria("Trả lời đúng các câu hỏi lý thuyết và bài tập");
            } else if (name.contains("cuối kỳ") || name.contains("thi")) {
                asResponse.setMethod("Thi cuối kỳ");
                asResponse.setForm("Thi viết (90 phút)");
                asResponse.setCriteria("Đánh giá toàn diện kiến thức và kỹ năng");
            } else {
                asResponse.setMethod(as.getName());
                asResponse.setForm("Theo quy định");
                asResponse.setCriteria("Theo rubric đánh giá");
            }
            
            List<AssessmentCloMapping> acMappings = assessmentCloMappingRepository.findByAssessmentSchemeId(as.getId());
            List<String> cloCodes = acMappings.stream()
                .map(acm -> cloCodeMap.getOrDefault(acm.getClo().getId(), ""))
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
            asResponse.setClos(cloCodes);
            
            return asResponse;
        }).collect(Collectors.toList()));

        if (syllabus.getContent() != null && syllabus.getContent().containsKey("objectives")) {
            Object objectives = syllabus.getContent().get("objectives");
            if (objectives instanceof List) {
                response.setObjectives((List<String>) objectives);
            }
        }
        
        if (response.getDescription() == null && syllabus.getContent() != null && syllabus.getContent().containsKey("description")) {
            response.setDescription((String) syllabus.getContent().get("description"));
        }

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