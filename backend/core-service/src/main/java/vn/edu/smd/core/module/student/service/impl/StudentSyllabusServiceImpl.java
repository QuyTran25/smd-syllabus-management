package vn.edu.smd.core.module.student.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.entity.*;
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

        return subjectRepository.findAll().stream()
                .map(s -> {
                    // 🔥 LẤY SYLLABUS VERSION ID THAY VÌ SUBJECT ID
                    Optional<SyllabusVersion> latestVersion = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(s.getId());
                    
                    if (latestVersion.isEmpty()) {
                        log.warn("📍 [getAll] No syllabus version found for subject: {}", s.getCode());
                        return null; // Bỏ qua nếu không có version
                    }
                    
                    SyllabusVersion version = latestVersion.get();
                    String deptName = (s.getDepartment() != null) ? s.getDepartment().getName() : "Chưa phân bộ môn";
                    String facultyName = (s.getDepartment() != null && s.getDepartment().getFaculty() != null)
                            ? s.getDepartment().getFaculty().getName() : "Chưa phân khoa";
                    String programName = (s.getCurriculum() != null) ? s.getCurriculum().getName() : "Chương trình chuẩn";

                    return StudentSyllabusSummaryDto.builder()
                            .id(version.getId())  // ✅ Sử dụng SyllabusVersion ID, không phải Subject ID
                            .code(s.getCode())
                            .nameVi(s.getCurrentNameVi())
                            .term("HK1 2024-2025")
                            .credits(s.getDefaultCredits())
                            .faculty(facultyName)
                            .program(programName)
                            .lecturerName("Bộ môn " + deptName)
                            .majorShort(s.getCode().length() >= 2 ? s.getCode().substring(0, 2) : "GEN")
                            .progress(100)
                            .tracked(trackedIds.contains(version.getId()))
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

        // ✅ Lấy Subject từ Version
        Subject subject = version.getSubject();
        if (subject == null) {
            throw new BadRequestException("Không tìm thấy môn học liên kết với đề cương!");
        }

        // (Giữ nguyên logic mapping CLO/PLO như cũ...)
        List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(version.getId());
        List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());
        List<CloPlOMapping> cloPloMappings = cloIds.isEmpty() ? Collections.emptyList() : cloPloMappingRepository.findByCloIdIn(cloIds);
        Map<String, List<String>> matrixMap = new HashMap<>();
        for (CloPlOMapping map : cloPloMappings) {
            if (map.getClo() != null && map.getPlo() != null) {
                matrixMap.computeIfAbsent(map.getClo().getCode(), k -> new ArrayList<>()).add(map.getPlo().getCode());
            }
        }
        List<StudentSyllabusDetailDto.CloDto> cloDtos = clos.stream().map(clo ->
                StudentSyllabusDetailDto.CloDto.builder()
                        .code(clo.getCode())
                        .description(clo.getDescription())
                        .bloomLevel(clo.getBloomLevel())
                        .weight(clo.getWeight() != null ? clo.getWeight().intValue() : 0)
                        .plo(matrixMap.getOrDefault(clo.getCode(), new ArrayList<>()))
                        .build()
        ).collect(Collectors.toList());

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
        List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos = assessments.stream().map(a ->
                StudentSyllabusDetailDto.AssessmentDto.builder()
                        .method(a.getName())
                        .form(a.getName() != null && a.getName().contains("Thi") ? "Tự luận/Trắc nghiệm" : "Báo cáo")
                        .criteria("Rubric " + a.getName())
                        .weight(a.getWeightPercent() != null ? a.getWeightPercent().intValue() : 0)
                        .clo(finalAssessCloMap.getOrDefault(a.getId(), new ArrayList<>()))
                        .build()
        ).collect(Collectors.toList());

        // ✅ FIX: Use SyllabusVersion ID for tracker lookup
        boolean isTracked = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), version.getId()).isPresent();
        
        String facultyName = (subject.getDepartment() != null && subject.getDepartment().getFaculty() != null) ? 
                              subject.getDepartment().getFaculty().getName() : "";
        // ✅ FIX: Lấy PLO của Subject này thay vì tất cả PLO
        List<String> ploCodeList = ploRepository.findBySubjectId(subject.getId()).stream()
                .map(PLO::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return StudentSyllabusDetailDto.builder()
                .id(subject.getId())
                .versionId(version.getId())
                .code(subject.getCode())
                .nameVi(subject.getCurrentNameVi())
                .nameEn(subject.getCurrentNameEn())
                .term("HK1 2024-2025")
                .credits(subject.getDefaultCredits())
                .faculty(facultyName)
                .lecturerName("Giảng viên phụ trách")
                .description(subject.getDescription() != null ? subject.getDescription() : "Đang cập nhật...")
                .publishedAt("2024-12-01")
                .summaryInline(subject.getDescription() != null ? subject.getDescription() : "")
                .isTracked(isTracked)
                .clos(cloDtos)
                .ploList(ploCodeList)
                .cloPloMap(matrixMap)
                .assessmentMatrix(assessmentDtos)
                .studentTasks(List.of("Tham gia lớp học", "Làm bài tập", "Tự học"))
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
    }
}