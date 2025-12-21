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
        // Convert PENDING -> 'pending', IN_PROGRESS -> 'in-progress'
        return attribute.name().toLowerCase().replace('_', '-');
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        // Convert 'pending' -> PENDING, 'in-progress' -> IN_PROGRESS
        return AssignmentStatus.valueOf(dbData.toUpperCase().replace('-', '_'));
    }
}
