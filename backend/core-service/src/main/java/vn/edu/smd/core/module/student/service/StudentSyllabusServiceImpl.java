package vn.edu.smd.core.module.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final UUID MOCK_STUDENT_ID = UUID.fromString("1add472c-4664-4a22-8890-b7c6215996d2");

    @Override
    @Transactional(readOnly = true)
    public List<StudentSyllabusSummaryDto> getAll() {
        Set<UUID> trackedIds = trackerRepository.findByStudentId(MOCK_STUDENT_ID).stream()
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
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại ID: " + subjectId));

        SyllabusVersion version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(subjectId)
                .orElseThrow(() -> new RuntimeException("Chưa có đề cương cho môn học này"));

        List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(version.getId());
        List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());

        List<PLO> allPlos = ploRepository.findAll();
        List<String> ploCodeList = allPlos.stream()
                .map(PLO::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

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

        boolean isTracked = trackerRepository.findByStudentIdAndSyllabusId(MOCK_STUDENT_ID, subjectId).isPresent();

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
        if (!subjectRepository.existsById(syllabusId)) {
            throw new RuntimeException("Môn học không tồn tại ID: " + syllabusId);
        }

        Optional<StudentSyllabusTracker> existing = trackerRepository.findByStudentIdAndSyllabusId(MOCK_STUDENT_ID, syllabusId);

        if (existing.isPresent()) {
            trackerRepository.delete(existing.get());
        } else {
            StudentSyllabusTracker tracker = new StudentSyllabusTracker();
            tracker.setStudentId(MOCK_STUDENT_ID);
            tracker.setSyllabusId(syllabusId);
            tracker.setCreatedAt(LocalDateTime.now());
            trackerRepository.save(tracker);
        }
    }

    @Override
    @Transactional
    public void reportIssue(ReportIssueDto dto) {
        // 1. Tìm phiên bản đề cương mới nhất của môn học
        SyllabusVersion version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(dto.getSyllabusId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề cương!"));

        // 2. Lấy User sinh viên (Sử dụng Mock ID bạn đang dùng)
        System.out.println(">>> ĐANG TÌM SINH VIÊN VỚI ID: " + MOCK_STUDENT_ID);
        User student = userRepository.findById(MOCK_STUDENT_ID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));

        // 3. Mapping Section sang Enum chuẩn (Dựa trên V10)
        ErrorReportSection sectionEnum;
        String inputSection = (dto.getSection() != null) ? dto.getSection().toLowerCase() : "other";

        sectionEnum = switch (inputSection) {
            case "subject_info" -> ErrorReportSection.SUBJECT_INFO;
            case "objectives" -> ErrorReportSection.OBJECTIVES;
            case "assessment_matrix" -> ErrorReportSection.ASSESSMENT_MATRIX;
            case "clo" -> ErrorReportSection.CLO;
            case "clo_plo_matrix" -> ErrorReportSection.CLO_PLO_MATRIX;
            case "textbook" -> ErrorReportSection.TEXTBOOK;
            case "reference" -> ErrorReportSection.REFERENCE;
            default -> ErrorReportSection.OTHER;
        };

        // 4. Tạo thực thể Báo lỗi và lưu vào DB
        SyllabusErrorReport report = SyllabusErrorReport.builder()
                .syllabusVersion(version)
                .user(student)
                .title("Báo lỗi từ sinh viên: " + sectionEnum.getDisplayName())
                .description(dto.getDescription())
                .section(sectionEnum)
                .type(FeedbackType.ERROR) // Mặc định là lỗi theo V8
                .status("PENDING")        // Trạng thái chờ xử lý theo V6
                .build();

        errorReportRepository.save(report);
        log.info("Đã lưu báo cáo lỗi cho môn: {}", version.getSnapSubjectNameVi());
    }
}