package vn.edu.smd.core.module.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.module.student.dto.*;
import vn.edu.smd.core.module.student.service.PdfService;
import vn.edu.smd.core.module.student.service.StudentSyllabusService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/syllabi")
@RequiredArgsConstructor
public class StudentSyllabusController {

    private final StudentSyllabusService service;
    private final PdfService pdfService;

    @GetMapping
    public List<StudentSyllabusSummaryDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentSyllabusDetailDto> getById(@PathVariable UUID id) {  // Thêm ResponseEntity để handle error nếu cần
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/track")
    public ResponseEntity<?> toggleTrack(@PathVariable UUID id) {
        service.toggleTrack(id);
        return ResponseEntity.ok(java.util.Map.of(
            "success", true,
            "message", "Đã cập nhật trạng thái theo dõi thành công!",
            "syllabusId", id
        ));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadSyllabusPdf(@PathVariable UUID id) {
        // Lấy dữ liệu từ service
        StudentSyllabusDetailDto data = service.getById(id); 
        
        // Tạo PDF bằng dữ liệu DTO
        byte[] pdfBytes = pdfService.generateSyllabusPdf(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        String code = (data.getCode() != null) ? data.getCode() : "Unknown";
        String filename = "Syllabus_" + code + ".pdf";
        
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/issues/report")
    public ResponseEntity<?> reportIssue(@RequestBody ReportIssueDto dto) {
        service.reportIssue(dto);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }
}