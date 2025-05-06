package zydd.org.libsmanagement.Catalog.DTO;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CatalogRequest {
    private String title;
    private String author;
    private String genre;
    private String description;
    private int quantity;
    @Nullable
    private MultipartFile imageUrl;
    private boolean isActive;
}
