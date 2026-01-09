package vn.edu.smd.core.module.subject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.AcademicTerm;
import vn.edu.smd.core.entity.Curriculum;
import vn.edu.smd.core.entity.Department;
import vn.edu.smd.core.entity.Notification;
import vn.edu.smd.core.entity.Subject;
import vn.edu.smd.core.entity.SubjectRelationship;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteRequest;
import vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse;
import vn.edu.smd.core.module.subject.dto.SubjectRequest;
import vn.edu.smd.core.module.subject.dto.SubjectResponse;
import vn.edu.smd.core.module.syllabus.dto.SyllabusResponse;
import vn.edu.smd.core.repository.AcademicTermRepository;
import vn.edu.smd.core.repository.CurriculumRepository;
import vn.edu.smd.core.repository.DepartmentRepository;
import vn.edu.smd.core.repository.NotificationRepository;
import vn.edu.smd.core.repository.SubjectRelationshipRepository;
import vn.edu.smd.core.repository.SubjectRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.SubjectComponent;
import vn.edu.smd.shared.enums.SubjectRelationType;
import vn.edu.smd.shared.enums.SubjectType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final CurriculumRepository curriculumRepository;
    private final SubjectRelationshipRepository relationshipRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final AcademicTermRepository academicTermRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    
    private static final int ASSIGNMENT_DEADLINE_DAYS = 7; // Hạn chốt phân công: 7 ngày sau khi tạo môn

    @Transactional(readOnly = true)
    public Page<SubjectResponse> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAllWithDepartmentAndFaculty();
        
        // Load all prerequisites in one query
        List<UUID> subjectIds = subjects.stream().map(Subject::getId).collect(Collectors.toList());
        System.out.println("DEBUG: Loading prerequisites for " + subjectIds.size() + " subjects");
        
        List<SubjectRelationship> allPrerequisites = Collections.emptyList();
        if (!subjectIds.isEmpty()) {
            try {
                allPrerequisites = relationshipRepository
                        .findBySubjectIdsAndTypeWithRelatedSubject(subjectIds, SubjectRelationType.PREREQUISITE);
                System.out.println("DEBUG: Found " + allPrerequisites.size() + " prerequisites");
            } catch (Exception e) {
                System.out.println("DEBUG: Error loading prerequisites: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Group prerequisites by subject ID
        Map<UUID, String> prerequisitesMap = allPrerequisites.stream()
                .collect(Collectors.groupingBy(
                        rel -> rel.getSubject().getId(),
                        Collectors.mapping(
                                rel -> rel.getRelatedSubject().getCode(),
                                Collectors.joining(", ")
                        )
                ));
        
        return subjects.stream()
                .map(subject -> mapToResponse(subject, prerequisitesMap.get(subject.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        return mapToResponse(subject);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "code", code));
        return mapToResponse(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsByDepartment(UUID departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return subjectRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubjectResponse> getSubjectsByCurriculum(UUID curriculumId) {
        if (!curriculumRepository.existsById(curriculumId)) {
            throw new ResourceNotFoundException("Curriculum", "id", curriculumId);
        }
        return subjectRepository.findByCurriculumId(curriculumId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubjectResponse> getActiveSubjects() {
        return subjectRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SubjectResponse> searchSubjects(String keyword) {
        return subjectRepository.findByCodeContainingIgnoreCaseOrCurrentNameViContainingIgnoreCaseOrCurrentNameEnContainingIgnoreCase(
                keyword, keyword, keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        // 1. Validate và lấy Department (eager load Faculty để tránh lazy loading issues)
        Department department = departmentRepository.findWithFacultyById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // 2. Validate mã môn học không trùng
        if (subjectRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Subject code already exists");
        }
        
        // 3. Lấy Academic Term
        AcademicTerm academicTerm = academicTermRepository.findById(request.getAcademicTermId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicTerm", "id", request.getAcademicTermId()));

        // 4. Lấy Curriculum (nếu có)
        Curriculum curriculum = null;
        if (request.getCurriculumId() != null) {
            curriculum = curriculumRepository.findById(request.getCurriculumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", request.getCurriculumId()));
        }

        // 5. Tạo Subject
        Subject subject = Subject.builder()
                .code(request.getCode())
                .department(department)
                .curriculum(curriculum)
                .currentNameVi(request.getCurrentNameVi())
                .currentNameEn(request.getCurrentNameEn())
                .defaultCredits(request.getDefaultCredits())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .subjectType(request.getSubjectType() != null ? request.getSubjectType() : SubjectType.REQUIRED)
                .component(request.getComponent() != null ? request.getComponent() : SubjectComponent.BOTH)
                .defaultTheoryHours(request.getDefaultTheoryHours() != null ? request.getDefaultTheoryHours() : 0)
                .defaultPracticeHours(request.getDefaultPracticeHours() != null ? request.getDefaultPracticeHours() : 0)
                .defaultSelfStudyHours(request.getDefaultSelfStudyHours() != null ? request.getDefaultSelfStudyHours() : 0)
                .description(request.getDescription())
                .recommendedTerm(request.getRecommendedTerm())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        
        // 6. Gửi thông báo cho Trưởng bộ môn
        sendNotificationToHod(savedSubject, department, academicTerm);

        return mapToResponse(savedSubject);
    }
    
    /**
     * Gửi thông báo phân công biên soạn đề cương cho Trưởng bộ môn
     */
    private void sendNotificationToHod(Subject subject, Department department, AcademicTerm academicTerm) {
        // Tìm Trưởng bộ môn của department
        Optional<User> hodOpt = userRepository.findHodByDepartmentId(department.getId());
        
        // Nếu không tìm thấy qua department_id, thử tìm qua scope_id
        if (hodOpt.isEmpty()) {
            hodOpt = userRepository.findHodByScopeId(department.getId());
        }
        
        if (hodOpt.isEmpty()) {
            log.warn("Không tìm thấy Trưởng bộ môn cho department: {} ({}). Thông báo không được gửi.", 
                    department.getName(), department.getId());
            return;
        }
        
        User hod = hodOpt.get();
        
        // Tính hạn chốt phân công (7 ngày sau khi tạo môn)
        LocalDate deadline = LocalDate.now().plusDays(ASSIGNMENT_DEADLINE_DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Tạo tiêu đề thông báo
        String title = String.format("[Phân công biên soạn] Môn học mới: %s - %s", 
                subject.getCode(), academicTerm.getName());
        
        // Tạo nội dung thông báo chi tiết
        String message = buildNotificationMessage(subject, academicTerm, deadline, formatter);
        
        // Tạo payload chứa thông tin bổ sung
        Map<String, Object> payload = new HashMap<>();
        payload.put("subjectId", subject.getId().toString());
        payload.put("subjectCode", subject.getCode());
        payload.put("departmentId", department.getId().toString());
        payload.put("academicTermId", academicTerm.getId().toString());
        payload.put("deadline", deadline.format(formatter));
        payload.put("actionUrl", "/admin/teaching-assignment"); // URL để HOD phân công
        
        // Tạo và lưu notification
        Notification notification = Notification.builder()
                .user(hod)
                .title(title)
                .message(message)
                .type("ASSIGNMENT") // Loại thông báo: Phân công
                .payload(payload)
                .isRead(false)
                .relatedEntityType("SUBJECT")
                .relatedEntityId(subject.getId())
                .build();
        
        notificationRepository.save(notification);
        
        log.info("Đã gửi thông báo phân công biên soạn đề cương cho HOD: {} ({})", 
                hod.getFullName(), hod.getEmail());
    }
    
    /**
     * Tạo nội dung chi tiết cho thông báo
     */
    private String buildNotificationMessage(Subject subject, AcademicTerm academicTerm, 
            LocalDate deadline, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chào Trưởng bộ môn,\n\n");
        sb.append(String.format("Phòng Đào tạo vừa khởi tạo môn học mới: %s - %s thuộc bộ môn của bạn cho %s.\n\n",
                subject.getCode(), subject.getCurrentNameVi(), academicTerm.getName()));
        sb.append("Thông tin tóm tắt:\n");
        sb.append(String.format("• Số tín chỉ: %d (%d LT / %d TH)\n", 
                subject.getDefaultCredits(), 
                subject.getDefaultTheoryHours(), 
                subject.getDefaultPracticeHours()));
        sb.append(String.format("• Hạn chốt phân công: %s\n\n", deadline.format(formatter)));
        sb.append("Vui lòng truy cập hệ thống để chỉ định Giảng viên chính và Giảng viên cộng tác biên soạn đề cương cho môn học này.");
        return sb.toString();
    }

    @Transactional
    public SubjectResponse updateSubject(UUID id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        // Use findWithFacultyById to eagerly load Faculty to avoid lazy loading issues
        Department department = departmentRepository.findWithFacultyById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        if (!subject.getCode().equals(request.getCode()) 
                && subjectRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Subject code already exists");
        }

        Curriculum curriculum = null;
        if (request.getCurriculumId() != null) {
            curriculum = curriculumRepository.findById(request.getCurriculumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curriculum", "id", request.getCurriculumId()));
        }

        subject.setCode(request.getCode());
        subject.setDepartment(department);
        subject.setCurriculum(curriculum);
        subject.setCurrentNameVi(request.getCurrentNameVi());
        subject.setCurrentNameEn(request.getCurrentNameEn());
        subject.setDefaultCredits(request.getDefaultCredits());
        if (request.getIsActive() != null) {
            subject.setIsActive(request.getIsActive());
        }
        if (request.getSubjectType() != null) {
            subject.setSubjectType(request.getSubjectType());
        }
        if (request.getComponent() != null) {
            subject.setComponent(request.getComponent());
        }
        subject.setDefaultTheoryHours(request.getDefaultTheoryHours() != null ? request.getDefaultTheoryHours() : 0);
        subject.setDefaultPracticeHours(request.getDefaultPracticeHours() != null ? request.getDefaultPracticeHours() : 0);
        subject.setDefaultSelfStudyHours(request.getDefaultSelfStudyHours() != null ? request.getDefaultSelfStudyHours() : 0);
        subject.setDescription(request.getDescription());
        subject.setRecommendedTerm(request.getRecommendedTerm());

        Subject updatedSubject = subjectRepository.save(subject);
        return mapToResponse(updatedSubject);
    }

    @Transactional
    public void deleteSubject(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }
        subjectRepository.deleteById(id);
    }

    public List<PrerequisiteResponse> getPrerequisitesOfSubject(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }
        return relationshipRepository.findBySubjectId(id).stream()
                .map(this::mapToPrerequisiteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrerequisiteResponse addPrerequisiteToSubject(UUID id, PrerequisiteRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        Subject relatedSubject = subjectRepository.findById(request.getRelatedSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Related Subject", "id", request.getRelatedSubjectId()));

        if (id.equals(request.getRelatedSubjectId())) {
            throw new BadRequestException("Subject cannot have relationship with itself");
        }

        if (relationshipRepository.findBySubjectIdAndRelatedSubjectIdAndType(
                id, request.getRelatedSubjectId(), request.getType()).isPresent()) {
            throw new BadRequestException("Prerequisite relationship already exists");
        }

        SubjectRelationship relationship = SubjectRelationship.builder()
                .subject(subject)
                .relatedSubject(relatedSubject)
                .type(request.getType())
                .build();

        SubjectRelationship savedRelationship = relationshipRepository.save(relationship);
        return mapToPrerequisiteResponse(savedRelationship);
    }

    @Transactional
    public void removePrerequisiteFromSubject(UUID id, UUID prerequisiteId) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }

        SubjectRelationship relationship = relationshipRepository.findById(prerequisiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite", "id", prerequisiteId));

        if (!relationship.getSubject().getId().equals(id)) {
            throw new BadRequestException("Prerequisite does not belong to this subject");
        }

        relationshipRepository.deleteById(prerequisiteId);
    }

    public List<SyllabusResponse> getSyllabiOfSubject(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }
        
        return syllabusVersionRepository.findBySubjectId(id).stream()
                .map(this::mapToSyllabusResponse)
                .collect(Collectors.toList());
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return mapToResponse(subject, null);
    }
    
    private SubjectResponse mapToResponse(Subject subject, String prerequisites) {
        SubjectResponse response = new SubjectResponse();
        response.setId(subject.getId());
        response.setCode(subject.getCode());
        
        try {
            if (subject.getDepartment() != null) {
                response.setDepartmentId(subject.getDepartment().getId());
                response.setDepartmentCode(subject.getDepartment().getCode());
                response.setDepartmentName(subject.getDepartment().getName());
                
                if (subject.getDepartment().getFaculty() != null) {
                    response.setFacultyName(subject.getDepartment().getFaculty().getName());
                }
            }
        } catch (Exception e) {
            // Ignore lazy loading errors
        }
        
        // Get semester from latest syllabus
        try {
            var latestSyllabus = syllabusVersionRepository.findBySubjectId(subject.getId())
                    .stream()
                    .findFirst();
            if (latestSyllabus.isPresent() && latestSyllabus.get().getAcademicTerm() != null) {
                String termCode = latestSyllabus.get().getAcademicTerm().getCode();
                if (termCode != null && termCode.startsWith("HK")) {
                    response.setSemester(termCode.substring(2, 3));
                }
            }
        } catch (Exception e) {
            // Ignore if no syllabus found
        }
        
        // Set prerequisites if provided
        if (prerequisites != null && !prerequisites.isEmpty()) {
            response.setPrerequisites(prerequisites);
        }
        
        if (subject.getCurriculum() != null) {
            response.setCurriculumId(subject.getCurriculum().getId());
            response.setCurriculumCode(subject.getCurriculum().getCode());
            response.setCurriculumName(subject.getCurriculum().getName());
        }
        response.setCurrentNameVi(subject.getCurrentNameVi());
        response.setCurrentNameEn(subject.getCurrentNameEn());
        response.setDefaultCredits(subject.getDefaultCredits());
        response.setIsActive(subject.getIsActive());
        response.setSubjectType(subject.getSubjectType());
        response.setComponent(subject.getComponent());
        response.setDefaultTheoryHours(subject.getDefaultTheoryHours());
        response.setDefaultPracticeHours(subject.getDefaultPracticeHours());
        response.setDefaultSelfStudyHours(subject.getDefaultSelfStudyHours());
        response.setDescription(subject.getDescription());
        response.setRecommendedTerm(subject.getRecommendedTerm());
        response.setCreatedAt(subject.getCreatedAt());
        response.setUpdatedAt(subject.getUpdatedAt());
        return response;
    }

    private PrerequisiteResponse mapToPrerequisiteResponse(SubjectRelationship relationship) {
        PrerequisiteResponse response = new PrerequisiteResponse();
        response.setId(relationship.getId());
        response.setSubjectId(relationship.getSubject().getId());
        response.setSubjectCode(relationship.getSubject().getCode());
        response.setSubjectName(relationship.getSubject().getCurrentNameVi());
        response.setRelatedSubjectId(relationship.getRelatedSubject().getId());
        response.setRelatedSubjectCode(relationship.getRelatedSubject().getCode());
        response.setRelatedSubjectName(relationship.getRelatedSubject().getCurrentNameVi());
        response.setType(relationship.getType());
        response.setCreatedAt(relationship.getCreatedAt());
        return response;
    }

    private SyllabusResponse mapToSyllabusResponse(SyllabusVersion syllabus) {
        SyllabusResponse response = new SyllabusResponse();
        response.setId(syllabus.getId());
        response.setSubjectId(syllabus.getSubject().getId());
        response.setSubjectCode(syllabus.getSubject().getCode());
        response.setSubjectNameVi(syllabus.getSubject().getCurrentNameVi());
        response.setSubjectNameEn(syllabus.getSubject().getCurrentNameEn());
        if (syllabus.getAcademicTerm() != null) {
            response.setAcademicTermId(syllabus.getAcademicTerm().getId());
            response.setAcademicTermCode(syllabus.getAcademicTerm().getCode());
        }
        response.setStatus(syllabus.getStatus().name());
        response.setCreatedAt(syllabus.getCreatedAt());
        response.setUpdatedAt(syllabus.getUpdatedAt());
        if (syllabus.getCreatedBy() != null) {
            response.setCreatedBy(syllabus.getCreatedBy().getId());
        }
        if (syllabus.getUpdatedBy() != null) {
            response.setUpdatedBy(syllabus.getUpdatedBy().getId());
        }
        return response;
    }
}
