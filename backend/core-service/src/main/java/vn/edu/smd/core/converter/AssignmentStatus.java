package vn.edu.smd.core.converter;

/**
 * Enum đại diện cho các trạng thái của Assignment.
 * 
 * Các giá trị được lưu vào DB dưới dạng lowercase (ví dụ: 'pending', 'completed').
 */
public enum AssignmentStatus {
    DRAFT,
    PENDING,
    SUBMITTED,
    IN_PROGRESS,
    COMPLETED,
    GRADED,
    RETURNED,
    REJECTED;

    // Giá trị lưu vào database (lowercase tên enum)
    private final String dbValue;

    AssignmentStatus() {
        this.dbValue = this.name().toLowerCase();
    }

    /**
     * Trả về giá trị String để lưu vào database.
     * Được gọi từ AssignmentStatusConverter.
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * Chuyển đổi String từ database thành enum tương ứng.
     * Được gọi từ AssignmentStatusConverter.
     * 
     * @param value Giá trị String từ DB (ví dụ: "pending", "completed")
     * @return AssignmentStatus tương ứng hoặc null nếu value rỗng/null
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static AssignmentStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        try {
            return AssignmentStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Không tìm thấy AssignmentStatus cho giá trị: '" + value + "'", e);
        }
    }
}