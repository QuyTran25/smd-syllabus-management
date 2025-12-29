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
        // Get current user to filter by role
        User currentUser = getCurrentUser();
        
        // If status is not provided, apply default filter based on user role
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }
        
        if (statusStrings != null && !statusStrings.isEmpty()) {
            String[] statusArray = statusStrings.toArray(new String[0]);
            return syllabusVersionRepository.findByStatusInWithPage(statusArray, pageable)
                    .map(this::mapToResponse);
        }
        
        // Admin sees all
        return syllabusVersionRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    /**
     * Get default status filter based on user role
     * This ensures each role only sees relevant syllabi
     */
    private List<String> getDefaultStatusByRole(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return List.of(); // No filter if no role
        }
        
        // Get primary role (assuming user has one primary role)
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(role -> role.getCode())
                .orElse("");
        
        return switch (primaryRole) {
            case "PRINCIPAL" -> List.of(
                SyllabusStatus.PENDING_PRINCIPAL.name(),
                SyllabusStatus.APPROVED.name()
            );
            case "AA" -> List.of(
                SyllabusStatus.PENDING_AA.name()
            );
            case "HOD" -> List.of(
                SyllabusStatus.PENDING_HOD.name(),
                SyllabusStatus.PENDING_HOD_REVISION.name()
            );
            case "LECTURER" -> List.of(
                SyllabusStatus.DRAFT.name(),
                SyllabusStatus.PENDING_HOD.name(),
                SyllabusStatus.PENDING_AA.name(),
                SyllabusStatus.PENDING_PRINCIPAL.name(),
                SyllabusStatus.APPROVED.name(),
                SyllabusStatus.PUBLISHED.name(),
                SyllabusStatus.REJECTED.name(),
                SyllabusStatus.REVISION_IN_PROGRESS.name(),
                SyllabusStatus.PENDING_HOD_REVISION.name()
            );
            case "ADMIN" -> List.of(); // Admin sees all, return empty to skip filter
            default -> List.of(); // Unknown role sees nothing or all
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

        SyllabusVersion syllabus = new SyllabusVersion();
        syllabus.setSubject(subject);
        syllabus.setAcademicTerm(academicTerm);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setStatus(SyllabusStatus.DRAFT);
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());

        // Set snapshots
        syllabus.setSnapSubjectCode(subject.getCode());
        syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
        syllabus.setSnapSubjectNameEn(subject.getCurrentNameEn());
        syllabus.setSnapCreditCount(subject.getDefaultCredits());

        syllabus.setCreatedBy(currentUser);
        syllabus.setUpdatedBy(currentUser);

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(savedSyllabus);
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

        AcademicTerm academicTerm = null;
        if (request.getAcademicTermId() != null) {
            academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));
        }

        syllabus.setSubject(subject);
        syllabus.setAcademicTerm(academicTerm);
        syllabus.setVersionNo(request.getVersionNo());
        syllabus.setReviewDeadline(request.getReviewDeadline());
        syllabus.setEffectiveDate(request.getEffectiveDate());
        syllabus.setKeywords(request.getKeywords());
        syllabus.setContent(request.getContent());

        // Update snapshots
        syllabus.setSnapSubjectCode(subject.getCode());
        syllabus.setSnapSubjectNameVi(subject.getCurrentNameVi());
        syllabus.setSnapSubjectNameEn(subject.getCurrentNameEn());
        syllabus.setSnapCreditCount(subject.getDefaultCredits());

        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public void deleteSyllabus(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be deleted");
        }

        syllabusVersionRepository.deleteById(id);
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

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse approveSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // Simple approval logic - move to next status
        SyllabusStatus currentStatus = syllabus.getStatus();
        SyllabusStatus nextStatus;

        switch (currentStatus) {
            case PENDING_HOD:
                nextStatus = SyllabusStatus.PENDING_AA;
                break;
            case PENDING_AA:
                nextStatus = SyllabusStatus.PENDING_PRINCIPAL;
                break;
            case PENDING_PRINCIPAL:
                nextStatus = SyllabusStatus.APPROVED;
                break;
            case APPROVED:
                nextStatus = SyllabusStatus.PUBLISHED;
                break;
            default:
                throw new BadRequestException("Cannot approve syllabus in status: " + currentStatus);
        }

        syllabus.setStatus(nextStatus);
        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse rejectSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        if (syllabus.getStatus() == SyllabusStatus.DRAFT || syllabus.getStatus() == SyllabusStatus.REJECTED) {
            throw new BadRequestException("Cannot reject syllabus in status: " + syllabus.getStatus());
        }

        syllabus.setStatus(SyllabusStatus.REJECTED);
        syllabus.setUpdatedBy(getCurrentUser());

        SyllabusVersion updatedSyllabus = syllabusVersionRepository.save(syllabus);
        return mapToResponse(updatedSyllabus);
    }

    @Transactional
    public SyllabusResponse cloneSyllabus(UUID id) {
        SyllabusVersion originalSyllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        User currentUser = getCurrentUser();

        SyllabusVersion newSyllabus = new SyllabusVersion();
        newSyllabus.setSubject(originalSyllabus.getSubject());
        newSyllabus.setAcademicTerm(originalSyllabus.getAcademicTerm());
        newSyllabus.setVersionNo(generateNextVersionNo(originalSyllabus.getVersionNo()));
        newSyllabus.setStatus(SyllabusStatus.DRAFT);
        newSyllabus.setPreviousVersion(originalSyllabus);
        newSyllabus.setReviewDeadline(originalSyllabus.getReviewDeadline());
        newSyllabus.setEffectiveDate(originalSyllabus.getEffectiveDate());
        newSyllabus.setKeywords(originalSyllabus.getKeywords());
        newSyllabus.setContent(originalSyllabus.getContent());

        // Copy snapshots
        newSyllabus.setSnapSubjectCode(originalSyllabus.getSnapSubjectCode());
        newSyllabus.setSnapSubjectNameVi(originalSyllabus.getSnapSubjectNameVi());
        newSyllabus.setSnapSubjectNameEn(originalSyllabus.getSnapSubjectNameEn());
        newSyllabus.setSnapCreditCount(originalSyllabus.getSnapCreditCount());

        newSyllabus.setCreatedBy(currentUser);
        newSyllabus.setUpdatedBy(currentUser);

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(newSyllabus);
        return mapToResponse(savedSyllabus);
    }

    public List<SyllabusResponse> getSyllabusVersions(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        List<SyllabusVersion> versions = new ArrayList<>();
        SyllabusVersion current = syllabus;

        // Get all versions by following previousVersion links
        while (current != null) {
            versions.add(current);
            current = current.getPreviousVersion();
        }

        Collections.reverse(versions);
        return versions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public SyllabusCompareResponse compareSyllabi(UUID id1, UUID id2) {
        SyllabusVersion syllabus1 = syllabusVersionRepository.findById(id1)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id1));
        SyllabusVersion syllabus2 = syllabusVersionRepository.findById(id2)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id2));

        SyllabusCompareResponse response = new SyllabusCompareResponse();
        response.setSyllabusA(mapToResponse(syllabus1));
        response.setSyllabusB(mapToResponse(syllabus2));
        response.setDifferences(calculateDifferences(syllabus1, syllabus2));

        return response;
    }

    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        return syllabusVersionRepository.findBySubject(subject).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public byte[] exportSyllabusToPdf(UUID id) {
        syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // TODO: Implement PDF export logic
        throw new BadRequestException("PDF export not implemented yet");
    }

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

        // Subject type and component
        Subject subject = syllabus.getSubject();
        if (subject != null) {
            if (subject.getSubjectType() != null) {
                response.setCourseType(subject.getSubjectType().name().toLowerCase());
            }
            // Get component type from syllabus (major/foundation/general), not from subject component (theory/practice)
            if (syllabus.getComponentType() != null) {
                response.setComponentType(syllabus.getComponentType().name().toLowerCase());
            }
            
            // Time allocation from subject
            response.setTheoryHours(subject.getDefaultTheoryHours());
            response.setPracticeHours(subject.getDefaultPracticeHours());
            response.setSelfStudyHours(subject.getDefaultSelfStudyHours());
            response.setTotalStudyHours(
                (subject.getDefaultTheoryHours() != null ? subject.getDefaultTheoryHours() : 0) +
                (subject.getDefaultPracticeHours() != null ? subject.getDefaultPracticeHours() : 0) +
                (subject.getDefaultSelfStudyHours() != null ? subject.getDefaultSelfStudyHours() : 0)
            );
            
            // Description from subject if content doesn't have it
            if (subject.getDescription() != null) {
                response.setDescription(subject.getDescription());
            }
            
            // Department and Faculty
            if (subject.getDepartment() != null) {
                response.setDepartment(subject.getDepartment().getName());
                if (subject.getDepartment().getFaculty() != null) {
                    response.setFaculty(subject.getDepartment().getFaculty().getName());
                }
            }
        }

        // Owner and department info
        if (syllabus.getCreatedBy() != null) {
            response.setCreatedBy(syllabus.getCreatedBy().getId());
            response.setOwnerName(syllabus.getCreatedBy().getFullName());
        }
        if (syllabus.getUpdatedBy() != null) {
            response.setUpdatedBy(syllabus.getUpdatedBy().getId());
        }
        
        // Academic year and semester from academic term
        if (syllabus.getAcademicTerm() != null) {
            // Parse semester number from academic term code (e.g., "HK1_2024" -> "1")
            String code = syllabus.getAcademicTerm().getCode();
            if (code != null && code.startsWith("HK")) {
                String semesterNum = code.substring(2, code.indexOf('_'));
                response.setSemester(semesterNum);
            } else {
                response.setSemester(syllabus.getAcademicTerm().getName());
            }
            response.setAcademicYear(syllabus.getAcademicTerm().getAcademicYear());
        }
        
        // Approval workflow tracking
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

        // Load CLO-PLO Mappings - DISABLED (lazy loading issue)
        // TODO: Fix lazy loading for PLO entity in CloPlOMapping
        List<SyllabusResponse.CLOPLOMappingResponse> ploMappings = new ArrayList<>();
        response.setPloMappings(ploMappings);

        // Load Assessment Schemes with CLO mappings
        List<AssessmentScheme> assessments = assessmentSchemeRepository.findBySyllabusVersionId(syllabus.getId());
        response.setAssessmentMethods(assessments.stream().map(as -> {
            SyllabusResponse.AssessmentResponse asResponse = new SyllabusResponse.AssessmentResponse();
            asResponse.setId(as.getId());
            asResponse.setName(as.getName());
            asResponse.setWeight(as.getWeightPercent());
            
            // Determine method and form based on assessment name
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
            
            // Get CLO codes linked to this assessment
            List<AssessmentCloMapping> acMappings = assessmentCloMappingRepository.findByAssessmentSchemeId(as.getId());
            List<String> cloCodes = acMappings.stream()
                .map(acm -> cloCodeMap.getOrDefault(acm.getClo().getId(), ""))
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
            asResponse.setClos(cloCodes);
            
            return asResponse;
        }).collect(Collectors.toList()));

        // Extract objectives from content if available
        if (syllabus.getContent() != null && syllabus.getContent().containsKey("objectives")) {
            Object objectives = syllabus.getContent().get("objectives");
            if (objectives instanceof List) {
                response.setObjectives((List<String>) objectives);
            }
        }
        
        // Extract description from content if not from subject
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
        // Simple version increment logic: v1.0 -> v1.1, v1.9 -> v2.0
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
        
        // Handle anonymous users
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findByIdWithRoles(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
