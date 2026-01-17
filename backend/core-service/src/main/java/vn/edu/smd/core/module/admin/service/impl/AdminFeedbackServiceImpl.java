package vn.edu.smd.core.module.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.entity.SyllabusVersion;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.module.admin.dto.AdminResolveIssueDto;
import vn.edu.smd.core.module.admin.service.AdminFeedbackService;
import vn.edu.smd.core.repository.SyllabusErrorReportRepository;
import vn.edu.smd.core.repository.SyllabusVersionRepository;
import vn.edu.smd.core.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFeedbackServiceImpl implements AdminFeedbackService {

    private final SyllabusErrorReportRepository errorReportRepository;
    private final SyllabusVersionRepository syllabusVersionRepository;
    private final UserRepository userRepository;

    // TODO: Thay bằng ID Admin thật khi tích hợp Spring Security
    private final UUID MOCK_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111"); 

    @Override
    public List<SyllabusErrorReport> getPendingIssues() {
        // Lấy tất cả lỗi có status là PENDING, sắp xếp mới nhất lên đầu
        return errorReportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Override
    @Transactional
    public void resolveIssue(AdminResolveIssueDto dto) {
        // 1. Tìm báo cáo lỗi
        SyllabusErrorReport report = errorReportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo lỗi với ID: " + dto.getReportId()));

        // 2. Lấy thông tin Admin xử lý (Mock hoặc từ Security Context)
        User adminUser = userRepository.findById(MOCK_ADMIN_ID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Admin user"));

        // 3. Xử lý logic theo Action
        if ("APPROVE".equalsIgnoreCase(dto.getAction())) {
            // A. Nếu DUYỆT (Chấp nhận lỗi đúng -> Mở quyền sửa cho GV)
            report.setStatus("RESOLVED");
            
            // Tìm Syllabus Version liên quan để mở khóa
            SyllabusVersion syllabusVersion = report.getSyllabusVersion();
            if (syllabusVersion != null) {
                syllabusVersion.setIsEditEnabled(true); // ✅ Mở khóa chỉnh sửa
                syllabusVersion.setEditEnabledBy(adminUser);
                syllabusVersion.setEditEnabledAt(LocalDateTime.now());
                
                syllabusVersionRepository.save(syllabusVersion);
                log.info("Admin đã mở quyền chỉnh sửa cho Syllabus: {}", syllabusVersion.getSnapSubjectCode());
            }

        } else if ("REJECT".equalsIgnoreCase(dto.getAction())) {
            // B. Nếu TỪ CHỐI (Lỗi sai -> Không làm gì cả, chỉ đổi status)
            report.setStatus("REJECTED");
        } else {
            throw new IllegalArgumentException("Hành động không hợp lệ: " + dto.getAction());
        }

        // 4. Cập nhật thông tin phản hồi vào báo cáo
        report.setAdminResponse(dto.getAdminComment());
        report.setResolvedBy(adminUser);
        report.setResolvedAt(LocalDateTime.now());
        report.setRespondedBy(adminUser); // Có thể dùng chung trường này
        report.setRespondedAt(LocalDateTime.now());

        errorReportRepository.save(report);
        log.info("Đã xử lý báo lỗi {} với trạng thái {}", report.getId(), report.getStatus());
    }
}