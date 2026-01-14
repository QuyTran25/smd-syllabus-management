package vn.edu.smd.core.module.plo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.plo.dto.PloRequest;
import vn.edu.smd.core.module.plo.dto.PloResponse;
import vn.edu.smd.core.module.plo.service.PloService;

import java.util.List;
import java.util.UUID;

@Tag(name = "PLO Management", description = "Program Learning Outcome management APIs")
@RestController
@RequestMapping("/api/plos")
@RequiredArgsConstructor
public class PloController {

    private final PloService ploService;

    @Operation(summary = "Get all PLOs", description = "Get list of all PLOs")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PloResponse>>> getAllPlos() {
        List<PloResponse> plos = ploService.getAllPlos();
        return ResponseEntity.ok(ApiResponse.success(plos));
    }

    @Operation(summary = "Get PLOs by subject", description = "Get list of PLOs for a subject")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<PloResponse>>> getPlosBySubject(@PathVariable UUID subjectId) {
        List<PloResponse> plos = ploService.getPlosBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(plos));
    }

    @Operation(summary = "Get PLO by ID", description = "Get PLO details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PloResponse>> getPloById(@PathVariable UUID id) {
        PloResponse plo = ploService.getPloById(id);
        return ResponseEntity.ok(ApiResponse.success(plo));
    }

    @Operation(summary = "Create PLO", description = "Create new program learning outcome")
    @PostMapping
    public ResponseEntity<ApiResponse<PloResponse>> createPlo(@Valid @RequestBody PloRequest request) {
        PloResponse plo = ploService.createPlo(request);
        return ResponseEntity.ok(ApiResponse.success("PLO created successfully", plo));
    }

    @Operation(summary = "Update PLO", description = "Update PLO information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PloResponse>> updatePlo(
            @PathVariable UUID id, 
            @Valid @RequestBody PloRequest request) {
        PloResponse plo = ploService.updatePlo(id, request);
        return ResponseEntity.ok(ApiResponse.success("PLO updated successfully", plo));
    }

    @Operation(summary = "Delete PLO", description = "Delete PLO by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlo(@PathVariable UUID id) {
        ploService.deletePlo(id);
        return ResponseEntity.ok(ApiResponse.success("PLO deleted successfully", null));
    }
}
