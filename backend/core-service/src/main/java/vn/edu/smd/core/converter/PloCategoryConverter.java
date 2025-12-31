package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.PloCategory;

/**
 * JPA converter for mapping between Java enum {@link PloCategory}
 * and database enum stored as capitalized string (e.g. "Knowledge").
 * Bộ chuyển đổi dữ liệu từ Postgres (Knowledge) sang Java (KNOWLEDGE)
 */
@Converter(autoApply = true)
public class PloCategoryConverter implements AttributeConverter<PloCategory, String> {

    @Override
    public String convertToDatabaseColumn(PloCategory category) {
        if (category == null) {
            return null;
        }
        // Chuyển đổi từ định dạng Java Enum sang DB: KNOWLEDGE -> Knowledge
        String name = category.name();
        if (name.length() <= 1) return name.toUpperCase();
        
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @Override
    public PloCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            // Chuyển đổi từ định dạng DB sang Java Enum: Knowledge -> KNOWLEDGE
            return PloCategory.valueOf(dbData.trim().toUpperCase());
        } catch (Exception e) {
            // Trả về giá trị mặc định nếu dữ liệu trong DB không khớp để tránh crash hệ thống
            return PloCategory.KNOWLEDGE;
        }
    }
}