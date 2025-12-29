package vn.edu.smd.core.module.semester.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.semester.dto.SemesterRequest;
import vn.edu.smd.core.module.semester.dto.SemesterResponse;
import vn.edu.smd.core.module.semester.service.SemesterService;
import vn.edu.smd.core.module.classmodule.service.ClassService; // Import trực tiếp
import vn.edu.smd.core.module.classmodule.dto.ClassResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = "Semester Management", description = "Semester management APIs")
@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;
    private final ClassService classService;

    // Sử dụng Constructor Injection kết hợp @Lazy cho ClassService
    public SemesterController(SemesterService semesterService, @Lazy ClassService classService) {
        this.semesterService = semesterService;
        this.classService = classService;
    }

    @Operation(summary = "Get all semesters with pagination", description = "Get list of semesters with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SemesterResponse>>> getAllSemesters(Pageable pageable) {
        Page<SemesterResponse> semesters = semesterService.getAllSemesters(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(semesters)));
    }

    // ... Các method khác giữ nguyên ...

    @Operation(summary = "Get semester by ID", description = "Get semester details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemesterById(@PathVariable UUID id) {
        SemesterResponse semester = semesterService.getSemesterById(id);
        return ResponseEntity.ok(ApiResponse.success(semester));
    }

    @Operation(summary = "Get current semester", description = "Get the currently active semester")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SemesterResponse>> getCurrentSemester() {
        SemesterResponse semester = semesterService.getCurrentSemester();
        return ResponseEntity.ok(ApiResponse.success(semester));
    }

    @Operation(summary = "Create semester", description = "Create new semester")
    @PostMapping
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse semester = semesterService.createSemester(request);
        return ResponseEntity.ok(ApiResponse.success("Semester created successfully", semester));
    }

    @Operation(summary = "Update semester", description = "Update semester information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(
            @PathVariable UUID id,
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse semester = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(ApiResponse.success("Semester updated successfully", semester));
    }

    @Operation(summary = "Delete semester", description = "Delete semester by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable UUID id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.ok(ApiResponse.success("Semester deleted successfully", null));
    }

    @Operation(summary = "Get classes in semester", description = "Get list of classes in a specific semester")
    @GetMapping("/{id}/classes")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getClassesInSemester(@PathVariable UUID id, Pageable pageable) {
        // Gọi trực tiếp method, không thông qua reflection, có kiểm soát kiểu ClassResponse
        Page<ClassResponse> classPage = classService.getClassesBySemester(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(classPage.getContent()));
    }
}