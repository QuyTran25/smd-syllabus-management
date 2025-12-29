package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.SyllabusStatus;

/**
 * JPA Converter for SyllabusStatus enum
 * Converts between Java enum and PostgreSQL ENUM type
 */
@Converter(autoApply = false)
public class SyllabusStatusConverter implements AttributeConverter<SyllabusStatus, String> {

    @Override
    public String convertToDatabaseColumn(SyllabusStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public SyllabusStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return SyllabusStatus.valueOf(dbData);
    }
}
