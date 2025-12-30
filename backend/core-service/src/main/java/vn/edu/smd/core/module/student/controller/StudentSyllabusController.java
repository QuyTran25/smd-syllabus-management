package vn.edu.smd.core.module.student.controller;

import lombok.RequiredArgsConstructor;
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
}