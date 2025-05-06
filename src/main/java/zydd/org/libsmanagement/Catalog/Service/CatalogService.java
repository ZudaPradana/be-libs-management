package zydd.org.libsmanagement.Catalog.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zydd.org.libsmanagement.Catalog.DTO.CatalogParamRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogDetailResponse;
import zydd.org.libsmanagement.Catalog.DTO.CatalogResponse;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;

public interface CatalogService {
    ApiResponse<?> addCatalog(CatalogRequest request);
    ApiResponse<?> deleteCatalog(Long catalogId);
    ApiResponse<?> updateCatalog(Long catalogId, CatalogRequest request);
    ApiResponse<CatalogDetailResponse> getCatalog(Long catalogId);
    ApiResponse<Page<CatalogResponse>> getAllCatalogs(CatalogParamRequest request, Pageable pageable);
}
