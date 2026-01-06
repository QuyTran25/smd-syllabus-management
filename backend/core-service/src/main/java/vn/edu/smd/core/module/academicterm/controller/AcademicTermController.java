package vn.edu.smd.core.module.academicterm.controller;

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
import vn.edu.smd.core.module.academicterm.dto.AcademicTermRequest;
import vn.edu.smd.core.module.academicterm.dto.AcademicTermResponse;
import vn.edu.smd.core.module.academicterm.service.AcademicTermService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Academic Term Management", description = "Academic term (học kỳ) management APIs")
@RestController
@RequestMapping("/api/academic-terms")
@RequiredArgsConstructor
public class AcademicTermController {

    private final AcademicTermService academicTermService;

    @Operation(summary = "Get all academic terms with pagination", description = "Get list of academic terms with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AcademicTermResponse>>> getAllAcademicTerms(Pageable pageable) {
        Page<AcademicTermResponse> terms = academicTermService.getAllAcademicTerms(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(terms)));
    }

    @Operation(summary = "Get all academic terms", description = "Get list of all academic terms without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AcademicTermResponse>>> getAllAcademicTermsNoPaging() {
        List<AcademicTermResponse> terms = academicTermService.getAllAcademicTerms();
        return ResponseEntity.ok(ApiResponse.success(terms));
    }

    @Operation(summary = "Get academic term by ID", description = "Get academic term details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicTermResponse>> getAcademicTermById(@PathVariable UUID id) {
        AcademicTermResponse term = academicTermService.getAcademicTermById(id);
        return ResponseEntity.ok(ApiResponse.success(term));
    }

    @Operation(summary = "Get academic term by code", description = "Get academic term details by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AcademicTermResponse>> getAcademicTermByCode(@PathVariable String code) {
        AcademicTermResponse term = academicTermService.getAcademicTermByCode(code);
        return ResponseEntity.ok(ApiResponse.success(term));
    }

    @Operation(summary = "Get active academic terms", description = "Get list of all active academic terms")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AcademicTermResponse>>> getActiveAcademicTerms() {
        List<AcademicTermResponse> terms = academicTermService.getActiveAcademicTerms();
        return ResponseEntity.ok(ApiResponse.success(terms));
    }

    @Operation(summary = "Get current academic year", description = "Get the currently active academic year")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<AcademicTermResponse>> getCurrentAcademicYear() {
        AcademicTermResponse term = academicTermService.getCurrentAcademicYear();
        return ResponseEntity.ok(ApiResponse.success(term));
    }

    @Operation(summary = "Get semesters by academic year", description = "Get list of semesters in an academic year")
    @GetMapping("/{id}/semesters")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.semester.dto.SemesterResponse>>> getSemestersByAcademicYear(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.semester.dto.SemesterResponse> semesters = academicTermService.getSemestersByAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.success(semesters));
    }

    @Operation(summary = "Create academic term", description = "Create new academic term")
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicTermResponse>> createAcademicTerm(@Valid @RequestBody AcademicTermRequest request) {
        AcademicTermResponse term = academicTermService.createAcademicTerm(request);
        return ResponseEntity.ok(ApiResponse.success("Academic term created successfully", term));
    }

    @Operation(summary = "Update academic term", description = "Update academic term information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicTermResponse>> updateAcademicTerm(
            @PathVariable UUID id, 
            @Valid @RequestBody AcademicTermRequest request) {
        AcademicTermResponse term = academicTermService.updateAcademicTerm(id, request);
        return ResponseEntity.ok(ApiResponse.success("Academic term updated successfully", term));
    }

    @Operation(summary = "Set active academic term", description = "Set an academic term as active (deactivates others)")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AcademicTermResponse>> setActiveAcademicTerm(@PathVariable UUID id) {
        AcademicTermResponse term = academicTermService.setActiveAcademicTerm(id);
        return ResponseEntity.ok(ApiResponse.success("Academic term activated successfully", term));
    }

    @Operation(summary = "Delete academic term", description = "Delete academic term by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicTerm(@PathVariable UUID id) {
        academicTermService.deleteAcademicTerm(id);
        return ResponseEntity.ok(ApiResponse.success("Academic term deleted successfully", null));
    }
}
