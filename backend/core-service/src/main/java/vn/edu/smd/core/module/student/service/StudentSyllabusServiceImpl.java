// package vn.edu.smd.core.module.student.service;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import vn.edu.smd.core.entity.Syllabus;
// import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;
// import vn.edu.smd.core.module.student.dto.StudentSyllabusSummaryDto;
// import vn.edu.smd.core.module.student.service.StudentSyllabusService;
// import vn.edu.smd.core.repository.SyllabusRepository;

// import java.util.*;
// import java.util.stream.Collectors;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class StudentSyllabusServiceImpl implements StudentSyllabusService {

//     private final SyllabusRepository repository;

//     @Override
//     @Transactional(readOnly = true)
//     public List<StudentSyllabusSummaryDto> getAll() {
//         return repository.findAll().stream()
//                 .map(s -> StudentSyllabusSummaryDto.builder()
//                         .id(s.getId())
//                         .code(s.getCode())
//                         .nameVi(s.getNameVi())
//                         .term(s.getTerm())
//                         .credits(s.getCredits())
//                         .faculty(s.getFaculty())
//                         .program(s.getProgram())
//                         .lecturerName(s.getLecturerName())
//                         .majorShort(s.getCode() != null && s.getCode().length() >= 2 ? s.getCode().substring(0, 2) : "IT")
//                         .progress(100)
//                         .tracked(false) // Hiện tại chưa có bảng Tracker nên mặc định false
//                         .build())
//                 .collect(Collectors.toList());
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public StudentSyllabusDetailDto getById(UUID id) {
//         // Lấy thông tin từ DB
//         Syllabus s = repository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Syllabus not found with ID: " + id));

//         // =========================================================================
//         // COPY NGUYÊN VẸN LOGIC HARDCODE CỦA BẠN VÀO ĐÂY
//         // =========================================================================
        
//         List<String> ploList = List.of("PLO01", "PLO02", "PLO03", "PLO04", "PLO05");

//         Map<String, List<String>> matrixMap = new LinkedHashMap<>();
//         matrixMap.put("CLO1", List.of("PLO01", "PLO02"));
//         matrixMap.put("CLO2", List.of("PLO02", "PLO04"));
//         matrixMap.put("CLO3", List.of("PLO02", "PLO03"));
//         matrixMap.put("CLO4", List.of("PLO04", "PLO05"));

//         List<StudentSyllabusDetailDto.CloDto> fixedClos = new ArrayList<>();
//         fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO1").description("Hiểu và giải thích các khái niệm cơ bản về CSDL quan hệ").bloomLevel("Hiểu").weight(20).plo(matrixMap.get("CLO1")).build());
//         fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO2").description("Thiết kế mô hình ER và chuẩn hóa đến dạng 3NF").bloomLevel("Áp dụng").weight(25).plo(matrixMap.get("CLO2")).build());
//         fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO3").description("Viết các truy vấn SQL phức tạp (JOIN, Subquery, Aggregate)").bloomLevel("Áp dụng").weight(30).plo(matrixMap.get("CLO3")).build());
//         fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO4").description("Phân tích và tối ưu hóa hiệu suất truy vấn").bloomLevel("Phân tích").weight(25).plo(matrixMap.get("CLO4")).build());

//         List<StudentSyllabusDetailDto.AssessmentDto> fixedAssessments = new ArrayList<>();
//         fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Thi giữa kỳ").form("Thi").criteria("A1.2").weight(30).clo(List.of("CLO1", "CLO2")).build());
//         fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Thi cuối kỳ").form("Thi").criteria("A2.1").weight(40).clo(List.of("CLO1", "CLO2", "CLO3", "CLO4")).build());
//         fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Dự án nhóm").form("Nhóm").criteria("A3.1").weight(20).clo(List.of("CLO3", "CLO4")).build());
//         fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Bài tập thực hành").form("Cá nhân").criteria("A1.1").weight(10).clo(List.of("CLO2", "CLO3")).build());

//         return StudentSyllabusDetailDto.builder()
//                 .id(s.getId())
//                 .code(s.getCode())
//                 .nameVi(s.getNameVi())
//                 .nameEn(s.getNameEn())
//                 .term(s.getTerm())
//                 .credits(s.getCredits())
//                 .faculty(s.getFaculty())
//                 .program(s.getProgram())
//                 .lecturerName(s.getLecturerName())
//                 .lecturerEmail(s.getLecturerEmail())
//                 .description(s.getDescription())
//                 .publishedAt("2024-12-01")
//                 .summaryInline(s.getDescription())
//                 .clos(fixedClos)
//                 .ploList(ploList)
//                 .cloPloMap(matrixMap)
//                 .assessmentMatrix(fixedAssessments)
//                 .timeAllocation(new StudentSyllabusDetailDto.TimeAllocationDto(30, 30, 90))
//                 .studentTasks(List.of(
//                         "- Tham gia đầy đủ các buổi học (tối thiểu 80% số tiết)",
//                         "- Hoàn thành đầy đủ các bài tập được giao",
//                         "- Tham gia tích cực vào các hoạt động học tập nhóm",
//                         "- Tự nghiên cứu và chuẩn bị trước nội dung bài học",
//                         "- Thực hiện dự án nhóm theo yêu cầu"
//                 ))
//                 .textbooks(List.of("1. Database System Concepts - Silberschatz (7th Edition)", "2. SQL Queries for Mere Mortals - Viescas"))
//                 .references(List.of("1. Fundamentals of Database Systems - Elmasri & Navathe", "2. MySQL Documentation"))
//                 .build();
//     }

//     @Override
//     public void toggleTrack(UUID id) {
//         // Vì chưa có bảng Tracker nên tạm thời chỉ log ra console để không lỗi code
//         // Khi nào bạn tạo bảng StudentSyllabusTracker thì viết logic lưu DB vào đây
//         if (repository.existsById(id)) {
//             log.info("Yêu cầu Track/Untrack môn học ID: {} (Chưa lưu DB)", id);
//         } else {
//             throw new RuntimeException("Syllabus not found with ID: " + id);
//         }
//     }
// }


package vn.edu.smd.core.module.student.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Import Entities (Chú ý CLO, PLO viết hoa theo file bạn gửi)
import vn.edu.smd.core.entity.*; 
import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusSummaryDto;
import vn.edu.smd.core.module.student.repository.StudentSyllabusTrackerRepository;
import vn.edu.smd.core.module.student.service.StudentSyllabusService;

// Import Repositories
import vn.edu.smd.core.repository.*; 

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSyllabusServiceImpl implements StudentSyllabusService {

    // Inject Repositories
    private final SubjectRepository subjectRepository;
    private final SyllabusVersionRepository versionRepository;
    private final CLORepository cloRepository; // CLO viết hoa
    private final PLORepository ploRepository; // PLO viết hoa
    private final CloPlOMappingRepository cloPloMappingRepository;
    private final AssessmentSchemeRepository assessmentRepository;
    private final AssessmentCloMappingRepository assessmentCloMappingRepository;
    private final StudentSyllabusTrackerRepository trackerRepository;

    private final UUID MOCK_STUDENT_ID = UUID.fromString("9a456345-0d23-4567-89ab-cdef01234567");

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
        // 1. Tìm Subject
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại ID: " + subjectId));

        // 2. Tìm SyllabusVersion MỚI NHẤT
        SyllabusVersion version = versionRepository.findFirstBySubjectIdOrderByCreatedAtDesc(subjectId)
                .orElseThrow(() -> new RuntimeException("Chưa có đề cương cho môn học này"));

        // 3. Lấy CLO (Sắp xếp theo Code)
        List<CLO> clos = cloRepository.findBySyllabusVersionIdOrderByCodeAsc(version.getId());
        List<UUID> cloIds = clos.stream().map(CLO::getId).collect(Collectors.toList());

        // 4. Lấy PLO
        List<PLO> allPlos = ploRepository.findAll();
        List<String> ploCodeList = allPlos.stream()
                .map(PLO::getCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 5. Lấy Mapping CLO-PLO (Batch Fetch)
        // Chú ý: Entity của bạn là CloPlOMapping
        List<CloPlOMapping> cloPloMappings = cloPloMappingRepository.findByCloIdIn(cloIds);
        Map<String, List<String>> matrixMap = new HashMap<>();
        for (CloPlOMapping map : cloPloMappings) {
            String cloCode = map.getClo().getCode();
            String ploCode = map.getPlo().getCode();
            matrixMap.computeIfAbsent(cloCode, k -> new ArrayList<>()).add(ploCode);
        }

        // 6. Build DTO cho CLO
        List<StudentSyllabusDetailDto.CloDto> cloDtos = clos.stream().map(clo -> 
            StudentSyllabusDetailDto.CloDto.builder()
                .code(clo.getCode())
                .description(clo.getDescription())
                .bloomLevel(clo.getBloomLevel())
                .weight(clo.getWeight() != null ? clo.getWeight().intValue() : 0)
                .plo(matrixMap.getOrDefault(clo.getCode(), new ArrayList<>()))
                .build()
        ).collect(Collectors.toList());

        // 7. Lấy Assessment (Sắp xếp theo ngày tạo)
        List<AssessmentScheme> assessments = assessmentRepository.findBySyllabusVersionIdOrderByCreatedAtAsc(version.getId());
        List<UUID> assessmentIds = assessments.stream().map(AssessmentScheme::getId).collect(Collectors.toList());

        // 8. Lấy Mapping Assessment-CLO (Batch Fetch)
        List<AssessmentCloMapping> assessMappings = assessmentCloMappingRepository.findByAssessmentSchemeIdIn(assessmentIds);
        Map<UUID, List<String>> assessCloMap = assessMappings.stream()
            .collect(Collectors.groupingBy(
                m -> m.getAssessmentScheme().getId(),
                Collectors.mapping(m -> m.getClo().getCode(), Collectors.toList())
            ));

        // 9. Build DTO cho Assessment
        List<StudentSyllabusDetailDto.AssessmentDto> assessmentDtos = assessments.stream().map(a -> 
            StudentSyllabusDetailDto.AssessmentDto.builder()
                .method(a.getName())
                .form(a.getName().contains("Thi") ? "Tự luận/Trắc nghiệm" : "Báo cáo")
                .criteria("Rubric " + a.getName())
                .weight(a.getWeightPercent() != null ? a.getWeightPercent().intValue() : 0)
                .clo(assessCloMap.getOrDefault(a.getId(), new ArrayList<>()))
                .build()
        ).collect(Collectors.toList());

        // 10. Trả về DTO hoàn chỉnh
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
}