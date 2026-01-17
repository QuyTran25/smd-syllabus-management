package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.ComponentType;

/**
 * JPA Converter for ComponentType enum
 * Converts between Java enum (MAJOR, FOUNDATION, GENERAL, THESIS) and database values (major, foundation, general, thesis)
 */
@Converter(autoApply = true)
public class ComponentTypeConverter implements AttributeConverter<ComponentType, String> {

    @Override
    public String convertToDatabaseColumn(ComponentType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public ComponentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return ComponentType.valueOf(dbData.toUpperCase());
    }
}
