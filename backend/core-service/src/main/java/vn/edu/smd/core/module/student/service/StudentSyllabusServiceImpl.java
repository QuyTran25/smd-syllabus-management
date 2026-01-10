package vn.edu.smd.core.module.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.student.dto.ReportIssueDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusSummaryDto;
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

    // =========================================================================
    // HELPER: Lấy sinh viên hiện tại từ Token (Thay thế MOCK_STUDENT_ID)
    // =========================================================================
    private User getCurrentStudent() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(">>> Đang lấy thông tin sinh viên từ Token: {}", principal);

        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên! (Vui lòng đăng nhập lại)"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentSyllabusSummaryDto> getAll() {
        // Lấy ID thật của sinh viên đang đăng nhập
        User student = getCurrentStudent();

        Set<UUID> trackedIds = trackerRepository.findByStudentId(student.getId()).stream()
                .map(StudentSyllabusTracker::getSyllabusId)
                .collect(Collectors.toSet());

        return subjectRepository.findAll().stream()
                .map(s -> StudentSyllabusSummaryDto.builder()
                        .id(s.getId())
                        .code(s.getCode())
                        .nameVi(s.getCurrentNameVi())
                        .term("HK1 2024-2025")
                        .credits(s.getDefaultCredits())
                        .faculty(s.getDepartment() != null && s.getDepartment().getFaculty() != null 
                                ? s.getDepartment().getFaculty().getName() : "Khoa CNTT")
                        .program(s.getCurriculum() != null ? s.getCurriculum().getName() : "Chương trình chuẩn")
                        .lecturerName("Bộ môn " + (s.getDepartment() != null ? s.getDepartment().getName() : "CNTT"))
                        .majorShort(s.getCode().length() >= 2 ? s.getCode().substring(0, 2) : "IT")
                        .progress(100)
                        .tracked(trackedIds.contains(s.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentSyllabusDetailDto getById(UUID subjectId) {
        // Lấy thông tin sinh viên để check đã track hay chưa
        User student = getCurrentStudent();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại ID: " + subjectId));

        SyllabusVersion version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(subjectId)
                .orElseThrow(() -> new RuntimeException("Chưa có đề cương cho môn học này"));

        // ... (Giữ nguyên logic lấy CLO/PLO/Mapping cũ của bạn) ...
        List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(version.getId());
        List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());

        List<PLO> allPlos = ploRepository.findAll();
        List<String> ploCodeList = allPlos.stream()
                .map(PLO::getCode).distinct().sorted().collect(Collectors.toList());

        List<CloPlOMapping> cloPloMappings = cloPloMappingRepository.findByCloIdIn(cloIds);
        Map<String, List<String>> matrixMap = new HashMap<>();
        for (CloPlOMapping map : cloPloMappings) {
            String cloCode = map.getClo().getCode();
            String ploCode = map.getPlo().getCode();
            matrixMap.computeIfAbsent(cloCode, k -> new ArrayList<>()).add(ploCode);
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

        List<AssessmentCloMapping> assessMappings = assessmentCloMappingRepository.findByAssessmentSchemeIdIn(assessmentIds);
        Map<UUID, List<String>> assessCloMap = assessMappings.stream()
            .collect(Collectors.groupingBy(
                m -> m.getAssessmentScheme().getId(),
                Collectors.mapping(m -> m.getClo().getCode(), Collectors.toList())
            ));

        List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos = assessments.stream().map(a -> 
            StudentSyllabusDetailDto.AssessmentDto.builder()
                .method(a.getName())
                .form(a.getName().contains("Thi") ? "Tự luận/Trắc nghiệm" : "Báo cáo")
                .criteria("Rubric " + a.getName())
                .weight(a.getWeightPercent() != null ? a.getWeightPercent().intValue() : 0)
                .clo(assessCloMap.getOrDefault(a.getId(), new ArrayList<>()))
                .build()
        ).collect(Collectors.toList());

        // Check track dynamic
        boolean isTracked = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), subjectId).isPresent();

        return StudentSyllabusDetailDto.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .nameVi(subject.getCurrentNameVi())
                .nameEn(subject.getCurrentNameEn())
                .term("HK1 2024-2025")
                .credits(subject.getDefaultCredits())
                .faculty(subject.getDepartment() != null ? subject.getDepartment().getName() : "")
                .lecturerName("Giảng viên phụ trách")
                .description(subject.getDescription())
                .publishedAt("2024-12-01")
                .summaryInline(subject.getDescription())
                .isTracked(isTracked)
                .clos(cloDtos)
                .ploList(ploCodeList)
                .cloPloMap(matrixMap)
                .assessmentMatrix(assessmentDtos)
                .studentTasks(List.of("Tham gia lớp học", "Làm bài tập", "Tự học"))
                .timeAllocation(new StudentSyllabusDetailDto.TimeAllocationDto(
                    version.getTheoryHours(), 
                    version.getPracticeHours(), 
                    version.getSelfStudyHours()))
                .build();
    }

    @Override
    @Transactional
    public void toggleTrack(UUID syllabusId) {
        User student = getCurrentStudent();

        if (!subjectRepository.existsById(syllabusId)) {
            throw new RuntimeException("Môn học không tồn tại ID: " + syllabusId);
        }

        Optional<StudentSyllabusTracker> existing = trackerRepository.findByStudentIdAndSyllabusId(student.getId(), syllabusId);

        if (existing.isPresent()) {
            trackerRepository.delete(existing.get());
        } else {
            StudentSyllabusTracker tracker = new StudentSyllabusTracker();
            tracker.setStudentId(student.getId());
            tracker.setSyllabusId(syllabusId);
            tracker.setCreatedAt(LocalDateTime.now());
            trackerRepository.save(tracker);
        }
    }

    @Override
    @Transactional
    public void reportIssue(ReportIssueDto dto) {
        // 1. Tìm phiên bản đề cương
        SyllabusVersion version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(dto.getSyllabusId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề cương!"));

        // 2. Lấy User sinh viên THẬT (Fix lỗi 500)
        User student = getCurrentStudent();
        
        log.info(">>> Đang tạo báo cáo lỗi cho User: {} (ID: {})", student.getEmail(), student.getId());

        // 3. Mapping Section
        ErrorReportSection sectionEnum;
        String inputSection = (dto.getSection() != null) ? dto.getSection().toLowerCase() : "other";

        // Logic mapping đơn giản (bạn có thể giữ nguyên hoặc custom)
        if (inputSection.contains("info")) sectionEnum = ErrorReportSection.SUBJECT_INFO;
        else if (inputSection.contains("object")) sectionEnum = ErrorReportSection.OBJECTIVES;
        else if (inputSection.contains("assess")) sectionEnum = ErrorReportSection.ASSESSMENT_MATRIX;
        else if (inputSection.contains("clo")) sectionEnum = ErrorReportSection.CLO;
        else sectionEnum = ErrorReportSection.OTHER;

        // 4. Tạo thực thể Báo lỗi và lưu
        SyllabusErrorReport report = SyllabusErrorReport.builder()
                .syllabusVersion(version)
                .user(student) // <-- User lấy từ Token, đảm bảo tồn tại
                .title("Báo lỗi từ sinh viên: " + student.getFullName())
                .description(dto.getDescription())
                .section(sectionEnum)
                .type(FeedbackType.ERROR)
                .status("PENDING")
                .editEnabled(false)
                .build();

        errorReportRepository.save(report);
        log.info(">>> SUCCESS: Đã lưu báo cáo lỗi ID: {}", report.getId());
    }
}