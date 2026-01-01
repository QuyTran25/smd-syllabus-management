package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import vn.edu.smd.shared.enums.AssignmentStatus;

/**
 * JPA Converter for AssignmentStatus enum
 * Converts between Java enum (PENDING, IN_PROGRESS, SUBMITTED, COMPLETED) 
 * and database values ('pending', 'in-progress', 'submitted', 'completed')
 * This converter overrides @Enumerated to handle PostgreSQL enum type correctly
 */
@Slf4j
@Converter(autoApply = true)
public class AssignmentStatusConverter implements AttributeConverter<AssignmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssignmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        String dbValue = attribute.getDbValue();
        log.debug("Converting enum {} to DB value: {}", attribute, dbValue);
        return dbValue;
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            AssignmentStatus status = AssignmentStatus.fromString(dbData);
            log.debug("Converting DB value '{}' to enum: {}", dbData, status);
            return status;
        } catch (Exception e) {
            log.error("Failed to convert DB value '{}' to AssignmentStatus enum", dbData, e);
            throw e;
        }
    }
}
