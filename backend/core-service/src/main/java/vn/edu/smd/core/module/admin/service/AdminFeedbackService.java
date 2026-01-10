package vn.edu.smd.core.module.admin.service;

import vn.edu.smd.core.entity.SyllabusErrorReport;
import vn.edu.smd.core.module.admin.dto.AdminResolveIssueDto;

import java.util.List;

public interface AdminFeedbackService {
    // Lấy danh sách các lỗi đang chờ xử lý
    List<SyllabusErrorReport> getPendingIssues();

    // Xử lý lỗi (Duyệt/Từ chối)
    void resolveIssue(AdminResolveIssueDto dto);
}