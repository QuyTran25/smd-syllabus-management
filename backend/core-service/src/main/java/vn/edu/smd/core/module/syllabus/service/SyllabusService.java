package vn.edu.smd.core.module.syllabus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.ai.service.AITaskService;
import vn.edu.smd.core.module.student.repository.StudentSyllabusTrackerRepository; // Import Repo Tracker
import vn.edu.smd.core.module.syllabus.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.core.security.UserPrincipal;
import vn.edu.smd.shared.enums.SyllabusStatus;
import vn.edu.smd.shared.enums.AssignmentStatus;
import vn.edu.smd.shared.enums.ActorRoleType;
import vn.edu.smd.shared.enums.DecisionType;
import vn.edu.smd.shared.enums.NotificationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentCollaboratorRepository teachingAssignmentCollaboratorRepository;
    private final SyllabusCollaboratorRepository syllabusCollaboratorRepository;
    private final AITaskService aiTaskService;
    private final NotificationRepository notificationRepository;

    // --- Merge Conflict Resolved: Include ALL required dependencies ---
    // Từ Ours: Repo theo dõi để gửi thông báo
    private final StudentSyllabusTrackerRepository studentSyllabusTrackerRepository;
    
    // Từ Theirs: Service và Repo cho logic nghiệp vụ
    private final vn.edu.smd.core.service.PloMappingService ploMappingService;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final SyllabusVersionHistoryRepository syllabusVersionHistoryRepository;
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<SyllabusResponse> getAllSyllabi(Pageable pageable, List<String> statusStrings, String search, 
                                                  List<String> faculties, List<String> departments) {
        User currentUser = getCurrentUser();
        
        if (statusStrings == null || statusStrings.isEmpty()) {
            statusStrings = getDefaultStatusByRole(currentUser);
        }
        
        if (statusStrings != null && !statusStrings.isEmpty()) {
            List<SyllabusStatus> statuses = statusStrings.stream()
                    .map(SyllabusStatus::valueOf)
                    .collect(Collectors.toList());
            
            // Convert to String array for native query
            String[] statusArray = statuses.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
            
            List<SyllabusVersion> allResults = syllabusVersionRepository.findByStatusInAndIsDeletedFalse(statusArray);
            List<SyllabusResponse> responses = allResults.stream()
                    .filter(sv -> matchesSearchCriteria(sv, search, faculties, departments))
                    .map(this::mapToResponse)
                    .filter(Objects::nonNull) // Filter out null responses from invalid data
                    .collect(Collectors.toList());
            
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

    private boolean matchesSearchCriteria(SyllabusVersion syllabus, String search, 
                                         List<String> faculties, List<String> departments) {
        // Nếu không có tiêu chí tìm kiếm, chấp nhận
        if ((search == null || search.trim().isEmpty()) && 
            (faculties == null || faculties.isEmpty()) && 
            (departments == null || departments.isEmpty())) {
            return true;
        }

        Subject subject = syllabus.getSubject();
        if (subject == null) {
            return false;
        }

        // Kiểm tra search text
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            String code = subject.getCode() != null ? subject.getCode().toLowerCase() : "";
            String nameVi = subject.getCurrentNameVi() != null ? subject.getCurrentNameVi().toLowerCase() : "";
            String nameEn = subject.getCurrentNameEn() != null ? subject.getCurrentNameEn().toLowerCase() : "";
            
            boolean matchesSearch = code.contains(searchLower) || 
                                   nameVi.contains(searchLower) || 
                                   nameEn.contains(searchLower);
            if (!matchesSearch) {
                return false;
            }
        }

        // Kiểm tra faculty filter
        if (faculties != null && !faculties.isEmpty()) {
            boolean matchesFaculty = false;
            if (subject.getDepartment() != null && subject.getDepartment().getFaculty() != null) {
                String facultyName = subject.getDepartment().getFaculty().getName();
                matchesFaculty = faculties.contains(facultyName);
            }
            if (!matchesFaculty) {
                return false;
            }
        }

        // Kiểm tra department filter
        if (departments != null && !departments.isEmpty()) {
            boolean matchesDept = false;
            if (subject.getDepartment() != null) {
                String deptName = subject.getDepartment().getName();
                matchesDept = departments.contains(deptName);
            }
            if (!matchesDept) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public SyllabusResponse publishSyllabus(UUID id, PublishSyllabusRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // Chỉ cho phép xuất bản nếu đang ở trạng thái APPROVED
        if (syllabus.getStatus() != SyllabusStatus.APPROVED) {
             throw new BadRequestException("Chỉ đề cương đã được phê duyệt (APPROVED) mới có thể xuất hành");
        }

        // 1. Chuyển trạng thái sang PUBLISHED
        syllabus.setStatus(SyllabusStatus.PUBLISHED);
        syllabus.setPublishedAt(java.time.LocalDateTime.now());
        
        // 2. Lưu ngày hiệu lực (Lấy từ Modal Admin nhập)
        if (request != null && request.getEffectiveDate() != null) {
            syllabus.setEffectiveDate(request.getEffectiveDate());
        }
        
        syllabus.setUpdatedBy(getCurrentUser());
        
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // 3. Gửi thông báo (Gọi hàm đã sửa ở dưới)
        notifyStudentsOnPublish(savedSyllabus);
        
        return mapToResponse(savedSyllabus);
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
        
        if (request.getTeachingAssignmentId() != null) {
            updateTeachingAssignmentStatus(request.getTeachingAssignmentId(), AssignmentStatus.IN_PROGRESS);
            sendNotificationToHodOnCreate(request.getTeachingAssignmentId(), savedSyllabus, currentUser);
        }
        
        return mapToResponse(savedSyllabus);
    }

    @Transactional
    public SyllabusResponse createSyllabusFromAssignment(CreateSyllabusFromAssignmentRequest request) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(request.getTeachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("TeachingAssignment", "id", request.getTeachingAssignmentId()));
        
        User currentUser = getCurrentUser();
        
        if (!assignment.getMainLecturer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Chỉ giảng viên chính mới có thể tạo đề cương");
        }
        
        Optional<SyllabusVersion> existingDraft = syllabusVersionRepository
                .findBySubjectIdAndAcademicTermIdAndStatus(
                        assignment.getSubject().getId(),
                        assignment.getAcademicTerm().getId(),
                        SyllabusStatus.DRAFT
                );
        
        if (existingDraft.isPresent()) {
            log.info("Đã tồn tại syllabus draft cho assignment {}, trả về draft hiện tại", assignment.getId());
            SyllabusVersion existingSyllabus = existingDraft.get();
            createSyllabusCollaboratorsFromAssignment(assignment, existingSyllabus, currentUser);
            linkSyllabusToAssignment(assignment, existingSyllabus);
            return mapToResponse(existingSyllabus);
        }
        
        Subject subject = assignment.getSubject();
        AcademicTerm academicTerm = assignment.getAcademicTerm();
        String versionNo = generateVersionNo(subject.getId(), academicTerm.getId());
        
        log.info("Tạo syllabus draft từ teaching assignment {} cho môn {} - {}",
                assignment.getId(), subject.getCode(), subject.getCurrentNameVi());
        
        SyllabusVersion syllabus = SyllabusVersion.builder()
                .subject(subject)
                .academicTerm(academicTerm)
                .versionNo(versionNo)
                .status(SyllabusStatus.DRAFT)
                .reviewDeadline(assignment.getDeadline().atStartOfDay())
                .snapSubjectCode(subject.getCode())
                .snapSubjectNameVi(subject.getCurrentNameVi())
                .snapSubjectNameEn(subject.getCurrentNameEn())
                .snapCreditCount(subject.getDefaultCredits())
                .theoryHours(subject.getDefaultTheoryHours())
                .practiceHours(subject.getDefaultPracticeHours())
                .selfStudyHours(subject.getDefaultSelfStudyHours())
                .courseType(mapSubjectTypeToCourseType(subject.getSubjectType()))
                .componentType(mapSubjectComponentToComponentType(subject.getComponent()))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .isDeleted(false)
                .build();
        
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        createSyllabusCollaboratorsFromAssignment(assignment, savedSyllabus, currentUser);
        linkSyllabusToAssignment(assignment, savedSyllabus);
        
        log.info("Đã tạo syllabus draft {} cho môn {} (Teaching Assignment: {})",
                savedSyllabus.getId(), subject.getCode(), assignment.getId());
        
        return mapToResponse(savedSyllabus);
    }
    
    private String generateVersionNo(UUID subjectId, UUID academicTermId) {
        long count = syllabusVersionRepository.countBySubjectIdAndAcademicTermId(subjectId, academicTermId);
        return "v" + (count + 1) + ".0";
    }

    @Transactional
    public void deleteSyllabus(UUID id) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // Chỉ cho phép xóa nếu đang ở trạng thái DRAFT
        if (syllabus.getStatus() != SyllabusStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT syllabus can be deleted");
        }

        // Soft delete (Đánh dấu là đã xóa chứ không xóa hẳn khỏi DB)
        syllabus.setIsDeleted(true);
        syllabusVersionRepository.save(syllabus);
    }

    @Transactional
    public SyllabusResponse updateSyllabus(UUID id, SyllabusRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        // Allow update for DRAFT, REJECTED, and REVISION_IN_PROGRESS
        if (!syllabus.getStatus().isEditable()) {
            throw new BadRequestException("Chỉ có thể chỉnh sửa đề cương ở trạng thái Bản nháp, Bị từ chối hoặc Đang chỉnh sửa");
        }

        User currentUser = getCurrentUser();
        SyllabusStatus previousStatus = syllabus.getStatus();
        
        // If saving draft after rejection, create snapshot and increment version
        if (previousStatus == SyllabusStatus.REJECTED) {
            log.info("Creating snapshot for rejected syllabus {} before revision", syllabus.getId());
            
            // Create snapshot of current version before updating
            createSnapshot(syllabus, "BEFORE_REVISION_V" + (syllabus.getVersionNumber() != null ? syllabus.getVersionNumber() : 1));
            
            // Increment version number
            Integer currentVersionNumber = syllabus.getVersionNumber() != null ? syllabus.getVersionNumber() : 1;
            Integer newVersionNumber = currentVersionNumber + 1;
            syllabus.setVersionNumber(newVersionNumber);
            syllabus.setVersionNo("v" + newVersionNumber + ".0");
            
            // Change status to REVISION_IN_PROGRESS
            syllabus.setStatus(SyllabusStatus.REVISION_IN_PROGRESS);
            
            log.info("Incremented version from {} to {} for syllabus {}", 
                     currentVersionNumber, newVersionNumber, syllabus.getId());
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
        syllabus.setUpdatedBy(currentUser);

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // Gửi thông báo cho sinh viên khi đề cương bị cập nhật
        notifyStudentsOnUpdate(savedSyllabus);
        
        return mapToResponse(savedSyllabus);
    }

    @Transactional
    public SyllabusResponse submitSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        // Allow submit for DRAFT, REJECTED, and REVISION_IN_PROGRESS
        if (!syllabus.getStatus().isEditable()) {
            throw new BadRequestException("Chỉ có thể gửi phê duyệt đề cương ở trạng thái Bản nháp, Bị từ chối hoặc Đang chỉnh sửa");
        }
        
        User currentUser = getCurrentUser();
        
        // Single Active Record: Just update status
        syllabus.setStatus(SyllabusStatus.PENDING_HOD);
        syllabus.setUpdatedBy(currentUser);
        syllabus.setSubmittedAt(LocalDateTime.now());
        
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);

        log.info("Submitted syllabus {} (version {}) for approval", 
                 savedSyllabus.getId(), savedSyllabus.getVersionNo());
        
        // Update teaching assignment status to SUBMITTED
        updateTeachingAssignmentStatusBySyllabus(savedSyllabus, AssignmentStatus.SUBMITTED);
        
        sendNotificationToHod(savedSyllabus);
        
        try {
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
            log.error("❌ Failed to send message to AI Queue for Syllabus ID #{}: {}", 
                      savedSyllabus.getId(), e.getMessage());
        }
        
        return mapToResponse(savedSyllabus);
    }

    // ✅ 2. HÀM APPROVE ĐÃ ĐƯỢC CẬP NHẬT LOGIC GỬI THÔNG BÁO
    @Transactional
    public SyllabusResponse approveSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));

        User currentUser = getCurrentUser();
        SyllabusStatus currentStatus = syllabus.getStatus();
        
        SyllabusStatus nextStatus = switch (currentStatus) {
            case PENDING_HOD -> SyllabusStatus.PENDING_AA;
            case PENDING_HOD_REVISION -> SyllabusStatus.PENDING_ADMIN_REPUBLISH; // Post-publication revision approved by HOD
            case PENDING_AA -> SyllabusStatus.PENDING_PRINCIPAL;
            case PENDING_PRINCIPAL -> SyllabusStatus.APPROVED;
            case APPROVED -> SyllabusStatus.PUBLISHED;
            default -> throw new BadRequestException("Cannot approve in current status: " + syllabus.getStatus());
        };

        SyllabusStatus previousStatus = syllabus.getStatus();
        syllabus.setStatus(nextStatus);
        syllabus.setUpdatedBy(currentUser);
        
        // Cập nhật ngày xuất hành nếu là PUBLISHED
        if (nextStatus == SyllabusStatus.PUBLISHED) {
            syllabus.setPublishedAt(java.time.LocalDateTime.now());
        }

        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // Save approval history (Audit Log)
        ActorRoleType actorRole = determineActorRole(currentStatus);
        ApprovalHistory approvalHistory = ApprovalHistory.builder()
                .syllabusVersion(savedSyllabus)
                .actor(currentUser)
                .action(DecisionType.APPROVED)
                .comment(request != null ? request.getComment() : null)
                .actorRole(actorRole)
                .build();
        approvalHistoryRepository.save(approvalHistory);
        
        // When HOD approves (PENDING_HOD -> PENDING_AA), update assignment to COMPLETED
        if (currentStatus == SyllabusStatus.PENDING_HOD && nextStatus == SyllabusStatus.PENDING_AA) {
            updateTeachingAssignmentStatusBySyllabus(savedSyllabus, AssignmentStatus.COMPLETED);
            // Send notification to AA
            sendNotificationToAA(savedSyllabus, currentUser);
        }
        
        // When HOD approves revision (PENDING_HOD_REVISION -> PENDING_ADMIN_REPUBLISH), send notification to Admin
        // DO NOT update teaching assignment as it's already COMPLETED from first publication
        if (currentStatus == SyllabusStatus.PENDING_HOD_REVISION && nextStatus == SyllabusStatus.PENDING_ADMIN_REPUBLISH) {
            sendNotificationToAdminForRepublish(savedSyllabus, currentUser);
        }
        
        // When AA approves (PENDING_AA -> PENDING_PRINCIPAL), send notification to Principal
        if (currentStatus == SyllabusStatus.PENDING_AA && nextStatus == SyllabusStatus.PENDING_PRINCIPAL) {
            sendNotificationToPrincipal(savedSyllabus, currentUser);
        }
        
        // When Principal approves (PENDING_PRINCIPAL -> APPROVED), send notification to Admin
        if (currentStatus == SyllabusStatus.PENDING_PRINCIPAL && nextStatus == SyllabusStatus.APPROVED) {
            sendNotificationToAdmin(savedSyllabus, currentUser);
        }
        
        // Gửi thông báo cho sinh viên dựa vào stage phê duyệt
        if (nextStatus == SyllabusStatus.PUBLISHED) {
            notifyStudentsOnPublish(savedSyllabus);
        } else {
            notifyStudentsOnApprovalStages(savedSyllabus, previousStatus, nextStatus);
        }
        
        return mapToResponse(savedSyllabus);
    }

    @Transactional
    public SyllabusResponse rejectSyllabus(UUID id, SyllabusApprovalRequest request) {
        SyllabusVersion syllabus = syllabusVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syllabus", "id", id));
        
        User currentUser = getCurrentUser();
        SyllabusStatus currentStatus = syllabus.getStatus();
        String rejectionReason = request != null ? request.getComment() : null;
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new BadRequestException("Lý do từ chối không được để trống");
        }
        
        syllabus.setStatus(SyllabusStatus.REJECTED);
        syllabus.setUnpublishReason(rejectionReason);
        syllabus.setUpdatedBy(currentUser);
        SyllabusVersion savedSyllabus = syllabusVersionRepository.save(syllabus);
        
        // Save approval history
        ActorRoleType actorRole = determineActorRole(currentStatus);
        ApprovalHistory approvalHistory = ApprovalHistory.builder()
                .syllabusVersion(savedSyllabus)
                .actor(currentUser)
                .action(DecisionType.REJECTED)
                .comment(rejectionReason)
                .actorRole(actorRole)
                .build();
        approvalHistoryRepository.save(approvalHistory);
        
        // Send notification to primary lecturer
        sendRejectionNotificationToLecturer(savedSyllabus, currentUser, rejectionReason, actorRole);
        
        // If AA rejects, also send notification to HOD
        if (currentStatus == SyllabusStatus.PENDING_AA) {
            sendRejectionNotificationToHOD(savedSyllabus, currentUser, rejectionReason);
        }
        
        // If Principal rejects, send notification to Lecturer + HOD + AA
        if (currentStatus == SyllabusStatus.PENDING_PRINCIPAL) {
            sendRejectionNotificationToAA(savedSyllabus, currentUser, rejectionReason);
        }
        
        return mapToResponse(savedSyllabus);
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

