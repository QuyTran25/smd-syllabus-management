package vn.edu.smd.core.module.syllabus.service;

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
import vn.edu.smd.core.module.ai.service.AITaskService;
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.SyllabusStatus;
import vn.edu.smd.shared.enums.NotificationType;
import vn.edu.smd.shared.enums.AssignmentStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyllabusService {

    private final SyllabusVersionRepository syllabusVersionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicTermRepository academicTermRepository;
    private final UserRepository userRepository;
    private final CLORepository cloRepository;
    private final AssessmentSchemeRepository assessmentSchemeRepository;
    private final CloPlOMappingRepository cloPlOMappingRepository;
    private final AssessmentCloMappingRepository assessmentCloMappingRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentCollaboratorRepository teachingAssignmentCollaboratorRepository;
    private final SyllabusCollaboratorRepository syllabusCollaboratorRepository;
    private final AITaskService aiTaskService;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings) {
        User currentUser = getCurrentUser();
        
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }
        
        if (statusStrings != null && !statusStrings.isEmpty()) {
            // Convert String list to SyllabusStatus enum list
            List<SyllabusStatus> statuses = statusStrings.stream()
                    .map(SyllabusStatus::valueOf)
                    .collect(Collectors.toList());
            
            // Use Spring Data method - @JdbcType in entity handles enum properly
            List<SyllabusVersion> allResults = syllabusVersionRepository.findByStatusInAndIsDeletedFalse(statuses);
            List<SyllabusResponse> responses = allResults.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), responses.size());
            List<SyllabusResponse> pageContent = start < responses.size() ? responses.subList(start, end) : List.of();
            
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, responses.size());
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
            case "AA" -> List.of(
                SyllabusStatus.PENDING_AA.name(), 
                SyllabusStatus.PENDING_PRINCIPAL.name(), 
                SyllabusStatus.REJECTED.name()
            );
            case "HOD" -> List.of(
                SyllabusStatus.PENDING_HOD.name(), 
                SyllabusStatus.PENDING_AA.name(), 
                SyllabusStatus.REJECTED.name()
            );
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

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // If created from teaching assignment, update status and notify HOD
        if (request.getTeachingAssignmentId() != null) {
            updateTeachingAssignmentStatus(request.getTeachingAssignmentId(), AssignmentStatus.IN_PROGRESS);
            sendNotificationToHodOnCreate(request.getTeachingAssignmentId(), savedSyllabus, currentUser);
        }
        
        return mapToResponse(savedSyllabus);
    }

    /**
     * Tạo syllabus draft từ teaching assignment
     * Tự động điền các thông tin cơ bản từ subject và academic term
     */
    @Transactional
    public SyllabusResponse createSyllabusFromAssignment(CreateSyllabusFromAssignmentRequest request) {
        // Lấy teaching assignment
        TeachingAssignment assignment = teachingAssignmentRepository.findById(request.getTeachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("TeachingAssignment", "id", request.getTeachingAssignmentId()));
        
        User currentUser = getCurrentUser();
        
        // Kiểm tra quyền: chỉ main lecturer mới có thể tạo
        if (!assignment.getMainLecturer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Chỉ giảng viên chính mới có thể tạo đề cương");
        }
        
        // Kiểm tra xem đã có syllabus draft cho assignment này chưa
        Optional<SyllabusVersion> existingDraft = syllabusVersionRepository
                .findBySubjectIdAndAcademicTermIdAndStatus(
                        assignment.getSubject().getId(),
                        assignment.getAcademicTerm().getId(),
                        SyllabusStatus.DRAFT
                );
        
        if (existingDraft.isPresent()) {
            // Nếu đã có draft thì trả về draft đó, nhưng vẫn cần đảm bảo collaborators được tạo
            log.info("Đã tồn tại syllabus draft cho assignment {}, trả về draft hiện tại", assignment.getId());
            SyllabusVersion existingSyllabus = existingDraft.get();
            
            // Đảm bảo collaborators được tạo cho draft hiện tại (nếu chưa có)
            createSyllabusCollaboratorsFromAssignment(assignment, existingSyllabus, currentUser);
            
            // Link syllabus to teaching assignment and update status
            linkSyllabusToAssignment(assignment, existingSyllabus);
            
            return mapToResponse(existingSyllabus);
        }
        
        Subject subject = assignment.getSubject();
        AcademicTerm academicTerm = assignment.getAcademicTerm();
        
        // Tạo version number tự động
        String versionNo = generateVersionNo(subject.getId(), academicTerm.getId());
        
        log.info("Tạo syllabus draft từ teaching assignment {} cho môn {} - {}",
                assignment.getId(), subject.getCode(), subject.getCurrentNameVi());
        
        // Tạo syllabus version với thông tin cơ bản từ assignment
        SyllabusVersion syllabus = SyllabusVersion.builder()
                .subject(subject)
                .academicTerm(academicTerm)
                .versionNo(versionNo)
                .status(SyllabusStatus.DRAFT)
                .reviewDeadline(assignment.getDeadline().atStartOfDay())
                // Snapshot từ subject
                .snapSubjectCode(subject.getCode())
                .snapSubjectNameVi(subject.getCurrentNameVi())
                .snapSubjectNameEn(subject.getCurrentNameEn())
                .snapCreditCount(subject.getDefaultCredits())
                // Theory/Practice hours từ subject
                .theoryHours(subject.getDefaultTheoryHours())
                .practiceHours(subject.getDefaultPracticeHours())
                .selfStudyHours(subject.getDefaultSelfStudyHours())
                // Audit fields
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .isDeleted(false)
                .build();
        
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // Auto-create syllabus collaborators from teaching assignment collaborators
        createSyllabusCollaboratorsFromAssignment(assignment, savedSyllabus, currentUser);
        
        // Link syllabus to teaching assignment and update status to IN_PROGRESS
        linkSyllabusToAssignment(assignment, savedSyllabus);
        
        log.info("Đã tạo syllabus draft {} cho môn {} (Teaching Assignment: {})",
                savedSyllabus.getId(), subject.getCode(), assignment.getId());
        
        return mapToResponse(savedSyllabus);
    }
    
    /**
     * Generate version number tự động
     */
    private String generateVersionNo(UUID subjectId, UUID academicTermId) {
        // Đếm số syllabus versions của môn học trong kỳ này
        long count = syllabusVersionRepository.countBySubjectIdAndAcademicTermId(subjectId, academicTermId);
        return "v" + (count + 1) + ".0";
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
        
        // Update status to SUBMITTED (PENDING_HOD)
        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setUpdatedBy(getCurrentUser());
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
                // Update teaching assignment status to SUBMITTED
        updateTeachingAssignmentStatusBySyllabus(savedSyllabus, AssignmentStatus.SUBMITTED);
                // � Send notification to HOD
        sendNotificationToHod(savedSyllabus);
        
        // �🚀 Send message to RabbitMQ AI Queue for processing
        try {
            User currentUser = getCurrentUser();
            String messageId = aiTaskService.requestCloPloMapping(
                savedSyllabus.getId(),
                syllabus.getSubject() != null && syllabus.getSubject().getCurriculum() != null 
                    ? syllabus.getSubject().getCurriculum().getId() 
                    : null,
                currentUser.getId().toString()
            );
            
            log.info("[Sent] Message to AI Queue: Syllabus ID #{} | Message ID: {}", 
                     savedSyllabus.getId(), messageId);
            
        } catch (Exception e) {
            // Log error nhưng không fail transaction
            log.error("❌ Failed to send message to AI Queue for Syllabus ID #{}: {}", 
                      savedSyllabus.getId(), e.getMessage());
        }
        
        return mapToResponse(savedSyllabus);
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
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // When HOD approves (PENDING_HOD → PENDING_AA), update assignment to COMPLETED
        if (syllabus.getStatus() == SyllabusStatus.PENDING_HOD && nextStatus == SyllabusStatus.PENDING_AA) {
            updateTeachingAssignmentStatusBySyllabus(savedSyllabus, AssignmentStatus.COMPLETED);
        }
        
        return mapToResponse(savedSyllabus);
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
            
            // Extract semester from term code (HK1_2024 -> HK1, Học kỳ 1)
            String termCode = syllabus.getAcademicTerm().getCode();
            if (termCode != null && termCode.contains("_")) {
                String semesterCode = termCode.split("_")[0]; // HK1, HK2, HK3
                response.setSemester(semesterCode);
            }
            
            // Set academic year from term
            if (syllabus.getAcademicTerm().getAcademicYear() != null) {
                response.setAcademicYear(syllabus.getAcademicTerm().getAcademicYear());
            }
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
            // Get component type from syllabus (major/foundation/general), not from subject component (theory/practice)
            if (syllabus.getComponentType() != null) {
                response.setComponentType(syllabus.getComponentType().name().toLowerCase());
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
                SyllabusResponse.CLOPLOMappingResponse mappingResponse = new SyllabusResponse.CLOPLOMappingResponse();
                mappingResponse.setCloCode(clo.getCode());
                // Load PLO eagerly to avoid LazyInitializationException
                PLO plo = mapping.getPlo();
                mappingResponse.setPloCode(plo.getCode());
                mappingResponse.setContributionLevel(mapping.getMappingLevel());
                ploMappings.add(mappingResponse);
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
                @SuppressWarnings("unchecked")
                List<String> objectivesList = (List<String>) objectives;
                response.setObjectives(objectivesList);
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
    
    /**
     * Send notification to HOD when lecturer submits syllabus for approval
     */
    private void sendNotificationToHod(SyllabusVersion syllabus) {
        try {
            // Find teaching assignment to get HOD (assignedBy)
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                        syllabus.getSubject().getId(),
                        syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isEmpty()) {
                log.warn("No teaching assignment found for syllabus {} - cannot send HOD notification", 
                         syllabus.getId());
                return;
            }
            
            TeachingAssignment assignment = assignmentOpt.get();
            User hod = assignment.getAssignedBy(); // HOD is the one who assigned
            User lecturer = syllabus.getCreatedBy();
            
            if (hod == null) {
                log.warn("HOD not found for teaching assignment {} - cannot send notification", 
                         assignment.getId());
                return;
            }
            
            // Build notification message
            String title = String.format("[Đề cương mới] %s - %s", 
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
            
            String message = String.format(
                "Giảng viên %s đã nộp đề cương môn học %s (%s) - %s để bạn phê duyệt.\n\n" +
                "Số tín chỉ: %d\n" +
                "Học kỳ: %s\n" +
                "Phiên bản: %s\n\n" +
                "Vui lòng xem xét và phê duyệt đề cương.",
                lecturer != null ? lecturer.getFullName() : "Unknown",
                syllabus.getSnapSubjectNameVi(),
                syllabus.getSnapSubjectCode(),
                syllabus.getAcademicTerm() != null ? syllabus.getAcademicTerm().getName() : "",
                syllabus.getSnapCreditCount(),
                syllabus.getAcademicTerm() != null ? syllabus.getAcademicTerm().getName() : "",
                syllabus.getVersionNo()
            );
            
            // Create payload for action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/hod/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem và phê duyệt");
            
            Notification notification = Notification.builder()
                    .user(hod)
                    .title(title)
                    .message(message)
                    .type("SYLLABUS_REVIEW")
                    .payload(payload)
                    .isRead(false)
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Sent notification to HOD {} for syllabus {} submission", 
                     hod.getEmail(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send notification to HOD for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification to HOD when lecturer starts creating syllabus from assignment
     */
    private void sendNotificationToHodOnCreate(UUID teachingAssignmentId, SyllabusVersion syllabus, User lecturer) {
        try {
            TeachingAssignment assignment = teachingAssignmentRepository.findById(teachingAssignmentId)
                    .orElse(null);
            
            if (assignment == null) {
                log.warn("Teaching assignment {} not found - cannot send HOD notification", teachingAssignmentId);
                return;
            }
            
            User hod = assignment.getAssignedBy();
            if (hod == null) {
                log.warn("HOD not found for teaching assignment {} - cannot send notification", teachingAssignmentId);
                return;
            }
            
            String title = String.format("[Đang biên soạn] %s - %s", 
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
            
            String message = String.format(
                "Giảng viên %s đã bắt đầu biên soạn đề cương môn học %s (%s) - %s.\n\n" +
                "Số tín chỉ: %d\n" +
                "Học kỳ: %s\n" +
                "Trạng thái: Đang soạn thảo\n\n" +
                "Bạn sẽ nhận được thông báo khi giảng viên nộp đề cương để phê duyệt.",
                lecturer.getFullName(),
                syllabus.getSnapSubjectNameVi(),
                syllabus.getSnapSubjectCode(),
                syllabus.getAcademicTerm() != null ? syllabus.getAcademicTerm().getName() : "",
                syllabus.getSnapCreditCount(),
                syllabus.getAcademicTerm() != null ? syllabus.getAcademicTerm().getName() : ""
            );
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("assignmentId", teachingAssignmentId.toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/hod/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem tiến độ");
            
            Notification notification = Notification.builder()
                    .user(hod)
                    .title(title)
                    .message(message)
                    .type("SYLLABUS_PROGRESS")
                    .payload(payload)
                    .isRead(false)
                    .relatedEntityType("SYLLABUS")
                    .relatedEntityId(syllabus.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Sent progress notification to HOD {} for syllabus {} creation", 
                     hod.getEmail(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send progress notification to HOD for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Auto-create syllabus collaborators from teaching assignment collaborators
     */
    private void createSyllabusCollaboratorsFromAssignment(TeachingAssignment assignment, 
                                                           SyllabusVersion syllabus, 
                                                           User mainLecturer) {
        try {
            // Get collaborators from teaching assignment
            List<TeachingAssignmentCollaborator> assignmentCollaborators = 
                    teachingAssignmentCollaboratorRepository.findByAssignmentId(assignment.getId());
            
            if (assignmentCollaborators.isEmpty()) {
                log.info("No collaborators found for teaching assignment {}", assignment.getId());
                return;
            }
            
            log.info("Creating {} syllabus collaborators from teaching assignment {}", 
                     assignmentCollaborators.size(), assignment.getId());
            
            for (TeachingAssignmentCollaborator assignmentCollab : assignmentCollaborators) {
                // Check if collaborator already exists
                Optional<SyllabusCollaborator> existing = syllabusCollaboratorRepository
                        .findBySyllabusVersionIdAndUserId(syllabus.getId(), assignmentCollab.getLecturer().getId());
                
                if (existing.isEmpty()) {
                    SyllabusCollaborator syllabusCollab = SyllabusCollaborator.builder()
                            .syllabusVersion(syllabus)
                            .user(assignmentCollab.getLecturer())
                            .role(vn.edu.smd.shared.enums.CollaboratorRole.EDITOR) // Default role for all collaborators
                            .build();
                    
                    syllabusCollaboratorRepository.save(syllabusCollab);
                    
                    log.info("Created syllabus collaborator for user {} on syllabus {}", 
                             assignmentCollab.getLecturer().getEmail(), syllabus.getId());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to create syllabus collaborators from assignment {}: {}", 
                      assignment.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Link syllabus to teaching assignment and update status
     */
    private void linkSyllabusToAssignment(TeachingAssignment assignment, SyllabusVersion syllabus) {
        try {
            // Set syllabus reference
            assignment.setSyllabusVersion(syllabus);
            
            // Update status to IN_PROGRESS if still PENDING
            if (assignment.getStatus() == AssignmentStatus.PENDING) {
                assignment.setStatus(AssignmentStatus.IN_PROGRESS);
            }
            
            teachingAssignmentRepository.save(assignment);
            log.info("Linked syllabus {} to teaching assignment {} with status {}", 
                    syllabus.getId(), assignment.getId(), assignment.getStatus());
        } catch (Exception e) {
            log.error("Failed to link syllabus {} to teaching assignment {}: {}", 
                      syllabus.getId(), assignment.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Update teaching assignment status by ID
     */
    private void updateTeachingAssignmentStatus(UUID assignmentId, AssignmentStatus newStatus) {
        try {
            TeachingAssignment assignment = teachingAssignmentRepository.findById(assignmentId)
                    .orElse(null);
            
            if (assignment != null) {
                assignment.setStatus(newStatus);
                teachingAssignmentRepository.save(assignment);
                log.info("Updated teaching assignment {} status to {}", assignmentId, newStatus);
            }
        } catch (Exception e) {
            log.error("Failed to update teaching assignment {} status: {}", 
                      assignmentId, e.getMessage(), e);
        }
    }
    
    /**
     * Update teaching assignment status by syllabus (find assignment by subject + term)
     */
    private void updateTeachingAssignmentStatusBySyllabus(SyllabusVersion syllabus, AssignmentStatus newStatus) {
        try {
            if (syllabus.getSubject() == null || syllabus.getAcademicTerm() == null) {
                return;
            }
            
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                        syllabus.getSubject().getId(),
                        syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isPresent()) {
                TeachingAssignment assignment = assignmentOpt.get();
                assignment.setStatus(newStatus);
                teachingAssignmentRepository.save(assignment);
                log.info("Updated teaching assignment {} status to {} for syllabus {}", 
                         assignment.getId(), newStatus, syllabus.getId());
            }
        } catch (Exception e) {
            log.error("Failed to update teaching assignment status for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
}