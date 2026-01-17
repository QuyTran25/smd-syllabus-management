package vn.edu.smd.core.module.student.dto;

import lombok.Data;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportIssueDto {
    private UUID syllabusId;
    private String section;
    private String description;
}