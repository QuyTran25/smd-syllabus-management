package vn.edu.smd.core.module.materialresource.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaterialResourceResponse {
    private UUID id;
    private UUID syllabusVersionId;
    private String resourceType;
    private String title;
    private String author;
    private String publisher;
    private Integer year;
    private String url;
    private Boolean isRequired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
