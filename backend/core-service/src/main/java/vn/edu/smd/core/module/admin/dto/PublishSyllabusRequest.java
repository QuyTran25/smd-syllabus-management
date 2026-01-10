package vn.edu.smd.core.module.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishSyllabusRequest {
    // Ghi chú khi xuất hành (Optional)
    private String comment; 
    
    // Lý do khi gỡ bỏ (Required for unpublish)
    private String reason;
    
    // Ngày hiệu lực (Optional)
    private String effectiveDate;
}