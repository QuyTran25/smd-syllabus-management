package vn.edu.smd.core.module.classmodule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    private UUID semesterId;

    private UUID lecturerId;

    private Integer maxStudents;

    private String schedule;

    private String room;

    private String status;
}
