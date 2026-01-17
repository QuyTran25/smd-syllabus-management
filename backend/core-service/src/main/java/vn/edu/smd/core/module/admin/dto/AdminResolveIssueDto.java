package vn.edu.smd.core.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AdminResolveIssueDto {
    @NotNull(message = "Report ID không được để trống")
    private UUID reportId;

    @NotNull(message = "Hành động không được để trống (APPROVE/REJECT)")
    private String action; // Giá trị: "APPROVE" hoặc "REJECT"

    private String adminComment; // Lý do từ chối hoặc ghi chú thêm
}