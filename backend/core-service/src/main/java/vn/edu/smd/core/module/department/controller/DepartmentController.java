package vn.edu.smd.core.module.department.controller;

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
import vn.edu.smd.core.module.department.dto.DepartmentRequest;
import vn.edu.smd.core.module.department.dto.DepartmentResponse;
import vn.edu.smd.core.module.department.service.DepartmentService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Department Management", description = "Department management APIs")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Get all departments with pagination", description = "Get list of departments with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> getAllDepartments(Pageable pageable) {
        Page<DepartmentResponse> departments = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(departments)));
    }

    @Operation(summary = "Get all departments", description = "Get list of all departments without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartmentsNoPaging() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @Operation(summary = "Get department by ID", description = "Get department details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable UUID id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @Operation(summary = "Get department by code", description = "Get department details by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentByCode(@PathVariable String code) {
        DepartmentResponse department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @Operation(summary = "Get departments by faculty", description = "Get list of departments by faculty ID")
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartmentsByFaculty(@PathVariable UUID facultyId) {
        List<DepartmentResponse> departments = departmentService.getDepartmentsByFaculty(facultyId);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @Operation(summary = "Create department", description = "Create new department")
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", department));
    }

    @Operation(summary = "Update department", description = "Update department information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable UUID id, 
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", department));
    }

    @Operation(summary = "Delete department", description = "Delete department by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }

    @Operation(summary = "Get subjects of department", description = "Get list of subjects belonging to a department")
    @GetMapping("/{id}/subjects")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.subject.dto.SubjectResponse>>> getSubjectsOfDepartment(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.subject.dto.SubjectResponse> subjects = departmentService.getSubjectsOfDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Get lecturers of department", description = "Get list of lecturers belonging to a department")
    @GetMapping("/{id}/lecturers")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.user.dto.UserResponse>>> getLecturersOfDepartment(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.user.dto.UserResponse> lecturers = departmentService.getLecturersOfDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(lecturers));
    }
}
