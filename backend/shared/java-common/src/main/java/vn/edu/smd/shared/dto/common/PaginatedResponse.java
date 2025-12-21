package vn.edu.smd.shared.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints
 *
 * @param <T> Type of items in the list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    
    /**
     * List of items for current page
     */
    private List<T> data;
    
    /**
     * Current page number (1-based)
     */
    private int page;
    
    /**
     * Number of items per page
     */
    private int pageSize;
    
    /**
     * Total number of items across all pages
     */
    private long total;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Indicates if there's a next page
     */
    private boolean hasNext;
    
    /**
     * Indicates if there's a previous page
     */
    private boolean hasPrevious;
    
    /**
     * Create paginated response with automatic calculations
     */
    public static <T> PaginatedResponse<T> of(
            List<T> data, 
            int page, 
            int pageSize, 
            long total
    ) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        return PaginatedResponse.<T>builder()
                .data(data)
                .page(page)
                .pageSize(pageSize)
                .total(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }
    
    /**
     * Create empty paginated response
     */
    public static <T> PaginatedResponse<T> empty(int page, int pageSize) {
        return PaginatedResponse.<T>builder()
                .data(List.of())
                .page(page)
                .pageSize(pageSize)
                .total(0L)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
