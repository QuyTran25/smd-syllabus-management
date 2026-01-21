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
import vn.edu.smd.shared.enums.SyllabusStatus;

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

    // Helper: Lấy sinh viên hiện tại từ Security Context
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
        
        // Lấy danh sách ID các môn đã theo dõi
        Set<UUID> trackedIds = trackerRepository.findByStudentId(student.getId()).stream()
                .map(StudentSyllabusTracker::getSyllabusId)
                .collect(Collectors.toSet());

        // Chỉ lấy các syllabus có status = PUBLISHED
        return versionRepository.findByStatusAndNotDeleted(SyllabusStatus.PUBLISHED).stream()
                // Sắp xếp: Mới nhất lên đầu (publishedAt DESC)
                .sorted((v1, v2) -> {
                    if (v1.getPublishedAt() == null && v2.getPublishedAt() == null) return 0;
                    if (v1.getPublishedAt() == null) return 1;
                    if (v2.getPublishedAt() == null) return -1;
                    return v2.getPublishedAt().compareTo(v1.getPublishedAt());
                })
                .map(version -> mapToSummaryDto(version, trackedIds))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentSyllabusDetailDto getById(UUID id) {
        User student = getCurrentStudent();

        // 🟢 FIX 1: Logic tìm kiếm thông minh (Fallback)
        // Thử tìm theo ID (Version ID) trước. Nếu không thấy -> Tìm theo Subject ID
        SyllabusVersion version = versionRepository.findById(id)
                .orElseGet(() -> {
                    log.warn("⚠️ [getById] ID {} không phải Version ID. Đang thử tìm theo Subject ID...", id);
                    return versionRepository.findFirstBySubjectIdAndStatusOrderByCreatedAtDesc(id, SyllabusStatus.PUBLISHED)
                            .orElseThrow(() -> new BadRequestException("Đề cương không tồn tại hoặc chưa được xuất bản!"));
                });

        // 🟢 FIX 2: Nới lỏng điều kiện Status (Chấp nhận cả APPROVED và PUBLISHED)
        // Điều này giúp tránh lỗi 403 khi tải PDF nếu dữ liệu cũ chưa kịp update status
        if (version.getStatus() != SyllabusStatus.PUBLISHED && version.getStatus() != SyllabusStatus.APPROVED) {
            log.warn("⛔ [getById] Sinh viên {} cố truy cập đề cương {} trạng thái {}", student.getId(), version.getId(), version.getStatus());
            throw new BadRequestException("Đề cương chưa được xuất bản!");
        }

        Subject subject = version.getSubject();
        if (subject == null) {
            throw new BadRequestException("Dữ liệu lỗi: Đề cương không gắn với môn học nào!");
        }

        // Gọi hàm helper để map dữ liệu chi tiết (đã bao gồm logic parse JSON)
        return mapToDetailDto(version, subject, student.getId());
    }

    @Override
    @Transactional
    public void toggleTrack(UUID syllabusId) {
        try {
            User student = getCurrentStudent();
            
            // 🟢 FIX 3: Logic Fallback cho tính năng Theo dõi
            if (!versionRepository.existsById(syllabusId)) {
                log.info("ℹ️ [ToggleTrack] ID {} không tìm thấy trong bảng Version. Thử tìm theo Subject...", syllabusId);
                var v = versionRepository.findFirstBySubjectIdAndStatusOrderByCreatedAtDesc(syllabusId, SyllabusStatus.PUBLISHED);
                if (v.isPresent()) {
                    syllabusId = v.get().getId(); // Cập nhật lại ID đúng
                    log.info("✅ [ToggleTrack] Đã tìm thấy Version ID thay thế: {}", syllabusId);
                } else {
                    throw new BadRequestException("Đề cương không tồn tại!");
                }
            }
            
            Optional<StudentSyllabusTracker> existing = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), syllabusId);
            
            if (existing.isPresent()) {
                trackerRepository.delete(existing.get());
                log.info("🗑️ [ToggleTrack] Đã bỏ theo dõi: {}", syllabusId);
            } else {
                StudentSyllabusTracker tracker = new StudentSyllabusTracker();
                tracker.setStudentId(student.getId());
                tracker.setSyllabusId(syllabusId);
                tracker.setCreatedAt(LocalDateTime.now());
                trackerRepository.save(tracker);
                log.info("⭐ [ToggleTrack] Đã theo dõi: {}", syllabusId);
            }
        } catch (Exception e) {
            log.error("❌ [ToggleTrack] Lỗi: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void reportIssue(ReportIssueDto dto) {
        // Logic tìm đề cương để báo lỗi (có fallback)
        SyllabusVersion version = versionRepository.findById(dto.getSyllabusId())
                .orElseGet(() -> versionRepository.findFirstBySubjectIdAndStatusOrderByCreatedAtDesc(dto.getSyllabusId(), SyllabusStatus.PUBLISHED)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy đề cương để báo lỗi!")));

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

        SyllabusErrorReport report = SyllabusErrorReport.builder()
                .syllabusVersion(version)
                .user(student)
                .title("Báo lỗi từ SV: " + student.getFullName())
                .description(dto.getDescription())
                .section(sectionEnum)
                .type(FeedbackType.ERROR)
                .status("PENDING")
                .editEnabled(false)
                .build();

        errorReportRepository.save(report);
        
        // Gửi thông báo cho Admin
        notifyAdmins(student, version, sectionEnum);
    }

    // =================================================================
    // CÁC HÀM HELPER (Giúp code gọn gàng, logic không bị thay đổi)
    // =================================================================

    private StudentSyllabusSummaryDto mapToSummaryDto(SyllabusVersion version, Set<UUID> trackedIds) {
        Subject s = version.getSubject();
        if (s == null) return null;

        String deptName = (s.getDepartment() != null) ? s.getDepartment().getName() : "Chưa phân bộ môn";
        String facultyName = (s.getDepartment() != null && s.getDepartment().getFaculty() != null)
                ? s.getDepartment().getFaculty().getName() : "Chưa phân khoa";
        String programName = (s.getCurriculum() != null) ? s.getCurriculum().getName() : "Chương trình chuẩn";
        String termName = (version.getAcademicTerm() != null) ? version.getAcademicTerm().getName() : "HK1 2024-2025";
        String publishedAtStr = (version.getPublishedAt() != null) ? version.getPublishedAt().toLocalDate().toString() : null;

        return StudentSyllabusSummaryDto.builder()
                .id(version.getId())
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
                .status("PUBLISHED")
                .publishedAt(publishedAtStr)
                .build();
    }

    private StudentSyllabusDetailDto mapToDetailDto(SyllabusVersion version, Subject subject, UUID studentId) {
        boolean isTracked = trackerRepository.findByStudentIdAndSyllabusId(studentId, version.getId()).isPresent();
        
        // Khởi tạo các list dữ liệu
        List<StudentSyllabusDetailDto.CloDto> cloDtos = new ArrayList<>();
        List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos = new ArrayList<>();
        List<String> textbooksList = new ArrayList<>();
        List<String> referencesList = new ArrayList<>();
        Map<String, List<String>> matrixMap = new HashMap<>();

        // 1. Ưu tiên Parse từ JSON content
        if (version.getContent() != null) {
            parseContent(version.getContent(), cloDtos, assessmentDtos, textbooksList, referencesList, matrixMap);
        }

        // 2. Fallback: Nếu JSON rỗng thì lấy từ DB (Giữ nguyên logic cũ của bạn)
        if (cloDtos.isEmpty()) fallbackClosFromDb(version.getId(), cloDtos, matrixMap);
        if (assessmentDtos.isEmpty()) fallbackAssessmentsFromDb(version.getId(), assessmentDtos);

        String facultyName = (subject.getDepartment() != null && subject.getDepartment().getFaculty() != null) ? 
                              subject.getDepartment().getFaculty().getName() : "";
        String termName = (version.getAcademicTerm() != null) ? version.getAcademicTerm().getName() : "HK1 2024-2025";
        String publishedAtStr = (version.getPublishedAt() != null) ? version.getPublishedAt().toLocalDate().toString() : null;
        String descriptionText = (version.getDescription() != null) ? version.getDescription() : 
                                 (subject.getDescription() != null ? subject.getDescription() : "Đang cập nhật...");

        List<String> objectivesList = (version.getObjectives() != null && !version.getObjectives().isEmpty())
                ? List.of(version.getObjectives().split("\\n")) : List.of("Chưa có mục tiêu");
        
        List<String> studentTasksList = (version.getStudentTasks() != null && !version.getStudentTasks().isEmpty())
                ? List.of(version.getStudentTasks().split("\\n")) : List.of("Tham gia lớp học", "Làm bài tập");

        List<String> ploCodeList = ploRepository.findBySubjectId(subject.getId()).stream()
                .map(PLO::getCode).distinct().sorted().collect(Collectors.toList());

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
                .status("PUBLISHED") // Luôn trả về PUBLISHED để Frontend hiển thị đúng
                .isTracked(isTracked)
                .clos(cloDtos)
                .ploList(ploCodeList)
                .cloPloMap(matrixMap)
                .assessmentMatrix(assessmentDtos)
                .objectives(objectivesList)
                .studentTasks(studentTasksList)
                .textbooks(textbooksList)
                .references(referencesList)
                .timeAllocation(new StudentSyllabusDetailDto.TimeAllocationDto(
                        version.getTheoryHours(), version.getPracticeHours(), version.getSelfStudyHours()))
                .build();
    }

    // Hàm này gộp logic parse JSON của cả CLO, Assessment, Textbooks, References vào một chỗ
    private void parseContent(Map<String, Object> content, 
                              List<StudentSyllabusDetailDto.CloDto> cloDtos,
                              List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos,
                              List<String> textbooks,
                              List<String> references,
                              Map<String, List<String>> matrixMap) {
        try {
            // CLOs
            Object closObj = content.get("clos");
            if (closObj instanceof List) {
                for (Object item : (List<?>) closObj) {
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        String code = (String) map.get("code");
                        List<String> ploList = new ArrayList<>();
                        Object mappedPLOs = map.get("mappedPLOs");
                        if (mappedPLOs instanceof List) {
                            ((List<?>) mappedPLOs).forEach(p -> ploList.add(p.toString()));
                        }
                        
                        cloDtos.add(StudentSyllabusDetailDto.CloDto.builder()
                                .code(code)
                                .description((String) map.get("description"))
                                .bloomLevel((String) map.get("bloomLevel"))
                                .weight(map.get("weight") != null ? ((Number) map.get("weight")).intValue() : 0)
                                .plo(ploList)
                                .build());
                        
                        if (code != null && !ploList.isEmpty()) matrixMap.put(code, ploList);
                    }
                }
            }

            // Assessments
            Object assessObj = content.get("assessmentMethods");
            if (assessObj instanceof List) {
                for (Object item : (List<?>) assessObj) {
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        List<String> cloList = new ArrayList<>();
                        Object closRelated = map.get("clos");
                        if (closRelated instanceof List) {
                            ((List<?>) closRelated).forEach(c -> cloList.add(c.toString()));
                        }
                        
                        assessmentDtos.add(StudentSyllabusDetailDto.AssessmentDto.builder()
                                .method((String) map.get("method"))
                                .form((String) map.get("form"))
                                .criteria((String) map.get("criteria"))
                                .weight(map.get("weight") != null ? ((Number) map.get("weight")).intValue() : 0)
                                .clo(cloList)
                                .build());
                    }
                }
            }

            // Textbooks
            Object tbObj = content.get("textbooks");
            if (tbObj instanceof List) {
                for (Object item : (List<?>) tbObj) {
                    if (item instanceof Map) {
                        String t = (String) ((Map<?, ?>) item).get("title");
                        if (t != null) textbooks.add(t);
                    }
                }
            }

            // References
            Object refObj = content.get("references");
            if (refObj instanceof String) {
                references.addAll(Arrays.asList(((String) refObj).split("\\n")));
            }
        } catch (Exception e) {
            log.error("❌ Error parsing JSON content: {}", e.getMessage());
        }
    }

    private void fallbackClosFromDb(UUID versionId, List<StudentSyllabusDetailDto.CloDto> cloDtos, Map<String, List<String>> matrixMap) {
        List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(versionId);
        List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());
        List<CloPlOMapping> mappings = cloIds.isEmpty() ? Collections.emptyList() : cloPloMappingRepository.findByCloIdIn(cloIds);
        
        for (CloPlOMapping map : mappings) {
            if (map.getClo() != null && map.getPlo() != null) {
                matrixMap.computeIfAbsent(map.getClo().getCode(), k -> new ArrayList<>()).add(map.getPlo().getCode());
            }
        }
        
        clos.forEach(clo -> cloDtos.add(StudentSyllabusDetailDto.CloDto.builder()
                .code(clo.getCode())
                .description(clo.getDescription())
                .bloomLevel(clo.getBloomLevel())
                .weight(clo.getWeight() != null ? clo.getWeight().intValue() : 0)
                .plo(matrixMap.getOrDefault(clo.getCode(), new ArrayList<>()))
                .build()));
    }

    private void fallbackAssessmentsFromDb(UUID versionId, List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos) {
        List<AssessmentScheme> assessments = assessmentRepository.findBySyllabusVersionIdOrderByCreatedAtAsc(versionId);
        assessments.forEach(a -> assessmentDtos.add(StudentSyllabusDetailDto.AssessmentDto.builder()
                .method(a.getName())
                .form("Báo cáo/Thi")
                .criteria("Rubric")
                .weight(a.getWeightPercent() != null ? a.getWeightPercent().intValue() : 0)
                .clo(new ArrayList<>())
                .build()));
    }

    private void notifyAdmins(User student, SyllabusVersion version, ErrorReportSection section) {
        String notificationTitle = "🚨 Báo lỗi từ sinh viên";
        String notificationMessage = String.format("Sinh viên %s đã báo lỗi về đề cương '%s' (Phần: %s)",
                student.getFullName(), version.getSubject().getCurrentNameVi(), section.toString());

        List<User> adminUsers = userRepository.findAll().stream()
                .filter(u -> u.getUserRoles() != null && u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole() != null && 
                                ("Administrator".equals(ur.getRole().getName()) || "ADMIN".equals(ur.getRole().getCode()))))
                .collect(Collectors.toList());

        for (User admin : adminUsers) {
            notificationService.createNotificationForUser(admin, notificationTitle, notificationMessage, "ERROR_REPORT");
        }
    }
}