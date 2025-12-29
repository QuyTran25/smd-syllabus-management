package vn.edu.smd.core.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.ErrorReportSection;

/**
 * JPA Converter for ErrorReportSection enum to handle case-insensitive mapping
 * between database lowercase values and Java uppercase enum values
 */
@Converter(autoApply = true)
public class ErrorReportSectionConverter implements AttributeConverter<ErrorReportSection, String> {

    @Override
    public String convertToDatabaseColumn(ErrorReportSection attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public ErrorReportSection convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return ErrorReportSection.valueOf(dbData.toUpperCase());
    }
}
