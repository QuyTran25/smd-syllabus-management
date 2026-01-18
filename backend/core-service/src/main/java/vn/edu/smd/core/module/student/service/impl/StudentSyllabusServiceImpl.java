package vn.edu.smd.core.module.student.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.notification.service.NotificationService;
import vn.edu.smd.core.module.student.dto.ReportIssueDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusSummaryDto;
import vn.edu.smd.core.module.student.service.StudentSyllabusService;
import vn.edu.smd.core.module.student.repository.StudentSyllabusTrackerRepository;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.shared.enums.ErrorReportSection;
import vn.edu.smd.shared.enums.FeedbackType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSyllabusServiceImpl implements StudentSyllabusService {

    private final SubjectRepository subjectRepository;
    private final SyllabusVersionRepository versionRepository;
    private final CLORepository cloRepository;
    private final PLORepository ploRepository;
    private final CloPlOMappingRepository cloPloMappingRepository;
    private final AssessmentSchemeRepository assessmentRepository;
    private final AssessmentCloMappingRepository assessmentCloMappingRepository;
    private final StudentSyllabusTrackerRepository trackerRepository;
    private final SyllabusErrorReportRepository errorReportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private User getCurrentStudent() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> {
                    log.error("Token User not found: {}", principal);
                    return new RuntimeException("Không tìm thấy sinh viên! (Vui lòng đăng nhập lại)");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSyllabusSummaryDto> getAll() {
        User student = getCurrentStudent();
        
        // ✅ FIX: Lấy tracked IDs từ student_syllabus_tracker
        Set<UUID> trackedIds = trackerRepository.findByStudentId(student.getId()).stream()
                .map(StudentSyllabusTracker::getSyllabusId)
                .collect(Collectors.toSet());

        // 🔥 FIX: Chỉ lấy các syllabus có status = PUBLISHED
        return versionRepository.findByStatusAndNotDeleted(vn.edu.smd.shared.enums.SyllabusStatus.PUBLISHED).stream()
                // 🔥 FIX: Sort by publishedAt DESC (mới nhất lên đầu)
                .sorted((v1, v2) -> {
                    if (v1.getPublishedAt() == null && v2.getPublishedAt() == null) return 0;
                    if (v1.getPublishedAt() == null) return 1;  // null xuống cuối
                    if (v2.getPublishedAt() == null) return -1;
                    return v2.getPublishedAt().compareTo(v1.getPublishedAt()); // DESC
                })
                .map(version -> {
                    Subject s = version.getSubject();
                    if (s == null) {
                        log.warn("📍 [getAll] Syllabus version {} has no subject", version.getId());
                        return null;
                    }
                    String deptName = (s.getDepartment() != null) ? s.getDepartment().getName() : "Chưa phân bộ môn";
                    String facultyName = (s.getDepartment() != null && s.getDepartment().getFaculty() != null)
                            ? s.getDepartment().getFaculty().getName() : "Chưa phân khoa";
                    String programName = (s.getCurriculum() != null) ? s.getCurriculum().getName() : "Chương trình chuẩn";
                    
                    // 🔥 FIX: Lấy term từ AcademicTerm
                    String termName = (version.getAcademicTerm() != null) 
                            ? version.getAcademicTerm().getName() 
                            : "HK1 2024-2025";
                    
                    // 🔥 FIX: Format publishedAt thành YYYY-MM-DD
                    String publishedAtStr = (version.getPublishedAt() != null) 
                            ? version.getPublishedAt().toLocalDate().toString() 
                            : null;

                    return StudentSyllabusSummaryDto.builder()
                            .id(version.getId())  // ✅ Sử dụng SyllabusVersion ID, không phải Subject ID
                            .code(s.getCode())
                            .nameVi(s.getCurrentNameVi())
                            .term(termName)
                            .credits(s.getDefaultCredits())
                            .faculty(facultyName)
                            .program(programName)
                            .lecturerName("Bộ môn " + deptName)
                            .majorShort(s.getCode().length() >= 2 ? s.getCode().substring(0, 2) : "GEN")
                            .progress(100)
                            .tracked(trackedIds.contains(version.getId()))
                            .status("PUBLISHED")  // ✅ Always PUBLISHED (đã filter ở query)
                            .publishedAt(publishedAtStr)  // ✅ Ngày xuất bản thật
                            .build();
                })
                .filter(Objects::nonNull)  // Loại bỏ các null
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentSyllabusDetailDto getById(UUID id) {  // Bây giờ id là syllabusVersionId (từ frontend)
        User student = getCurrentStudent();

        // ✅ FIX: Fetch SyllabusVersion trước thay vì Subject
        SyllabusVersion version = versionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Đề cương không tồn tại!"));  // Thay RuntimeException bằng BadRequestException để handle graceful

        // 🔥 FIX: Kiểm tra status = PUBLISHED
        if (version.getStatus() != vn.edu.smd.shared.enums.SyllabusStatus.PUBLISHED) {
            log.warn("📍 [getById] Student {} attempted to access non-published syllabus {}", student.getId(), id);
            throw new BadRequestException("Đề cương chưa được xuất bản!");
        }

        // ✅ Lấy Subject từ Version
        Subject subject = version.getSubject();
        if (subject == null) {
            throw new BadRequestException("Không tìm thấy môn học liên kết với đề cương!");
        }

        // 🔥 FIX: Parse CLO và Assessment từ content JSONB (không phải từ bảng riêng)
        List<StudentSyllabusDetailDto.CloDto> cloDtos = new ArrayList<>();
        List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos = new ArrayList<>();
        Map<String, List<String>> matrixMap = new HashMap<>();
        
        if (version.getContent() != null) {
            try {
                // Parse CLOs từ content->clos
                Object closObj = version.getContent().get("clos");
                if (closObj instanceof List) {
                    for (Object item : (List<?>) closObj) {
                        if (item instanceof Map) {
                            Map<?, ?> cloMap = (Map<?, ?>) item;
                            String code = (String) cloMap.get("code");
                            String description = (String) cloMap.get("description");
                            String bloomLevel = (String) cloMap.get("bloomLevel");
                            Integer weight = cloMap.get("weight") != null ? 
                                    ((Number) cloMap.get("weight")).intValue() : 0;
                            
                            // Parse mappedPLOs
                            List<String> ploList = new ArrayList<>();
                            Object mappedPLOs = cloMap.get("mappedPLOs");
                            if (mappedPLOs instanceof List) {
                                for (Object plo : (List<?>) mappedPLOs) {
                                    ploList.add(plo.toString());
                                }
                            }
                            
                            cloDtos.add(StudentSyllabusDetailDto.CloDto.builder()
                                    .code(code)
                                    .description(description)
                                    .bloomLevel(bloomLevel)
                                    .weight(weight)
                                    .plo(ploList)
                                    .build());
                            
                            // Build matrixMap for CLO-PLO matrix
                            if (code != null && !ploList.isEmpty()) {
                                matrixMap.put(code, ploList);
                            }
                        }
                    }
                }
                
                // Parse Assessments từ content->assessmentMethods
                Object assessObj = version.getContent().get("assessmentMethods");
                if (assessObj instanceof List) {
                    for (Object item : (List<?>) assessObj) {
                        if (item instanceof Map) {
                            Map<?, ?> assessMap = (Map<?, ?>) item;
                            String method = (String) assessMap.get("method");
                            String form = (String) assessMap.get("form");
                            String criteria = (String) assessMap.get("criteria");
                            Integer weight = assessMap.get("weight") != null ?
                                    ((Number) assessMap.get("weight")).intValue() : 0;
                            
                            // Parse CLOs liên quan
                            List<String> cloList = new ArrayList<>();
                            Object closRelated = assessMap.get("clos");
                            if (closRelated instanceof List) {
                                for (Object clo : (List<?>) closRelated) {
                                    cloList.add(clo.toString());
                                }
                            }
                            
                            assessmentDtos.add(StudentSyllabusDetailDto.AssessmentDto.builder()
                                    .method(method != null ? method : "")
                                    .form(form != null ? form : "")
                                    .criteria(criteria != null ? criteria : "")
                                    .weight(weight)
                                    .clo(cloList)
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("❌ [getById] Failed to parse CLO/Assessment from content: {}", e.getMessage(), e);
            }
        }
        
        // Fallback: Nếu không có trong content, thử query từ bảng clos/assessment_schemes
        if (cloDtos.isEmpty()) {
            log.info("📍 [getById] No CLOs in content, trying database tables...");
            List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(version.getId());
            List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());
            List<CloPlOMapping> cloPloMappings = cloIds.isEmpty() ? Collections.emptyList() : 
                    cloPloMappingRepository.findByCloIdIn(cloIds);
            for (CloPlOMapping map : cloPloMappings) {
                if (map.getClo() != null && map.getPlo() != null) {
                    matrixMap.computeIfAbsent(map.getClo().getCode(), k -> new ArrayList<>())
                            .add(map.getPlo().getCode());
                }
            }
            cloDtos = clos.stream().map(clo ->
                    StudentSyllabusDetailDto.CloDto.builder()
                            .code(clo.getCode())
                            .description(clo.getDescription())
                            .bloomLevel(clo.getBloomLevel())
                            .weight(clo.getWeight() != null ? clo.getWeight().intValue() : 0)
                            .plo(matrixMap.getOrDefault(clo.getCode(), new ArrayList<>()))
                            .build()
            ).collect(Collectors.toList());
        }
        
        if (assessmentDtos.isEmpty()) {
            log.info("📍 [getById] No Assessments in content, trying database tables...");
            List<AssessmentScheme> assessments = assessmentRepository.findBySyllabusVersionIdOrderByCreatedAtAsc(version.getId());
            List<UUID> assessmentIds = assessments.stream().map(AssessmentScheme::getId).collect(Collectors.toList());
            Map<UUID, List<String>> assessCloMap = new HashMap<>();
            if (!assessmentIds.isEmpty()) {
                 List<AssessmentCloMapping> assessMappings = assessmentCloMappingRepository.findByAssessmentSchemeIdIn(assessmentIds);
                 assessCloMap = assessMappings.stream()
                    .filter(m -> m.getAssessmentScheme() != null && m.getClo() != null)
                    .collect(Collectors.groupingBy(
                            m -> m.getAssessmentScheme().getId(),
                            Collectors.mapping(m -> m.getClo().getCode(), Collectors.toList())
                    ));
            }
            Map<UUID, List<String>> finalAssessCloMap = assessCloMap;
            assessmentDtos = assessments.stream().map(a ->
                    StudentSyllabusDetailDto.AssessmentDto.builder()
                            .method(a.getName())
                            .form(a.getName() != null && a.getName().contains("Thi") ? "Tự luận/Trắc nghiệm" : "Báo cáo")
                            .criteria("Rubric " + a.getName())
                            .weight(a.getWeightPercent() != null ? a.getWeightPercent().intValue() : 0)
                            .clo(finalAssessCloMap.getOrDefault(a.getId(), new ArrayList<>()))
                            .build()
            ).collect(Collectors.toList());
        }

        // ✅ FIX: Use SyllabusVersion ID for tracker lookup
        boolean isTracked = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), version.getId()).isPresent();
        
        String facultyName = (subject.getDepartment() != null && subject.getDepartment().getFaculty() != null) ? 
                              subject.getDepartment().getFaculty().getName() : "";
        
        // 🔥 FIX: Lấy term từ AcademicTerm
        String termName = (version.getAcademicTerm() != null) 
                ? version.getAcademicTerm().getName() 
                : "HK1 2024-2025";
        
        // 🔥 FIX: Lấy publishedAt thực tế từ database
        String publishedAtStr = (version.getPublishedAt() != null) 
                ? version.getPublishedAt().toLocalDate().toString() 
                : null;
        
        // 🔥 FIX: Lấy description/objectives/studentTasks từ SyllabusVersion (không phải Subject)
        String descriptionText = (version.getDescription() != null) 
                ? version.getDescription() 
                : (subject.getDescription() != null ? subject.getDescription() : "Đang cập nhật...");
        
        // Parse objectives và studentTasks từ text
        List<String> objectivesList = (version.getObjectives() != null && !version.getObjectives().isEmpty())
                ? List.of(version.getObjectives().split("\\n"))
                : List.of("Chưa có mục tiêu");
        
        List<String> studentTasksList = (version.getStudentTasks() != null && !version.getStudentTasks().isEmpty())
                ? List.of(version.getStudentTasks().split("\\n"))
                : List.of("Tham gia lớp học", "Làm bài tập", "Tự học");
        
        // ✅ FIX: Lấy PLO của Subject này thay vì tất cả PLO
        List<String> ploCodeList = ploRepository.findBySubjectId(subject.getId()).stream()
                .map(PLO::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // 🔥 FIX: Extract textbooks và references từ content JSONB
        List<String> textbooksList = new ArrayList<>();
        List<String> referencesList = new ArrayList<>();
        
        if (version.getContent() != null) {
            try {
                // Parse textbooks (array of objects)
                Object textbooksObj = version.getContent().get("textbooks");
                if (textbooksObj != null) {
                    if (textbooksObj instanceof List) {
                        for (Object item : (List<?>) textbooksObj) {
                            if (item instanceof Map) {
                                Map<?, ?> book = (Map<?, ?>) item;
                                String title = (String) book.get("title");
                                String authors = (String) book.get("authors");
                                String year = book.get("year") != null ? book.get("year").toString() : "";
                                if (title != null) {
                                    textbooksList.add(title + (authors != null ? " - " + authors : "") 
                                                    + (year != null && !year.isEmpty() ? " (" + year + ")" : ""));
                                }
                            }
                        }
                    }
                }
                
                // Parse references (string with line breaks)
                Object referencesObj = version.getContent().get("references");
                if (referencesObj instanceof String) {
                    String refText = (String) referencesObj;
                    if (refText != null && !refText.isEmpty()) {
                        referencesList = Arrays.asList(refText.split("\\n"));
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ [getById] Failed to parse textbooks/references from content: {}", e.getMessage());
            }
        }

        return StudentSyllabusDetailDto.builder()
                .id(subject.getId())
                .versionId(version.getId())
                .code(subject.getCode())
                .nameVi(subject.getCurrentNameVi())
                .nameEn(subject.getCurrentNameEn())
                .term(termName)
                .credits(subject.getDefaultCredits())
                .faculty(facultyName)
                .lecturerName("Giảng viên phụ trách")
                .description(descriptionText)
                .publishedAt(publishedAtStr)
                .summaryInline(descriptionText)
                .status("PUBLISHED")
                .isTracked(isTracked)
                .clos(cloDtos)
                .ploList(ploCodeList)
                .cloPloMap(matrixMap)
                .assessmentMatrix(assessmentDtos)
                .objectives(objectivesList)
                .studentTasks(studentTasksList)
                .textbooks(textbooksList)  // 🔥 FIX: Thêm textbooks
                .references(referencesList)  // 🔥 FIX: Thêm references
                .timeAllocation(new StudentSyllabusDetailDto.TimeAllocationDto(
                        version.getTheoryHours(), version.getPracticeHours(), version.getSelfStudyHours()))
                .build();
    }

    @Override
    @Transactional
    public void toggleTrack(UUID syllabusId) {
        try {
            User student = getCurrentStudent();
            log.info("📍 [ToggleTrack] Start - Syllabus: {}, Student: {}", syllabusId, student.getId());
            
            // ✅ FIX: Check versionRepository thay vì subjectRepository
            // Thêm logging để debug
            boolean exists = versionRepository.existsById(syllabusId);
            log.info("🔍 [ToggleTrack] Syllabus exists: {}", exists);
            
            if (!exists) {
                log.error("❌ [ToggleTrack] Syllabus not found: {}", syllabusId);
                throw new BadRequestException("Đề cương không tồn tại!");
            }
            
            // Gọi đúng tên hàm Repository
            Optional<StudentSyllabusTracker> existing = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), syllabusId);
            
            if (existing.isPresent()) {
                trackerRepository.delete(existing.get());
                log.info("✅ [ToggleTrack] Untracked syllabus {} for student {}", syllabusId, student.getId());
            } else {
                StudentSyllabusTracker tracker = new StudentSyllabusTracker();
                
                // setStudentId hoạt động nhờ hàm thủ công trong Entity
                tracker.setStudentId(student.getId());
                tracker.setSyllabusId(syllabusId);
                tracker.setCreatedAt(LocalDateTime.now());
                
                StudentSyllabusTracker saved = trackerRepository.save(tracker);
                log.info("✅ [ToggleTrack] Tracked syllabus {} for student {} - Tracker ID: {}", 
                        syllabusId, student.getId(), saved.getId());
            }
        } catch (Exception e) {
            log.error("❌ [ToggleTrack] Error toggling track for syllabus {}: {}", syllabusId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void reportIssue(ReportIssueDto dto) {
        SyllabusVersion version;
        if (versionRepository.existsById(dto.getSyllabusId())) {
            version = versionRepository.findById(dto.getSyllabusId()).get();
        } else {
            version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(dto.getSyllabusId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đề cương để báo lỗi!"));
        }

        User student = getCurrentStudent();
        
        ErrorReportSection sectionEnum = ErrorReportSection.OTHER;
        try {
            if(dto.getSection() != null) {
                String s = dto.getSection().toLowerCase();
                if (s.contains("info")) sectionEnum = ErrorReportSection.SUBJECT_INFO;
                else if (s.contains("object")) sectionEnum = ErrorReportSection.OBJECTIVES;
                else if (s.contains("clo")) sectionEnum = ErrorReportSection.CLO;
            }
        } catch (Exception e) {}

        ErrorReportSection finalSectionEnum = sectionEnum;
        SyllabusErrorReport report = SyllabusErrorReport.builder()
                .syllabusVersion(version)
                .user(student)
                .title("Báo lỗi từ sinh viên: " + student.getFullName())
                .description(dto.getDescription())
                .section(finalSectionEnum)
                .type(FeedbackType.ERROR)
                .status("PENDING")
                .editEnabled(false)
                .build();

        errorReportRepository.save(report);

        // 🔔 Tạo notification cho tất cả ADMIN khi sinh viên báo lỗi
        String notificationTitle = "🚨 Báo lỗi từ sinh viên";
        String notificationMessage = String.format(
            "Sinh viên %s đã báo lỗi về đề cương '%s' (Phần: %s)",
            student.getFullName(),
            version.getSubject().getCurrentNameVi(),
            finalSectionEnum.toString()
        );

        // 🔥 FIX: Đổi từ "ADMIN" sang "Administrator"
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(u -> u.getUserRoles() != null && 
                        u.getUserRoles().stream()
                                .anyMatch(ur -> ur.getRole() != null && 
                                        ("Administrator".equals(ur.getRole().getName()) || 
                                        "ADMIN".equals(ur.getRole().getCode()))))
                .collect(Collectors.toList());

        log.info("📨 Found {} admin user(s) to notify", adminUsers.size());

        for (User admin : adminUsers) {
            log.info("🔔 Creating notification for admin: {} (ID: {})", admin.getFullName(), admin.getId());
            notificationService.createNotificationForUser(
                admin,
                notificationTitle,
                notificationMessage,
                "ERROR_REPORT"
            );
        }

        log.info("✅ Sinh viên {} đã báo lỗi về đề cương {} - Đã gửi thông báo cho {} admin(s)", 
                student.getId(), version.getId(), adminUsers.size());
    }
}