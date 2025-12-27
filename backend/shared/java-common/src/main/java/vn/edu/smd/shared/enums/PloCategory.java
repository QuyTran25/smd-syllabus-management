package vn.edu.smd.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

/**
 * PLO (Program Learning Outcome) category
 * Module: shared
 */
public enum PloCategory {
    KNOWLEDGE,
    SKILLS,
    COMPETENCE,
    ATTITUDE;

    /**
     * ⭐ QUAN TRỌNG: Hàm này phải là 'decode' để khớp với Converter
     */
    @JsonCreator
    public static PloCategory decode(String code) {
        if (code == null || code.isEmpty()) return KNOWLEDGE;
        return Stream.of(PloCategory.values())
                .filter(targetEnum -> targetEnum.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(KNOWLEDGE);
    }

    @JsonValue
    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return switch (this) {
            case KNOWLEDGE -> "Kiến thức";
            case SKILLS -> "Kỹ năng";
            case COMPETENCE -> "Năng lực";
            case ATTITUDE -> "Thái độ";
        };
    }
}