// File: vn/edu/smd/core/service/impl/SyllabusServiceImpl.java

    // Hàm này bị xóa vì đã có getAllSyllabi ở trên xử lý tất cả logic

    @Transactional(readOnly = true)
    public List<SyllabusResponse> getSyllabiBySubject(UUID subjectId) {
        return syllabusVersionRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    

    public byte[] exportSyllabusToPdf(UUID id) {
        return new byte[0];
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        
        // Count syllabi by each status
        for (SyllabusStatus status : SyllabusStatus.values()) {
            long count = syllabusVersionRepository.countByStatusAndIsDeletedFalse(status);
            statistics.put(status.name(), count);
        }
        
        return statistics;
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

    // --- HELPERS (Bao gồm hàm gửi thông báo cho sinh viên) ---
    
    // ✅ HÀM NOTIFICATION KHI XUẤT HÀNH (PUBLIC để có thể gọi từ AdminSyllabusService)
    public void notifyStudentsOnPublish(SyllabusVersion syllabus) {
        try {
            log.info("📌 [NotifyStudents] Bắt đầu tìm trackers cho syllabus: {}", syllabus.getId());
            
            // Tìm tất cả Tracker có syllabusId này (với EAGER fetch để tránh lazy loading issues)
            List<StudentSyllabusTracker> trackers = studentSyllabusTrackerRepository.findBySyllabusId(syllabus.getId());
            
            log.info("📌 [NotifyStudents] Tìm thấy {} trackers cho syllabus: {}", trackers.size(), syllabus.getId());
            
            if (trackers.isEmpty()) {
                log.info("ℹ️  Không có sinh viên nào theo dõi đề cương {}", syllabus.getId());
                return;
            }

            String title = "Đề cương đã xuất hành";
            String message = String.format("Đề cương môn %s - %s đã chính thức được xuất hành.", 
                    syllabus.getSnapSubjectCode(), syllabus.getSnapSubjectNameVi());

            Map<String, Object> payload = new HashMap<>();
            payload.put("actionUrl", "/student/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem ngay");
            payload.put("syllabusId", syllabus.getId().toString());

            List<Notification> notifications = new ArrayList<>();
            
            for (StudentSyllabusTracker tracker : trackers) {
                try {
                    // Lấy user từ tracker (EAGER load từ annotation)
                    User student = tracker.getStudent();
                    
                    if (student == null) {
                        log.warn("⚠️  [NotifyStudents] Student NULL cho tracker: {}", tracker.getId());
                        continue;
                    }
                    
                    log.info("📌 [NotifyStudents] Tạo notification cho student: {} ({})", student.getId(), student.getFullName());
                    
                    Notification notification = Notification.builder()
                            .user(student)
                            .type("SYSTEM") 
                            .title(title)
                            .message(message)
                            .payload(payload)
                            .isRead(false)
                            .createdAt(java.time.LocalDateTime.now())
                            .build();
                    notifications.add(notification);
                } catch (Exception e) {
                    log.error("❌ [NotifyStudents] Lỗi tạo notification cho tracker {}: {}", tracker.getId(), e.getMessage(), e);
                }
            }

            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
                log.info("✅ Đã gửi thông báo xuất hành cho {} sinh viên", notifications.size());
            } else {
                log.warn("⚠️  Không có notification nào được tạo!");
            }

        } catch (Exception e) {
            log.error("❌ Lỗi gửi thông báo xuất hành cho đề cương {}: {}", syllabus.getId(), e.getMessage(), e);
        }
    }
    
    // ✅ HÀM NOTIFICATION KHI CẬP NHẬT ĐỀ CƯƠNG
    private void notifyStudentsOnUpdate(SyllabusVersion syllabus) {
        try {
            log.info("📌 [NotifyUpdate] Bắt đầu tìm trackers cho syllabus: {}", syllabus.getId());
            
            List<StudentSyllabusTracker> trackers = studentSyllabusTrackerRepository.findBySyllabusId(syllabus.getId());
            
            log.info("📌 [NotifyUpdate] Tìm thấy {} trackers", trackers.size());
            
            if (trackers.isEmpty()) {
                log.info("ℹ️  Không có sinh viên nào theo dõi đề cương {}", syllabus.getId());
                return;
            }

            String title = "Đề cương đã được cập nhật";
            String message = String.format("Đề cương môn %s - %s đã được cập nhật với phiên bản mới.", 
                    syllabus.getSnapSubjectCode(), syllabus.getSnapSubjectNameVi());

            Map<String, Object> payload = new HashMap<>();
            payload.put("actionUrl", "/student/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem cập nhật");
            payload.put("syllabusId", syllabus.getId().toString());

            List<Notification> notifications = new ArrayList<>();
            
            for (StudentSyllabusTracker tracker : trackers) {
                try {
                    User student = tracker.getStudent();
                    if (student != null) {
                        Notification notification = Notification.builder()
                                .user(student)
                                .type("SYSTEM") 
                                .title(title)
                                .message(message)
                                .payload(payload)
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                        notifications.add(notification);
                    }
                } catch (Exception e) {
                    log.error("❌ Lỗi tạo notification update cho tracker {}: {}", tracker.getId(), e.getMessage());
                }
            }

            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
                log.info("✅ Đã gửi thông báo cập nhật cho {} sinh viên", notifications.size());
            }

        } catch (Exception e) {
            log.error("❌ Lỗi gửi thông báo cập nhật cho đề cương {}: {}", syllabus.getId(), e.getMessage(), e);
        }
    }
    
    // ✅ HÀM NOTIFICATION CHO CÁC STAGE CỦA CHU KỲ PHÊ DUYỆT
    private void notifyStudentsOnApprovalStages(SyllabusVersion syllabus, SyllabusStatus previousStatus, SyllabusStatus nextStatus) {
        try {
            log.info("📌 [NotifyApprovalStage] Stage: {} → {}", previousStatus, nextStatus);
            
            List<StudentSyllabusTracker> trackers = studentSyllabusTrackerRepository.findBySyllabusId(syllabus.getId());
            
            log.info("📌 [NotifyApprovalStage] Tìm thấy {} trackers", trackers.size());
            
            if (trackers.isEmpty()) {
                return;
            }

            String title = "";
            String message = "";

            // Xác định tiêu đề và nội dung dựa vào trạng thái
            switch (nextStatus) {
                case PENDING_AA:
                    title = "Đề cương đã được Bộ môn phê duyệt";
                    message = String.format("Đề cương môn %s - %s đã được Trưởng bộ môn phê duyệt và đang chờ duyệt từ Phòng Đào tạo.", 
                            syllabus.getSnapSubjectCode(), syllabus.getSnapSubjectNameVi());
                    break;
                case PENDING_PRINCIPAL:
                    title = "Đề cương đã được Phòng Đào tạo thông qua";
                    message = String.format("Đề cương môn %s - %s đã được Phòng Đào tạo thông qua và đang chờ duyệt từ Hiệu trưởng.", 
                            syllabus.getSnapSubjectCode(), syllabus.getSnapSubjectNameVi());
                    break;
                case APPROVED:
                    title = "Đề cương đã được Hiệu trưởng phê duyệt";
                    message = String.format("Đề cương môn %s - %s đã được Hiệu trưởng phê duyệt cuối cùng.", 
                            syllabus.getSnapSubjectCode(), syllabus.getSnapSubjectNameVi());
                    break;
                default:
                    return; // Không gửi thông báo cho các trạng thái khác
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("actionUrl", "/student/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem chi tiết");
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("status", nextStatus.name());

            List<Notification> notifications = new ArrayList<>();
            
            for (StudentSyllabusTracker tracker : trackers) {
                try {
                    User student = tracker.getStudent();
                    if (student != null) {
                        Notification notification = Notification.builder()
                                .user(student)
                                .type("SYSTEM")
                                .title(title)
                                .message(message)
                                .payload(payload)
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                        notifications.add(notification);
                    }
                } catch (Exception e) {
                    log.error("❌ Lỗi tạo notification stage cho tracker {}: {}", tracker.getId(), e.getMessage());
                }
            }

            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
                log.info("✅ Đã gửi thông báo stage {} cho {} sinh viên", nextStatus, notifications.size());
            }

        } catch (Exception e) {
            log.error("❌ Lỗi gửi thông báo stage cho đề cương {}: {}", syllabus.getId(), e.getMessage(), e);
        }
    }
    
    // ✅ HÀM NOTIFICATION KHI GỠ BỎ ĐỀ CƯƠNG
    public void notifyStudentsOnUnpublish(SyllabusVersion syllabus, String reason) {
        try {
            log.info("📌 [NotifyUnpublish] Bắt đầu tìm trackers cho syllabus: {}", syllabus.getId());
            
            List<StudentSyllabusTracker> trackers = studentSyllabusTrackerRepository.findBySyllabusId(syllabus.getId());
            
            log.info("📌 [NotifyUnpublish] Tìm thấy {} trackers", trackers.size());
            
            if (trackers.isEmpty()) {
                log.info("ℹ️  Không có sinh viên nào theo dõi đề cương {}", syllabus.getId());
                return;
            }

            String title = "Đề cương đã bị gỡ bỏ";
            String message = String.format("Đề cương môn %s - %s đã bị gỡ bỏ khỏi hệ thống.%s", 
                    syllabus.getSnapSubjectCode(), 
                    syllabus.getSnapSubjectNameVi(),
                    (reason != null && !reason.isEmpty()) ? "\nLý do: " + reason : "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("reason", reason);
            payload.put("actionUrl", "/student/syllabi");
            payload.put("actionLabel", "Quay lại danh sách");

            List<Notification> notifications = new ArrayList<>();
            
            for (StudentSyllabusTracker tracker : trackers) {
                try {
                    User student = tracker.getStudent();
                    if (student != null) {
                        Notification notification = Notification.builder()
                                .user(student)
                                .type("SYSTEM")
                                .title(title)
                                .message(message)
                                .payload(payload)
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                        notifications.add(notification);
                    }
                } catch (Exception e) {
                    log.error("❌ Lỗi tạo notification unpublish cho tracker {}: {}", tracker.getId(), e.getMessage());
                }
            }

            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
                log.info("✅ Đã gửi thông báo gỡ bỏ cho {} sinh viên", notifications.size());
            }

        } catch (Exception e) {
            log.error("❌ Lỗi gửi thông báo gỡ bỏ cho đề cương {}: {}", syllabus.getId(), e.getMessage(), e);
        }
    }

    private SyllabusResponse mapToResponse(SyllabusVersion syllabus) {
        if (syllabus.getSubject() == null) {
            log.error("Syllabus with ID {} has a null subject. Skipping.", syllabus.getId());
            return null;
        }
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
            String termCode = syllabus.getAcademicTerm().getCode();
            if (termCode != null && termCode.contains("_")) {
                String semesterCode = termCode.split("_")[0];
                response.setSemester(semesterCode);
            }
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
            String code = syllabus.getAcademicTerm().getCode();
            if (code != null && code.startsWith("HK") && code.indexOf('_') > 2) {
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
        response.setPublishedAt(syllabus.getPublishedAt());

        response.setCreatedAt(syllabus.getCreatedAt());
        response.setUpdatedAt(syllabus.getUpdatedAt());
// --- Code lấy CLO (Load from DB, fallback to Content JSON) ---
        List<CLO> clos = cloRepository.findBySyllabusVersionId(syllabus.getId());
        Map<UUID, String> cloCodeMap = new HashMap<>();
        
        // If no CLOs in database, try to extract from content JSONB (for newly created syllabi)
        if (clos.isEmpty() && syllabus.getContent() != null && syllabus.getContent().containsKey("clos")) {
            response.setClos(extractClosFromContent(syllabus.getContent()));
        } else {
            // Use CLOs from database tables
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
        }

        // --- Code lấy PLO Mappings ---
        List<SyllabusResponse.CLOPLOMappingResponse> ploMappings = new ArrayList<>();
        
        // First try to load from database tables
        for (CLO clo : clos) {
            List<CloPlOMapping> mappings = cloPlOMappingRepository.findByCloId(clo.getId());
            for (CloPlOMapping mapping : mappings) {
                SyllabusResponse.CLOPLOMappingResponse mappingResponse = new SyllabusResponse.CLOPLOMappingResponse();
                mappingResponse.setCloCode(clo.getCode());
                PLO plo = mapping.getPlo();
                mappingResponse.setPloCode(plo.getCode());
                mappingResponse.setContributionLevel(mapping.getMappingLevel());
                ploMappings.add(mappingResponse);
            }
        }
        
        // If no mappings in database, try to extract from content JSONB
        if (ploMappings.isEmpty() && syllabus.getContent() != null && syllabus.getContent().containsKey("ploMappings")) {
            ploMappings = extractPloMappingsFromContent(syllabus.getContent());
        }
        
        response.setPloMappings(ploMappings);

        // Load Assessment Schemes from database tables
        List<AssessmentScheme> assessments = assessmentSchemeRepository.findBySyllabusVersionId(syllabus.getId());
        
        // If no assessments in database, try to extract from content JSONB (for newly created syllabi)
        if (assessments.isEmpty() && syllabus.getContent() != null && syllabus.getContent().containsKey("assessmentMethods")) {
            response.setAssessmentMethods(extractAssessmentMethodsFromContent(syllabus.getContent()));
        } else {
            // Use assessments from database tables
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
        }

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
    
    private void sendNotificationToHod(SyllabusVersion syllabus) {
        try {
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                        syllabus.getSubject().getId(),
                        syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isEmpty()) return;
            
            TeachingAssignment assignment = assignmentOpt.get();
            User hod = assignment.getAssignedBy();
            User lecturer = syllabus.getCreatedBy();
            
            if (hod == null) return;
            
            String title = String.format("[Đề cương mới] %s - %s", 
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
            
            String message = String.format(
                "Giảng viên %s đã nộp đề cương môn học %s (%s) - %s để bạn phê duyệt.",
                lecturer != null ? lecturer.getFullName() : "Unknown",
                syllabus.getSnapSubjectNameVi(),
                syllabus.getSnapSubjectCode(),
                syllabus.getAcademicTerm() != null ? syllabus.getAcademicTerm().getName() : ""
            );
            
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
                    .relatedEntityType("SYLLABUS")
                    .relatedEntityId(syllabus.getId())
                    .build();
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to send notification to HOD: {}", e.getMessage());
        }
    }
    
    private void sendNotificationToHodOnCreate(UUID teachingAssignmentId, SyllabusVersion syllabus, User lecturer) {
        try {
            TeachingAssignment assignment = teachingAssignmentRepository.findById(teachingAssignmentId)
                    .orElse(null);
            
            if (assignment == null) return;
            
            User hod = assignment.getAssignedBy();
            if (hod == null) return;
            
            String title = String.format("[Đang biên soạn] %s - %s", 
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi());
            
            String message = String.format(
                "Giảng viên %s đã bắt đầu biên soạn đề cương môn học %s (%s).",
                lecturer.getFullName(),
                syllabus.getSnapSubjectNameVi(),
                syllabus.getSnapSubjectCode()
            );
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
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
        } catch (Exception e) {
            log.error("Failed to send progress notification to HOD: {}", e.getMessage());
        }
    }
    
    private void createSyllabusCollaboratorsFromAssignment(TeachingAssignment assignment, 
                                                           SyllabusVersion syllabus, 
                                                           User mainLecturer) {
        try {
            List<TeachingAssignmentCollaborator> assignmentCollaborators = 
                    teachingAssignmentCollaboratorRepository.findByAssignmentId(assignment.getId());
            
            for (TeachingAssignmentCollaborator assignmentCollab : assignmentCollaborators) {
                Optional<SyllabusCollaborator> existing = syllabusCollaboratorRepository
                        .findBySyllabusVersionIdAndUserId(syllabus.getId(), assignmentCollab.getLecturer().getId());
                
                if (existing.isEmpty()) {
                    SyllabusCollaborator syllabusCollab = SyllabusCollaborator.builder()
                            .syllabusVersion(syllabus)
                            .user(assignmentCollab.getLecturer())
                            .role(vn.edu.smd.shared.enums.CollaboratorRole.EDITOR)
                            .build();
                    
                    syllabusCollaboratorRepository.save(syllabusCollab);
                }
            }
        } catch (Exception e) {
            log.error("Failed to create collaborators: {}", e.getMessage());
        }
    }
    
    private void linkSyllabusToAssignment(TeachingAssignment assignment, SyllabusVersion syllabus) {
        try {
            assignment.setSyllabusVersion(syllabus);
            if (assignment.getStatus() == AssignmentStatus.PENDING) {
                assignment.setStatus(AssignmentStatus.IN_PROGRESS);
            }
            teachingAssignmentRepository.save(assignment);
        } catch (Exception e) {
            log.error("Failed to link syllabus: {}", e.getMessage());
        }
    }
    
    private void updateTeachingAssignmentStatus(UUID assignmentId, AssignmentStatus newStatus) {
        try {
            TeachingAssignment assignment = teachingAssignmentRepository.findById(assignmentId)
                    .orElse(null);
            if (assignment != null) {
                assignment.setStatus(newStatus);
                teachingAssignmentRepository.save(assignment);
            }
        } catch (Exception e) {
            log.error("Failed to update status: {}", e.getMessage());
        }
    }
    
    private void updateTeachingAssignmentStatusBySyllabus(SyllabusVersion syllabus, AssignmentStatus newStatus) {
        try {
            if (syllabus.getSubject() == null || syllabus.getAcademicTerm() == null) return;
            
            // Find teaching assignment by subject and term
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                            syllabus.getSubject().getId(),
                            syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isPresent()) {
                TeachingAssignment assignment = assignmentOpt.get();
                assignment.setStatus(newStatus);
                teachingAssignmentRepository.save(assignment);
                log.info("Updated teaching assignment {} status to {}", assignment.getId(), newStatus);
            }
        } catch (Exception e) {
            log.error("Failed to update teaching assignment status: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Extract CLOs from content JSONB
     */
    @SuppressWarnings("unchecked")
    private List<SyllabusResponse.CLOResponse> extractClosFromContent(Map<String, Object> content) {
        List<SyllabusResponse.CLOResponse> closList = new ArrayList<>();
        
        try {
            Object closObj = content.get("clos");
            if (closObj instanceof List) {
                List<Map<String, Object>> clos = (List<Map<String, Object>>) closObj;
                
                for (Map<String, Object> clo : clos) {
                    SyllabusResponse.CLOResponse cloResponse = new SyllabusResponse.CLOResponse();
                    cloResponse.setCode((String) clo.get("code"));
                    cloResponse.setDescription((String) clo.get("description"));
                    cloResponse.setBloomLevel((String) clo.get("bloomLevel"));
                    
                    // Handle weight as either Number or BigDecimal
                    Object weightObj = clo.get("weight");
                    if (weightObj instanceof Number) {
                        cloResponse.setWeight(new BigDecimal(weightObj.toString()));
                    }
                    
                    closList.add(cloResponse);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract CLOs from content: {}", e.getMessage());
        }
        
        return closList;
    }
    
    /**
     * Extract PLO mappings from content JSONB
     */
    @SuppressWarnings("unchecked")
    private List<SyllabusResponse.CLOPLOMappingResponse> extractPloMappingsFromContent(Map<String, Object> content) {
        List<SyllabusResponse.CLOPLOMappingResponse> mappings = new ArrayList<>();
        
        try {
            Object mappingsObj = content.get("ploMappings");
            if (mappingsObj instanceof List) {
                List<Map<String, Object>> ploMappings = (List<Map<String, Object>>) mappingsObj;
                
                for (Map<String, Object> mapping : ploMappings) {
                    SyllabusResponse.CLOPLOMappingResponse mappingResponse = new SyllabusResponse.CLOPLOMappingResponse();
                    mappingResponse.setCloCode((String) mapping.get("cloCode"));
                    mappingResponse.setPloCode((String) mapping.get("ploCode"));
                    mappingResponse.setContributionLevel((String) mapping.get("contributionLevel"));
                    mappings.add(mappingResponse);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract PLO mappings from content: {}", e.getMessage());
        }
        
        return mappings;
    }
    
    /**
     * Extract assessment methods from content JSONB
     */
    @SuppressWarnings("unchecked")
    private List<SyllabusResponse.AssessmentResponse> extractAssessmentMethodsFromContent(Map<String, Object> content) {
        List<SyllabusResponse.AssessmentResponse> assessments = new ArrayList<>();
        
        try {
            Object assessmentsObj = content.get("assessmentMethods");
            if (assessmentsObj instanceof List) {
                List<Map<String, Object>> assessmentMethods = (List<Map<String, Object>>) assessmentsObj;
                
                for (Map<String, Object> assessment : assessmentMethods) {
                    SyllabusResponse.AssessmentResponse assessmentResponse = new SyllabusResponse.AssessmentResponse();
                    assessmentResponse.setMethod((String) assessment.get("method"));
                    assessmentResponse.setForm((String) assessment.get("form"));
                    assessmentResponse.setCriteria((String) assessment.get("criteria"));
                    
                    // Handle weight as either Number or BigDecimal
                    Object weightObj = assessment.get("weight");
                    if (weightObj instanceof Number) {
                        assessmentResponse.setWeight(new BigDecimal(weightObj.toString()));
                    }
                    
                    // Extract CLO codes
                    Object closObj = assessment.get("clos");
                    if (closObj instanceof List) {
                        assessmentResponse.setClos((List<String>) closObj);
                    }
                    
                    assessments.add(assessmentResponse);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract assessment methods from content: {}", e.getMessage());
        }
        
        return assessments;
    }
    
    // Helper method to find and update assignment status continues below...
    
    private void completeUpdateTeachingAssignmentStatusBySyllabus(SyllabusVersion syllabus, AssignmentStatus newStatus) {
        try {
            
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                        syllabus.getSubject().getId(),
                        syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isPresent()) {
                TeachingAssignment assignment = assignmentOpt.get();
                assignment.setStatus(newStatus);
                teachingAssignmentRepository.save(assignment);
            }
        } catch (Exception e) {
            log.error("Failed to update status: {}", e.getMessage());
        }
    }
    
    /**
     * Auto-suggest PLO mappings based on CLO descriptions and Bloom's levels
     * Delegates to PloMappingService for intelligent keyword matching
     */
    public List<Map<String, Object>> suggestPloMappings(List<Map<String, Object>> clos) {
        log.info("Suggesting PLO mappings for {} CLOs", clos.size());
        return ploMappingService.suggestPloMappings(clos);
    }
    
    /**
     * Determine actor role based on current syllabus status
     */
    private ActorRoleType determineActorRole(SyllabusStatus status) {
        return switch (status) {
            case PENDING_HOD, PENDING_HOD_REVISION -> ActorRoleType.HOD;
            case PENDING_AA -> ActorRoleType.AA;
            case PENDING_PRINCIPAL -> ActorRoleType.PRINCIPAL;
            case APPROVED, PUBLISHED -> ActorRoleType.ADMIN;
            default -> ActorRoleType.LECTURER;
        };
    }
    
    /**
     * Send rejection notification to primary lecturer
     */
    private void sendRejectionNotificationToLecturer(SyllabusVersion syllabus, User rejector, 
                                                      String rejectionReason, ActorRoleType rejectorRole) {
        try {
            User primaryLecturer = syllabus.getCreatedBy();
            
            if (primaryLecturer == null) {
                log.warn("Primary lecturer not found for syllabus {} - cannot send rejection notification", 
                         syllabus.getId());
                return;
            }
            
            // Build notification title
            String title = String.format("[Yêu cầu chỉnh sửa] Đề cương môn %s bị từ chối phê duyệt", 
                syllabus.getSnapSubjectCode());
            
            // Build notification message
            String rejectorTitle = rejectorRole != null ? rejectorRole.getDisplayName() : "Người phê duyệt";
            String message = String.format(
                "%s %s đã từ chối phê duyệt đề cương môn %s.\n\n" +
                "Lý do: %s\n\n" +
                "Vui lòng chỉnh sửa đề cương theo yêu cầu và gửi lại để phê duyệt.",
                rejectorTitle,
                rejector.getFullName(),
                syllabus.getSnapSubjectNameVi(),
                rejectionReason
            );
            
            // Create payload with action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/lecturer/syllabi/edit/" + syllabus.getId());
            payload.put("actionLabel", "Chỉnh sửa ngay");
            payload.put("rejectionReason", rejectionReason);
            payload.put("rejectorRole", rejectorRole != null ? rejectorRole.name() : null);
            
            Notification notification = Notification.builder()
                    .user(primaryLecturer)
                    .title(title)
                    .message(message)
                    .type("SYLLABUS_REJECTED")
                    .payload(payload)
                    .isRead(false)
                    .relatedEntityType("SYLLABUS")
                    .relatedEntityId(syllabus.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Sent rejection notification to lecturer {} for syllabus {}", 
                     primaryLecturer.getEmail(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send rejection notification for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification to Academic Affairs (AA) when HOD approves syllabus
     */
    private void sendNotificationToAA(SyllabusVersion syllabus, User hod) {
        try {
            // Find all users with AA role
            List<User> aaUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUserRoles() != null && 
                            user.getUserRoles().stream()
                                .anyMatch(ur -> ur.getRole() != null && "AA".equals(ur.getRole().getCode())))
                    .collect(Collectors.toList());
            
            if (aaUsers.isEmpty()) {
                log.warn("No AA users found - cannot send notification for syllabus {}", syllabus.getId());
                return;
            }
            
            User primaryLecturer = syllabus.getCreatedBy();
            String lecturerName = primaryLecturer != null ? primaryLecturer.getFullName() : "Không xác định";
            
            // Get department name
            String departmentName = "Không xác định";
            if (primaryLecturer != null && primaryLecturer.getDepartment() != null) {
                departmentName = primaryLecturer.getDepartment().getName();
            }
            
            // Build notification title
            String title = String.format("[Chờ duyệt] Đề cương môn học đã được Bộ môn thông qua: %s", 
                syllabus.getSnapSubjectCode());
            
            // Build notification message
            String message = String.format(
                "Chào bộ phận Phòng Đào tạo,\n\n" +
                "Đề cương môn học sau đây đã được Trưởng bộ môn %s phê duyệt về mặt nội dung và gửi đến Phòng Đào tạo rà soát:\n\n" +
                "Môn học: %s - %s\n" +
                "Bộ môn: %s\n" +
                "Giảng viên biên soạn: %s\n" +
                "Thời gian HoD duyệt: %s\n\n" +
                "Vui lòng thực hiện rà soát quy chuẩn và phê duyệt để phục vụ xuất bản đề cương.",
                hod.getFullName(),
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi(),
                departmentName,
                lecturerName,
                java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(java.time.LocalDateTime.now())
            );
            
            // Create payload with action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/admin/aa-syllabus-review");
            payload.put("actionLabel", "Kiểm duyệt ngay");
            payload.put("hodName", hod.getFullName());
            payload.put("departmentName", departmentName);
            
            // Send notification to all AA users
            for (User aaUser : aaUsers) {
                Notification notification = Notification.builder()
                        .user(aaUser)
                        .title(title)
                        .message(message)
                        .type("SYLLABUS_AA_REVIEW")
                        .payload(payload)
                        .isRead(false)
                        .relatedEntityType("SYLLABUS")
                        .relatedEntityId(syllabus.getId())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Sent AA review notification to {} AA users for syllabus {}", 
                     aaUsers.size(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send AA notification for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification to Principal when AA approves syllabus
     */
    private void sendNotificationToPrincipal(SyllabusVersion syllabus, User aaUser) {
        try {
            // Find all users with PRINCIPAL role
            List<User> principalUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUserRoles() != null && 
                            user.getUserRoles().stream()
                                .anyMatch(ur -> ur.getRole() != null && "PRINCIPAL".equals(ur.getRole().getCode())))
                    .collect(Collectors.toList());
            
            if (principalUsers.isEmpty()) {
                log.warn("No PRINCIPAL users found - cannot send notification for syllabus {}", syllabus.getId());
                return;
            }
            
            User primaryLecturer = syllabus.getCreatedBy();
            String lecturerName = primaryLecturer != null ? primaryLecturer.getFullName() : "Không xác định";
            
            // Get department name
            String departmentName = "Không xác định";
            if (primaryLecturer != null && primaryLecturer.getDepartment() != null) {
                departmentName = primaryLecturer.getDepartment().getName();
            }
            
            // Build notification title
            String title = String.format("[Chờ duyệt] Đề cương môn học đã được Phòng Đào tạo thông qua: %s", 
                syllabus.getSnapSubjectCode());
            
            // Build notification message
            String message = String.format(
                "Chào Hiệu trưởng,\n\n" +
                "Đề cương môn học sau đây đã được Phòng Đào tạo phê duyệt và gửi đến Hiệu trưởng để phê duyệt cuối cùng:\n\n" +
                "Môn học: %s - %s\n" +
                "Bộ môn: %s\n" +
                "Giảng viên biên soạn: %s\n" +
                "Thời gian Phòng ĐT duyệt: %s\n\n" +
                "Vui lòng xem xét và phê duyệt để đề cương có thể được xuất bản.",
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi(),
                departmentName,
                lecturerName,
                java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(java.time.LocalDateTime.now())
            );
            
            // Create payload with action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/principal/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem và duyệt");
            payload.put("aaUserName", aaUser.getFullName());
            payload.put("departmentName", departmentName);
            
            // Send notification to all Principal users
            for (User principal : principalUsers) {
                Notification notification = Notification.builder()
                        .user(principal)
                        .title(title)
                        .message(message)
                        .type("SYLLABUS_PRINCIPAL_REVIEW")
                        .payload(payload)
                        .isRead(false)
                        .relatedEntityType("SYLLABUS")
                        .relatedEntityId(syllabus.getId())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Sent Principal review notification to {} Principal users for syllabus {}", 
                     principalUsers.size(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send Principal notification for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send rejection notification to HOD when AA rejects syllabus
     */
    private void sendRejectionNotificationToHOD(SyllabusVersion syllabus, User rejector, String rejectionReason) {
        try {
            // Find teaching assignment to get HOD
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
            
            if (hod == null) {
                log.warn("HOD not found for teaching assignment {} - cannot send notification", 
                         assignment.getId());
                return;
            }
            
            User primaryLecturer = syllabus.getCreatedBy();
            String lecturerName = primaryLecturer != null ? primaryLecturer.getFullName() : "Không xác định";
            
            // Build notification title
            String title = String.format("[Thông báo] Đề cương môn %s bị Phòng Đào tạo từ chối", 
                syllabus.getSnapSubjectCode());
            
            // Build notification message
            String message = String.format(
                "Kính gửi Trưởng bộ môn,\n\n" +
                "Phòng Đào tạo đã từ chối phê duyệt đề cương môn %s do giảng viên %s biên soạn.\n\n" +
                "Lý do: %s\n\n" +
                "Đề cương sẽ được trả về cho giảng viên để chỉnh sửa. Vui lòng hỗ trợ giảng viên hoàn thiện đề cương.",
                syllabus.getSnapSubjectNameVi(),
                lecturerName,
                rejectionReason
            );
            
            // Create payload with action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("actionUrl", "/hod/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xem chi tiết");
            payload.put("rejectionReason", rejectionReason);
            payload.put("lecturerName", lecturerName);
            
            Notification notification = Notification.builder()
                    .user(hod)
                    .title(title)
                    .message(message)
                    .type("SYLLABUS_REJECTED_NOTIFICATION")
                    .payload(payload)
                    .isRead(false)
                    .relatedEntityType("SYLLABUS")
                    .relatedEntityId(syllabus.getId())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Sent rejection notification to HOD {} for syllabus {}", 
                     hod.getEmail(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send rejection notification to HOD for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification to Admin for republishing after HOD approved revision
     */
    private void sendNotificationToAdminForRepublish(SyllabusVersion syllabus, User hod) {
        log.info("Sending republish notification to Admins for syllabus {}", syllabus.getId());
        
        // Find all admins
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getCode().equals("ADMIN")))
                .collect(Collectors.toList());
        
        if (admins.isEmpty()) {
            log.warn("No ADMIN users found for republish notification");
            return;
        }
        
        for (User admin : admins) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("syllabusCode", syllabus.getSnapSubjectCode());
            payload.put("syllabusName", syllabus.getSnapSubjectNameVi());
            payload.put("hodName", hod.getFullName());
            payload.put("actionUrl", "/admin/syllabi/" + syllabus.getId() + "/republish");
            payload.put("actionLabel", "Xuất bản lại");
            payload.put("priority", "MEDIUM");
            
            try {
                Notification notification = Notification.builder()
                        .user(admin)
                        .title("[Đã duyệt] Đề cương " + syllabus.getSnapSubjectCode() + " chờ xuất bản lại")
                        .message("Trưởng bộ môn đã phê duyệt phiên bản chỉnh sửa. Vui lòng xuất bản lại.")
                        .type(NotificationType.PUBLICATION.name())
                        .payload(payload)
                        .isRead(false)
                        .relatedEntityType("SYLLABUS_VERSION")
                        .relatedEntityId(syllabus.getId())
                        .build();
                
                notificationRepository.save(notification);
                log.info("Sent republish notification to admin: {}", admin.getFullName());
            } catch (Exception e) {
                log.error("Failed to send republish notification to admin {}: {}", admin.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Send notification to Admin when Principal approves syllabus
     */
    private void sendNotificationToAdmin(SyllabusVersion syllabus, User principal) {
        try {
            // Find all users with ADMIN role
            List<User> adminUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUserRoles() != null && 
                            user.getUserRoles().stream()
                                .anyMatch(ur -> ur.getRole() != null && "ADMIN".equals(ur.getRole().getCode())))
                    .collect(Collectors.toList());
            
            if (adminUsers.isEmpty()) {
                log.warn("No ADMIN users found - cannot send notification for syllabus {}", syllabus.getId());
                return;
            }
            
            User primaryLecturer = syllabus.getCreatedBy();
            String lecturerName = primaryLecturer != null ? primaryLecturer.getFullName() : "Không xác định";
            
            // Get department name
            String departmentName = "Không xác định";
            if (primaryLecturer != null && primaryLecturer.getDepartment() != null) {
                departmentName = primaryLecturer.getDepartment().getName();
            }
            
            // Build notification title
            String title = String.format("[Chờ xuất bản] Đề cương môn học đã được Hiệu trưởng phê duyệt: %s", 
                syllabus.getSnapSubjectCode());
            
            // Build notification message
            String message = String.format(
                "Chào Admin,\n\n" +
                "Đề cương môn học sau đây đã được Hiệu trưởng phê duyệt và sẵn sàng xuất bản:\n\n" +
                "Môn học: %s - %s\n" +
                "Bộ môn: %s\n" +
                "Giảng viên biên soạn: %s\n" +
                "Phiên bản: %s\n" +
                "Thời gian phê duyệt: %s\n\n" +
                "Vui lòng xuất bản đề cương để sinh viên có thể truy cập.",
                syllabus.getSnapSubjectCode(),
                syllabus.getSnapSubjectNameVi(),
                departmentName,
                lecturerName,
                syllabus.getVersionNo(),
                java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(LocalDateTime.now())
            );
            
            // Create payload with action URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", syllabus.getId().toString());
            payload.put("subjectCode", syllabus.getSnapSubjectCode());
            payload.put("versionNo", syllabus.getVersionNo());
            payload.put("actionUrl", "/admin/syllabi/" + syllabus.getId());
            payload.put("actionLabel", "Xuất bản");
            payload.put("principalName", principal.getFullName());
            payload.put("departmentName", departmentName);
            
            // Send notification to all Admin users
            for (User admin : adminUsers) {
                Notification notification = Notification.builder()
                        .user(admin)
                        .title(title)
                        .message(message)
                        .type("SYLLABUS_ADMIN_PUBLISH")
                        .payload(payload)
                        .isRead(false)
                        .relatedEntityType("SYLLABUS")
                        .relatedEntityId(syllabus.getId())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Sent Admin publish notification to {} Admin users for syllabus {}", 
                     adminUsers.size(), syllabus.getId());
            
        } catch (Exception e) {
            log.error("Failed to send Admin notification for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send rejection notification to AA and HOD when Principal rejects syllabus
     */
    private void sendRejectionNotificationToAA(SyllabusVersion syllabus, User principal, String rejectionReason) {
        try {
            User primaryLecturer = syllabus.getCreatedBy();
            String lecturerName = primaryLecturer != null ? primaryLecturer.getFullName() : "Không xác định";
            
            // 1. Find and notify all AA users
            List<User> aaUsers = userRepository.findAll().stream()
                    .filter(user -> user.getUserRoles() != null && 
                            user.getUserRoles().stream()
                                .anyMatch(ur -> ur.getRole() != null && "ACADEMIC_AFFAIR".equals(ur.getRole().getCode())))
                    .collect(Collectors.toList());
            
            if (!aaUsers.isEmpty()) {
                String aaTitle = String.format("[Thông báo] Đề cương môn %s bị Hiệu trưởng từ chối", 
                    syllabus.getSnapSubjectCode());
                
                String aaMessage = String.format(
                    "Kính gửi Phòng Đào tạo,\n\n" +
                    "Hiệu trưởng đã từ chối phê duyệt đề cương môn %s do giảng viên %s biên soạn.\n\n" +
                    "Lý do: %s\n\n" +
                    "Đề cương sẽ được trả về cho giảng viên để chỉnh sửa.",
                    syllabus.getSnapSubjectNameVi(),
                    lecturerName,
                    rejectionReason
                );
                
                Map<String, Object> aaPayload = new HashMap<>();
                aaPayload.put("syllabusId", syllabus.getId().toString());
                aaPayload.put("subjectCode", syllabus.getSnapSubjectCode());
                aaPayload.put("actionUrl", "/aa/syllabi/" + syllabus.getId());
                aaPayload.put("actionLabel", "Xem chi tiết");
                aaPayload.put("rejectionReason", rejectionReason);
                aaPayload.put("lecturerName", lecturerName);
                
                for (User aaUser : aaUsers) {
                    Notification notification = Notification.builder()
                            .user(aaUser)
                            .title(aaTitle)
                            .message(aaMessage)
                            .type("SYLLABUS_REJECTED_NOTIFICATION")
                            .payload(aaPayload)
                            .isRead(false)
                            .build();
                    
                    notificationRepository.save(notification);
                }
                
                log.info("Sent rejection notification to {} AA users for syllabus {}", 
                         aaUsers.size(), syllabus.getId());
            }
            
            // 2. Find and notify HOD
            Optional<TeachingAssignment> assignmentOpt = teachingAssignmentRepository
                    .findBySubjectIdAndAcademicTermId(
                        syllabus.getSubject().getId(),
                        syllabus.getAcademicTerm().getId()
                    );
            
            if (assignmentOpt.isPresent()) {
                TeachingAssignment assignment = assignmentOpt.get();
                User hod = assignment.getAssignedBy();
                
                if (hod != null) {
                    String hodTitle = String.format("[Thông báo] Đề cương môn %s bị Hiệu trưởng từ chối", 
                        syllabus.getSnapSubjectCode());
                    
                    String hodMessage = String.format(
                        "Kính gửi Trưởng bộ môn,\n\n" +
                        "Hiệu trưởng đã từ chối phê duyệt đề cương môn %s do giảng viên %s biên soạn.\n\n" +
                        "Lý do: %s\n\n" +
                        "Đề cương sẽ được trả về cho giảng viên để chỉnh sửa. Vui lòng hỗ trợ giảng viên hoàn thiện đề cương.",
                        syllabus.getSnapSubjectNameVi(),
                        lecturerName,
                        rejectionReason
                    );
                    
                    Map<String, Object> hodPayload = new HashMap<>();
                    hodPayload.put("syllabusId", syllabus.getId().toString());
                    hodPayload.put("subjectCode", syllabus.getSnapSubjectCode());
                    hodPayload.put("actionUrl", "/hod/syllabi/" + syllabus.getId());
                    hodPayload.put("actionLabel", "Xem chi tiết");
                    hodPayload.put("rejectionReason", rejectionReason);
                    hodPayload.put("lecturerName", lecturerName);
                    
                    Notification hodNotification = Notification.builder()
                            .user(hod)
                            .title(hodTitle)
                            .message(hodMessage)
                            .type("SYLLABUS_REJECTED_NOTIFICATION")
                            .payload(hodPayload)
                            .isRead(false)
                            .build();
                    
                    notificationRepository.save(hodNotification);
                    
                    log.info("Sent rejection notification to HOD {} for syllabus {}", 
                             hod.getEmail(), syllabus.getId());
                }
            }
            
            // 3. Notification to Lecturer is already sent by sendRejectionNotificationToLecturer
            
        } catch (Exception e) {
            log.error("Failed to send rejection notification to AA/HOD for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Create snapshot of current syllabus version before modification
     * Implements Single Active Record pattern
     */
    private void createSnapshot(SyllabusVersion syllabus, String reason) {
        try {
            Integer versionNumber = syllabus.getVersionNumber() != null ? syllabus.getVersionNumber() : 1;
            
            SyllabusVersionHistory snapshot = SyllabusVersionHistory.builder()
                    .syllabusVersion(syllabus)
                    .versionNumber(versionNumber)
                    .versionNo(syllabus.getVersionNo())
                    .status(syllabus.getStatus())
                    // Copy full content
                    .content(syllabus.getContent())
                    .keywords(syllabus.getKeywords())
                    .description(syllabus.getDescription())
                    .objectives(syllabus.getObjectives())
                    .studentTasks(syllabus.getStudentTasks())
                    .studentDuties(syllabus.getStudentDuties())
                    // Copy snapshot metadata
                    .snapSubjectCode(syllabus.getSnapSubjectCode())
                    .snapSubjectNameVi(syllabus.getSnapSubjectNameVi())
                    .snapSubjectNameEn(syllabus.getSnapSubjectNameEn())
                    .snapCreditCount(syllabus.getSnapCreditCount())
                    // Copy course details
                    .courseType(syllabus.getCourseType())
                    .componentType(syllabus.getComponentType())
                    .theoryHours(syllabus.getTheoryHours())
                    .practiceHours(syllabus.getPracticeHours())
                    .selfStudyHours(syllabus.getSelfStudyHours())
                    // Set audit fields
                    .createdBy(getCurrentUser())
                    .snapshotReason(reason)
                    .build();
            
            syllabusVersionHistoryRepository.save(snapshot);
            
            log.info("Created snapshot for syllabus {} version {} with reason: {}", 
                     syllabus.getId(), versionNumber, reason);
            
        } catch (Exception e) {
            log.error("Failed to create snapshot for syllabus {}: {}", 
                      syllabus.getId(), e.getMessage(), e);
            // Don't throw exception - snapshot failure shouldn't block main operation
        }
    }
    
    // Helper methods to map Subject enums to Syllabus enums
    private vn.edu.smd.shared.enums.CourseType mapSubjectTypeToCourseType(vn.edu.smd.shared.enums.SubjectType subjectType) {
        if (subjectType == null) {
            return vn.edu.smd.shared.enums.CourseType.REQUIRED;
        }
        return switch (subjectType) {
            case REQUIRED -> vn.edu.smd.shared.enums.CourseType.REQUIRED;
            case ELECTIVE -> vn.edu.smd.shared.enums.CourseType.ELECTIVE;
            default -> vn.edu.smd.shared.enums.CourseType.REQUIRED;
        };
    }
    
    private vn.edu.smd.shared.enums.ComponentType mapSubjectComponentToComponentType(vn.edu.smd.shared.enums.SubjectComponent component) {
        if (component == null) {
            return vn.edu.smd.shared.enums.ComponentType.MAJOR;
        }
        return switch (component) {
            case THEORY -> vn.edu.smd.shared.enums.ComponentType.MAJOR;
            case PRACTICE -> vn.edu.smd.shared.enums.ComponentType.MAJOR;
            case BOTH -> vn.edu.smd.shared.enums.ComponentType.MAJOR;
            default -> vn.edu.smd.shared.enums.ComponentType.MAJOR;
        };
    }
}
