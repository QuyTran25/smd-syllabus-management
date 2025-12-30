package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.PloCategory;

/**
 * Converter to map between Java enum (KNOWLEDGE) and database enum (Knowledge)
 * Bộ chuyển đổi dữ liệu từ Postgres (Knowledge) sang Java (KNOWLEDGE)
 */
@Converter(autoApply = true)
public class PloCategoryConverter implements AttributeConverter<PloCategory, String> {

    @Override
    public String convertToDatabaseColumn(PloCategory category) {
        if (category == null) {
            return null;
        }
        // Convert KNOWLEDGE -> Knowledge
        String name = category.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @Override
    public PloCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            // Convert Knowledge -> KNOWLEDGE
            return PloCategory.valueOf(dbData.toUpperCase());
        } catch (Exception e) {
            return PloCategory.KNOWLEDGE;
        }
    }
}
