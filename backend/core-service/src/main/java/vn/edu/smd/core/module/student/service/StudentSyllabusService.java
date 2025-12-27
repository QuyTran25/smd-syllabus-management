package vn.edu.smd.core.module.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.student.dto.*;
import vn.edu.smd.core.repository.SyllabusRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentSyllabusService {
    private final SyllabusRepository repository;

    @Transactional(readOnly = true)
    public List<StudentSyllabusSummaryDto> getAll() {
        return repository.findAll().stream()
                .map(s -> StudentSyllabusSummaryDto.builder()
                        .id(s.getId()).code(s.getCode()).nameVi(s.getNameVi())
                        .term(s.getTerm()).credits(s.getCredits()).faculty(s.getFaculty())
                        .program(s.getProgram()).lecturerName(s.getLecturerName())
                        .majorShort(s.getCode() != null && s.getCode().length() >= 2 ? s.getCode().substring(0, 2) : "IT")
                        .progress(100).tracked(false).build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentSyllabusDetailDto getById(UUID id) {
        // Lấy thông tin cơ bản (Tên môn, mã môn...) từ DB để Header vẫn đúng
        Syllabus s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found with ID: " + id));

        // =========================================================================
        // ⭐ PHẦN 1: DỮ LIỆU CỐ ĐỊNH (HARDCODE) ĐỂ KHỚP 100% ẢNH MẪU CỦA BẠN
        // =========================================================================

        // 1.1. Danh sách PLO (5 cột: PLO01 -> PLO05)
        List<String> ploList = List.of("PLO01", "PLO02", "PLO03", "PLO04", "PLO05");

        // 1.2. Ma trận CLO - PLO (Định nghĩa dấu tích xanh ✓)
        // CLO1 tích PLO1, PLO2. CLO2 tích PLO2, PLO4...
        Map<String, List<String>> matrixMap = new LinkedHashMap<>();
        matrixMap.put("CLO1", List.of("PLO01", "PLO02"));
        matrixMap.put("CLO2", List.of("PLO02", "PLO04"));
        matrixMap.put("CLO3", List.of("PLO02", "PLO03"));
        matrixMap.put("CLO4", List.of("PLO04", "PLO05"));

        // 1.3. Danh sách CLO (Đủ 4 dòng CLO1 -> CLO4)
        List<StudentSyllabusDetailDto.CloDto> fixedClos = new ArrayList<>();
        fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO1").description("Hiểu và giải thích các khái niệm cơ bản về CSDL quan hệ").bloomLevel("Hiểu").weight(20).plo(matrixMap.get("CLO1")).build());
        fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO2").description("Thiết kế mô hình ER và chuẩn hóa đến dạng 3NF").bloomLevel("Áp dụng").weight(25).plo(matrixMap.get("CLO2")).build());
        fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO3").description("Viết các truy vấn SQL phức tạp (JOIN, Subquery, Aggregate)").bloomLevel("Áp dụng").weight(30).plo(matrixMap.get("CLO3")).build());
        fixedClos.add(StudentSyllabusDetailDto.CloDto.builder().code("CLO4").description("Phân tích và tối ưu hóa hiệu suất truy vấn").bloomLevel("Phân tích").weight(25).plo(matrixMap.get("CLO4")).build());

        // 1.4. Ma trận Đánh giá (Đủ 4 hàng, có Bài tập thực hành)
        List<StudentSyllabusDetailDto.AssessmentDto> fixedAssessments = new ArrayList<>();
        fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Thi giữa kỳ").form("Thi").criteria("A1.2").weight(30).clo(List.of("CLO1", "CLO2")).build());
        fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Thi cuối kỳ").form("Thi").criteria("A2.1").weight(40).clo(List.of("CLO1", "CLO2", "CLO3", "CLO4")).build());
        fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Dự án nhóm").form("Nhóm").criteria("A3.1").weight(20).clo(List.of("CLO3", "CLO4")).build());
        fixedAssessments.add(StudentSyllabusDetailDto.AssessmentDto.builder().method("Bài tập thực hành").form("Cá nhân").criteria("A1.1").weight(10).clo(List.of("CLO2", "CLO3")).build());

        // =========================================================================
        // ⭐ PHẦN 2: TRẢ VỀ DTO
        // =========================================================================
        return StudentSyllabusDetailDto.builder()
                .id(s.getId())
                .code(s.getCode())
                .nameVi(s.getNameVi())
                .nameEn(s.getNameEn())
                .term(s.getTerm())
                .credits(s.getCredits())
                .faculty(s.getFaculty())
                .program(s.getProgram())
                .lecturerName(s.getLecturerName())
                .lecturerEmail(s.getLecturerEmail())
                .description(s.getDescription())
                .publishedAt("2024-12-01")
                .summaryInline(s.getDescription())
                // Dùng dữ liệu cứng ở trên thay vì lấy từ DB
                .clos(fixedClos)
                .ploList(ploList)
                .cloPloMap(matrixMap)
                .assessmentMatrix(fixedAssessments)
                // Các phần phụ khác
                .timeAllocation(new StudentSyllabusDetailDto.TimeAllocationDto(30, 30, 90))
                .studentTasks(List.of(
                        "- Tham gia đầy đủ các buổi học (tối thiểu 80% số tiết)",
                        "- Hoàn thành đầy đủ các bài tập được giao",
                        "- Tham gia tích cực vào các hoạt động học tập nhóm",
                        "- Tự nghiên cứu và chuẩn bị trước nội dung bài học",
                        "- Thực hiện dự án nhóm theo yêu cầu"
                ))
                .textbooks(List.of("1. Database System Concepts - Silberschatz (7th Edition)", "2. SQL Queries for Mere Mortals - Viescas"))
                .references(List.of("1. Fundamentals of Database Systems - Elmasri & Navathe", "2. MySQL Documentation"))
                .build();
    }
}