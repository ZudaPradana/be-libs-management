package zydd.org.libsmanagement.Catalog.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogDetailResponse {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private int quantity;
    private int available;
    private String imageUrl;
    private boolean isActive;
}
