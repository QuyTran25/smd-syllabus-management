package vn.edu.smd.core.module.materialresource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.common.dto.ApiResponse;
import vn.edu.smd.core.module.materialresource.dto.MaterialResourceRequest;
import vn.edu.smd.core.module.materialresource.dto.MaterialResourceResponse;
import vn.edu.smd.core.module.materialresource.service.MaterialResourceService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Material Resource Management", description = "Material resource management APIs")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialResourceController {

    private final MaterialResourceService materialResourceService;

    @Operation(summary = "Get all materials", description = "Get list of all material resources")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaterialResourceResponse>>> getAllMaterials() {
        List<MaterialResourceResponse> materials = materialResourceService.getAllMaterials();
        return ResponseEntity.ok(ApiResponse.success(materials));
    }

    @Operation(summary = "Get material by ID", description = "Get material resource details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResourceResponse>> getMaterialById(@PathVariable UUID id) {
        MaterialResourceResponse material = materialResourceService.getMaterialById(id);
        return ResponseEntity.ok(ApiResponse.success(material));
    }

    @Operation(summary = "Create material resource", description = "Create new material resource")
    @PostMapping
    public ResponseEntity<ApiResponse<MaterialResourceResponse>> createMaterial(@Valid @RequestBody MaterialResourceRequest request) {
        MaterialResourceResponse material = materialResourceService.createMaterial(request);
        return ResponseEntity.ok(ApiResponse.success("Material resource created successfully", material));
    }

    @Operation(summary = "Update material resource", description = "Update existing material resource")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialResourceResponse>> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialResourceRequest request) {
        MaterialResourceResponse material = materialResourceService.updateMaterial(id, request);
        return ResponseEntity.ok(ApiResponse.success("Material resource updated successfully", material));
    }

    @Operation(summary = "Delete material resource", description = "Delete material resource by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(@PathVariable UUID id) {
        materialResourceService.deleteMaterial(id);
        return ResponseEntity.ok(ApiResponse.success("Material resource deleted successfully", null));
    }

    @Operation(summary = "Get materials by syllabus", description = "Get all materials for a specific syllabus")
    @GetMapping("/syllabus/{syllabusId}/materials")
    public ResponseEntity<ApiResponse<List<MaterialResourceResponse>>> getMaterialsBySyllabusId(@PathVariable UUID syllabusId) {
        List<MaterialResourceResponse> materials = materialResourceService.getMaterialsBySyllabusId(syllabusId);
        return ResponseEntity.ok(ApiResponse.success(materials));
    }
}
