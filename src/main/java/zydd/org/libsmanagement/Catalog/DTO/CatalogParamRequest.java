package zydd.org.libsmanagement.Catalog.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CatalogParamRequest {
    private String title;
    private String author;
    private String genre;
}
