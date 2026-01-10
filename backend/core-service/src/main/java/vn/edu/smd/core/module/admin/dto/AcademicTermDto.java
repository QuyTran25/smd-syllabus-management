package vn.edu.smd.core.module.admin.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AcademicTermDto {
    private UUID id;
    private String code;
    private String name;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}