package vn.edu.smd.core.module.classmodule.controller;

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
import vn.edu.smd.core.module.classmodule.dto.ClassRequest;
import vn.edu.smd.core.module.classmodule.dto.ClassResponse;
import vn.edu.smd.core.module.classmodule.service.ClassService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Class Management", description = "Class management APIs")
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @Operation(summary = "Get all classes", description = "Get list of classes with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClassResponse>>> getAllClasses(Pageable pageable) {
        Page<ClassResponse> classes = classService.getAllClasses(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(classes)));
    }

    @Operation(summary = "Get class by ID", description = "Get class details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassResponse>> getClassById(@PathVariable UUID id) {
        ClassResponse classResponse = classService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.success(classResponse));
    }

    @Operation(summary = "Create class", description = "Create new class")
    @PostMapping
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody ClassRequest request) {
        ClassResponse classResponse = classService.createClass(request);
        return ResponseEntity.ok(ApiResponse.success("Class created successfully", classResponse));
    }

    @Operation(summary = "Update class", description = "Update class information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClass(
            @PathVariable UUID id,
            @Valid @RequestBody ClassRequest request) {
        ClassResponse classResponse = classService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Class updated successfully", classResponse));
    }

    @Operation(summary = "Delete class", description = "Delete class by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable UUID id) {
        classService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success("Class deleted successfully", null));
    }

    @Operation(summary = "Get students in class", description = "Get list of student IDs in a class")
    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<UUID>>> getStudentsInClass(@PathVariable UUID id) {
        List<UUID> students = classService.getStudentsInClass(id);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @Operation(summary = "Add student to class", description = "Add a student to a class")
    @PostMapping("/{id}/students")
    public ResponseEntity<ApiResponse<Void>> addStudentToClass(
            @PathVariable UUID id,
            @RequestParam UUID studentId) {
        classService.addStudentToClass(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("Student added to class successfully", null));
    }

    @Operation(summary = "Remove student from class", description = "Remove a student from a class")
    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<ApiResponse<Void>> removeStudentFromClass(
            @PathVariable UUID id,
            @PathVariable UUID studentId) {
        classService.removeStudentFromClass(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("Student removed from class successfully", null));
    }
}
