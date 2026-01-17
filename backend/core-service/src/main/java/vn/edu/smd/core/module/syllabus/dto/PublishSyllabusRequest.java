package vn.edu.smd.core.module.syllabus.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PublishSyllabusRequest {
    private LocalDate effectiveDate; // Ngày hiệu lực
    private String comment;          // Ghi chú (nếu có)
}