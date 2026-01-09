package vn.edu.smd.core.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.SubjectRelationType;

/**
 * JPA Converter for SubjectRelationType to handle PostgreSQL ENUM type
 */
@Converter(autoApply = true)
public class SubjectRelationTypeConverter implements AttributeConverter<SubjectRelationType, String> {

    @Override
    public String convertToDatabaseColumn(SubjectRelationType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public SubjectRelationType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return SubjectRelationType.valueOf(dbData);
    }
}
