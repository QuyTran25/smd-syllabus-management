package vn.edu.smd.core.module.student.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.module.student.dto.*;
import vn.edu.smd.core.module.student.service.StudentSyllabusService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/syllabi")
@RequiredArgsConstructor
public class StudentSyllabusController {
    private final StudentSyllabusService service;

    @GetMapping
    public List<StudentSyllabusSummaryDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public StudentSyllabusDetailDto getById(@PathVariable UUID id) {
        return service.getById(id);
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
}