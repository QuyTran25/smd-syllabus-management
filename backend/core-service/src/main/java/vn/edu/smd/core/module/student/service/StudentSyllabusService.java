package vn.edu.smd.core.module.student.service;

import vn.edu.smd.core.module.student.dto.StudentSyllabusDetailDto;
import vn.edu.smd.core.module.student.dto.StudentSyllabusSummaryDto;
import vn.edu.smd.core.module.student.dto.ReportIssueDto; // 👈 Nhớ import DTO này

import java.util.List;
import java.util.UUID;

public interface StudentSyllabusService {
    
    // Lấy danh sách tóm tắt
    List<StudentSyllabusSummaryDto> getAll();

    // Lấy chi tiết
    StudentSyllabusDetailDto getById(UUID id);

    // Chức năng: Theo dõi / Bỏ theo dõi
    void toggleTrack(UUID id);

    // ⭐ Chức năng mới: Báo cáo lỗi cho Admin
    void reportIssue(ReportIssueDto dto); 
}