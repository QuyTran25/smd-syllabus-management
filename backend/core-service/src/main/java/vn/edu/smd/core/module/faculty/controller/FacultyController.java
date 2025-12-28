package vn.edu.smd.core.module.faculty.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.common.dto.PageResponse;
import vn.edu.smd.core.module.faculty.dto.FacultyRequest;
import vn.edu.smd.core.module.faculty.dto.FacultyResponse;
import vn.edu.smd.core.module.faculty.service.FacultyService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Faculty Management", description = "Faculty management APIs")
@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @Operation(summary = "Get all faculties with pagination", description = "Get list of faculties with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FacultyResponse>>> getAllFaculties(Pageable pageable) {
        Page<FacultyResponse> faculties = facultyService.getAllFaculties(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(faculties)));
    }

    @Operation(summary = "Get all faculties", description = "Get list of all faculties without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getAllFacultiesNoPaging() {
        List<FacultyResponse> faculties = facultyService.getAllFaculties();
        return ResponseEntity.ok(ApiResponse.success(faculties));
    }

    @Operation(summary = "Get faculty by ID", description = "Get faculty details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable UUID id) {
        FacultyResponse faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @Operation(summary = "Get faculty by code", description = "Get faculty details by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyByCode(@PathVariable String code) {
        FacultyResponse faculty = facultyService.getFacultyByCode(code);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    @Operation(summary = "Create faculty", description = "Create new faculty")
    @PostMapping
    public ResponseEntity<ApiResponse<FacultyResponse>> createFaculty(@Valid @RequestBody FacultyRequest request) {
        FacultyResponse faculty = facultyService.createFaculty(request);
        return ResponseEntity.ok(ApiResponse.success("Faculty created successfully", faculty));
    }

    @Operation(summary = "Update faculty", description = "Update faculty information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
            @PathVariable UUID id, 
            @Valid @RequestBody FacultyRequest request) {
        FacultyResponse faculty = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Faculty updated successfully", faculty));
    }

    @Operation(summary = "Delete faculty", description = "Delete faculty by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable UUID id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deleted successfully", null));
    }

    @Operation(summary = "Get departments of faculty", description = "Get list of departments belonging to a faculty")
    @GetMapping("/{id}/departments")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.department.dto.DepartmentResponse>>> getDepartmentsOfFaculty(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.department.dto.DepartmentResponse> departments = facultyService.getDepartmentsOfFaculty(id);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @Operation(summary = "Get faculty statistics", description = "Get statistics of faculty (number of departments, subjects)")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<vn.edu.smd.core.module.faculty.dto.FacultyStatisticsResponse>> getFacultyStatistics(@PathVariable UUID id) {
        vn.edu.smd.core.module.faculty.dto.FacultyStatisticsResponse statistics = facultyService.getFacultyStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
