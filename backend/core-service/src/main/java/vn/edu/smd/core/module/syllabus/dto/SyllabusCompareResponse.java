package vn.edu.smd.core.module.syllabus.dto;

import lombok.Data;
import java.util.List;

@Data
public class SyllabusCompareResponse {
    private SyllabusResponse syllabusA;
    private SyllabusResponse syllabusB;
    private List<FieldDifference> differences;

    @Data
    public static class FieldDifference {
        private String fieldName;
        private Object valueA;
        private Object valueB;
        private boolean isDifferent;
    }
}
