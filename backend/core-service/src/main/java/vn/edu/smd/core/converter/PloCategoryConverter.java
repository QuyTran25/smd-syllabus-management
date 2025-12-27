package vn.edu.smd.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.smd.shared.enums.PloCategory;

/**
 * Bộ chuyển đổi dữ liệu từ Postgres (Knowledge) sang Java (KNOWLEDGE)
 */
@Converter(autoApply = true)
public class PloCategoryConverter implements AttributeConverter<PloCategory, String> {

    @Override
    public String convertToDatabaseColumn(PloCategory attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public PloCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            // ⭐ Gọi đúng hàm decode đã định nghĩa ở module shared
            return PloCategory.decode(dbData.toUpperCase());
        } catch (Exception e) {
            return PloCategory.KNOWLEDGE;
        }
    }
}