package vn.edu.smd.core.module.curriculum.controller;

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
import vn.edu.smd.core.module.curriculum.dto.CurriculumRequest;
import vn.edu.smd.core.module.curriculum.dto.CurriculumResponse;
import vn.edu.smd.core.module.curriculum.service.CurriculumService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Curriculum Management", description = "Curriculum management APIs")
@RestController
@RequestMapping("/api/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @Operation(summary = "Get all curriculums with pagination", description = "Get list of curriculums with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CurriculumResponse>>> getAllCurriculums(Pageable pageable) {
        Page<CurriculumResponse> curriculums = curriculumService.getAllCurriculums(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(curriculums)));
    }

    @Operation(summary = "Get all curriculums", description = "Get list of all curriculums without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CurriculumResponse>>> getAllCurriculumsNoPaging() {
        List<CurriculumResponse> curriculums = curriculumService.getAllCurriculums();
        return ResponseEntity.ok(ApiResponse.success(curriculums));
    }

    @Operation(summary = "Get curriculum by ID", description = "Get curriculum details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> getCurriculumById(@PathVariable UUID id) {
        CurriculumResponse curriculum = curriculumService.getCurriculumById(id);
        return ResponseEntity.ok(ApiResponse.success(curriculum));
    }

    @Operation(summary = "Get curriculum by code", description = "Get curriculum details by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> getCurriculumByCode(@PathVariable String code) {
        CurriculumResponse curriculum = curriculumService.getCurriculumByCode(code);
        return ResponseEntity.ok(ApiResponse.success(curriculum));
    }

    @Operation(summary = "Get curriculums by faculty", description = "Get list of curriculums by faculty ID")
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<ApiResponse<List<CurriculumResponse>>> getCurriculumsByFaculty(@PathVariable UUID facultyId) {
        List<CurriculumResponse> curriculums = curriculumService.getCurriculumsByFaculty(facultyId);
        return ResponseEntity.ok(ApiResponse.success(curriculums));
    }

    @Operation(summary = "Create curriculum", description = "Create new curriculum")
    @PostMapping
    public ResponseEntity<ApiResponse<CurriculumResponse>> createCurriculum(@Valid @RequestBody CurriculumRequest request) {
        CurriculumResponse curriculum = curriculumService.createCurriculum(request);
        return ResponseEntity.ok(ApiResponse.success("Curriculum created successfully", curriculum));
    }

    @Operation(summary = "Update curriculum", description = "Update curriculum information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CurriculumResponse>> updateCurriculum(
            @PathVariable UUID id, 
            @Valid @RequestBody CurriculumRequest request) {
        CurriculumResponse curriculum = curriculumService.updateCurriculum(id, request);
        return ResponseEntity.ok(ApiResponse.success("Curriculum updated successfully", curriculum));
    }

    @Operation(summary = "Delete curriculum", description = "Delete curriculum by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCurriculum(@PathVariable UUID id) {
        curriculumService.deleteCurriculum(id);
        return ResponseEntity.ok(ApiResponse.success("Curriculum deleted successfully", null));
    }
}
