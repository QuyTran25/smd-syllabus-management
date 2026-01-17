package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.CourseType;

/**
 * JPA Converter for CourseType enum
 * Converts between Java enum (REQUIRED, ELECTIVE, FREE) and database values (required, elective, optional)
 */
@Converter(autoApply = true)
public class CourseTypeConverter implements AttributeConverter<CourseType, String> {

    @Override
    public String convertToDatabaseColumn(CourseType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public CourseType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return CourseType.valueOf(dbData.toUpperCase());
    }
}
