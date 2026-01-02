package vn.edu.smd.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Loại thành phần trong cấu trúc chương trình đào tạo (curriculum structure)
 * 
 * Ánh xạ trực tiếp với kiểu enum trong database: component_type
 * 
 * Giá trị trong DB: uppercase (MAJOR, FOUNDATION, GENERAL, THESIS)
 */
public enum ComponentType {

    /**
     * Môn chuyên ngành / chuyên sâu
     */
    MAJOR,

    /**
     * Môn cơ sở ngành / cơ sở khối ngành
     */
    FOUNDATION,

    /**
     * Môn đại cương / giáo dục đại cương
     */
    GENERAL,

    /**
     * Luận văn tốt nghiệp / khóa luận tốt nghiệp / đồ án tốt nghiệp
     */
    THESIS;

    /**
     * Tên hiển thị bằng tiếng Việt (dùng trong UI/API response)
     */
    @JsonValue
    public String getDisplayName() {
        return switch (this) {
            case MAJOR     -> "Chuyên ngành";
            case FOUNDATION -> "Cơ sở ngành";
            case GENERAL   -> "Đại cương";
            case THESIS    -> "Luận văn / Khóa luận";
        };
    }

    /**
     * Tên hiển thị ngắn gọn bằng tiếng Việt (dùng trong bảng, form, dropdown)
     */
    public String getShortName() {
        return switch (this) {
            case MAJOR     -> "CN";
            case FOUNDATION -> "CSN";
            case GENERAL   -> "ĐC";
            case THESIS    -> "LV/KL";
        };
    }

    /**
     * Kiểm tra xem đây có phải là môn chuyên ngành không
     */
    public boolean isMajor() {
        return this == MAJOR;
    }

    /**
     * Kiểm tra xem đây có phải là môn đại cương không
     */
    public boolean isGeneral() {
        return this == GENERAL;
    }

    /**
     * Kiểm tra xem đây có phải là môn thuộc khối luận văn/khóa luận không
     */
    public boolean isThesis() {
        return this == THESIS;
    }
}