package vn.edu.smd.core.module.performanceindicator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.performanceindicator.dto.PerformanceIndicatorRequest;
import vn.edu.smd.core.module.performanceindicator.dto.PerformanceIndicatorResponse;
import vn.edu.smd.core.module.performanceindicator.service.PerformanceIndicatorService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Performance Indicator Management", description = "Performance indicator management APIs")
@RestController
@RequestMapping("/api/performance-indicators")
@RequiredArgsConstructor
public class PerformanceIndicatorController {

    private final PerformanceIndicatorService piService;

    @Operation(summary = "Get PIs by PLO", description = "Get list of performance indicators for a PLO")
    @GetMapping("/plo/{ploId}")
    public ResponseEntity<ApiResponse<List<PerformanceIndicatorResponse>>> getPisByPlo(@PathVariable UUID ploId) {
        List<PerformanceIndicatorResponse> pis = piService.getPisByPlo(ploId);
        return ResponseEntity.ok(ApiResponse.success(pis));
    }

    @Operation(summary = "Get PI by ID", description = "Get performance indicator details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PerformanceIndicatorResponse>> getPiById(@PathVariable UUID id) {
        PerformanceIndicatorResponse pi = piService.getPiById(id);
        return ResponseEntity.ok(ApiResponse.success(pi));
    }

    @Operation(summary = "Create PI", description = "Create new performance indicator")
    @PostMapping
    public ResponseEntity<ApiResponse<PerformanceIndicatorResponse>> createPi(@Valid @RequestBody PerformanceIndicatorRequest request) {
        PerformanceIndicatorResponse pi = piService.createPi(request);
        return ResponseEntity.ok(ApiResponse.success("Performance indicator created successfully", pi));
    }

    @Operation(summary = "Update PI", description = "Update performance indicator information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PerformanceIndicatorResponse>> updatePi(
            @PathVariable UUID id, 
            @Valid @RequestBody PerformanceIndicatorRequest request) {
        PerformanceIndicatorResponse pi = piService.updatePi(id, request);
        return ResponseEntity.ok(ApiResponse.success("Performance indicator updated successfully", pi));
    }

    @Operation(summary = "Delete PI", description = "Delete performance indicator by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePi(@PathVariable UUID id) {
        piService.deletePi(id);
        return ResponseEntity.ok(ApiResponse.success("Performance indicator deleted successfully", null));
    }
}
