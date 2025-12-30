package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.AssignmentStatus;

/**
 * JPA Converter for AssignmentStatus enum
 * Converts between Java enum (PENDING, IN_PROGRESS, SUBMITTED, COMPLETED) 
 * and database values ('pending', 'in-progress', 'submitted', 'completed')
 */
@Converter(autoApply = true)
public class AssignmentStatusConverter implements AttributeConverter<AssignmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssignmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return AssignmentStatus.fromString(dbData);
    }
}
