package zydd.org.libsmanagement.Catalog.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogResponse {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String imageUrl;
    private boolean isActive;
    private int available;
}
