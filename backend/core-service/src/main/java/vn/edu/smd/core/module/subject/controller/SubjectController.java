package vn.edu.smd.core.module.subject.controller;

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
import vn.edu.smd.core.module.subject.dto.SubjectRequest;
import vn.edu.smd.core.module.subject.dto.SubjectResponse;
import vn.edu.smd.core.module.subject.service.SubjectService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Subject Management", description = "Subject management APIs")
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @Operation(summary = "Get all subjects with pagination", description = "Get list of subjects with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubjectResponse>>> getAllSubjects(Pageable pageable) {
        Page<SubjectResponse> subjects = subjectService.getAllSubjects(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(subjects)));
    }

    @Operation(summary = "Get all subjects", description = "Get list of all subjects without pagination")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjectsNoPaging() {
        List<SubjectResponse> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Get subject by ID", description = "Get subject details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable UUID id) {
        SubjectResponse subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @Operation(summary = "Get subject by code", description = "Get subject details by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(@PathVariable String code) {
        SubjectResponse subject = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @Operation(summary = "Get subjects by department", description = "Get list of subjects by department ID")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsByDepartment(@PathVariable UUID departmentId) {
        List<SubjectResponse> subjects = subjectService.getSubjectsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Get subjects by curriculum", description = "Get list of subjects by curriculum ID")
    @GetMapping("/curriculum/{curriculumId}")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsByCurriculum(@PathVariable UUID curriculumId) {
        List<SubjectResponse> subjects = subjectService.getSubjectsByCurriculum(curriculumId);
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Get active subjects", description = "Get list of all active subjects")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getActiveSubjects() {
        List<SubjectResponse> subjects = subjectService.getActiveSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Search subjects", description = "Search subjects by keyword in code or name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> searchSubjects(@RequestParam String keyword) {
        List<SubjectResponse> subjects = subjectService.searchSubjects(keyword);
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @Operation(summary = "Create subject", description = "Create new subject")
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse subject = subjectService.createSubject(request);
        return ResponseEntity.ok(ApiResponse.success("Subject created successfully", subject));
    }

    @Operation(summary = "Update subject", description = "Update subject information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable UUID id, 
            @Valid @RequestBody SubjectRequest request) {
        SubjectResponse subject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", subject));
    }

    @Operation(summary = "Delete subject", description = "Delete subject by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable UUID id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted successfully", null));
    }

    @Operation(summary = "Get prerequisites of subject", description = "Get list of prerequisites for a subject")
    @GetMapping("/{id}/prerequisites")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse>>> getPrerequisitesOfSubject(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse> prerequisites = subjectService.getPrerequisitesOfSubject(id);
        return ResponseEntity.ok(ApiResponse.success(prerequisites));
    }

    @Operation(summary = "Add prerequisite to subject", description = "Add a prerequisite relationship to a subject")
    @PostMapping("/{id}/prerequisites")
    public ResponseEntity<ApiResponse<vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse>> addPrerequisiteToSubject(
            @PathVariable UUID id, 
            @Valid @RequestBody vn.edu.smd.core.module.prerequisite.dto.PrerequisiteRequest request) {
        vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse prerequisite = subjectService.addPrerequisiteToSubject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Prerequisite added successfully", prerequisite));
    }

    @Operation(summary = "Remove prerequisite from subject", description = "Remove a prerequisite relationship from a subject")
    @DeleteMapping("/{id}/prerequisites/{prerequisiteId}")
    public ResponseEntity<ApiResponse<Void>> removePrerequisiteFromSubject(
            @PathVariable UUID id, 
            @PathVariable UUID prerequisiteId) {
        subjectService.removePrerequisiteFromSubject(id, prerequisiteId);
        return ResponseEntity.ok(ApiResponse.success("Prerequisite removed successfully", null));
    }

    @Operation(summary = "Get syllabi of subject", description = "Get list of syllabi for a subject")
    @GetMapping("/{id}/syllabi")
    public ResponseEntity<ApiResponse<List<vn.edu.smd.core.module.syllabus.dto.SyllabusResponse>>> getSyllabiOfSubject(@PathVariable UUID id) {
        List<vn.edu.smd.core.module.syllabus.dto.SyllabusResponse> syllabi = subjectService.getSyllabiOfSubject(id);
        return ResponseEntity.ok(ApiResponse.success(syllabi));
    }

    @Operation(summary = "Check cyclic dependency", description = "Check if adding a prerequisite would create a cyclic dependency")
    @GetMapping("/{id}/check-cycle")
    public ResponseEntity<ApiResponse<Boolean>> checkCyclicDependency(
            @PathVariable UUID id,
            @RequestParam UUID prerequisiteId,
            @RequestParam(defaultValue = "PREREQUISITE") vn.edu.smd.shared.enums.SubjectRelationType type) {
        boolean hasCycle = subjectService.checkCyclicDependency(id, prerequisiteId, type);
        return ResponseEntity.ok(ApiResponse.success(hasCycle));
    }

    @Operation(summary = "Get all relationships of subject", description = "Get all relationships (prerequisites, co-requisites, replacements) of a subject")
    @GetMapping("/{id}/relationships")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.util.List<vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse>>>> getAllRelationshipsOfSubject(@PathVariable UUID id) {
        java.util.Map<String, java.util.List<vn.edu.smd.core.module.prerequisite.dto.PrerequisiteResponse>> relationships = subjectService.getAllRelationshipsOfSubject(id);
        return ResponseEntity.ok(ApiResponse.success(relationships));
    }
}
