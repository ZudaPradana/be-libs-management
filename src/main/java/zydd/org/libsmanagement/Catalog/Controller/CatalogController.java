package zydd.org.libsmanagement.Catalog.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zydd.org.libsmanagement.Catalog.DTO.CatalogParamRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogRequest;
import zydd.org.libsmanagement.Catalog.Service.CatalogService;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @RolesAllowed("ADMIN")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addCatalog(@ModelAttribute @Valid CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addCatalog(request));
    }

    @RolesAllowed("ADMIN")
    @PutMapping(path = "/{catalogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCatalog(
            @PathVariable Long catalogId,
            @ModelAttribute @Valid CatalogRequest request
    ) {
        return ResponseEntity.ok(catalogService.updateCatalog(catalogId, request));
    }

    @RolesAllowed("ADMIN")
    @DeleteMapping("/{catalogId}")
    public ResponseEntity<?> deleteCatalog(@PathVariable Long catalogId) {
        return ResponseEntity.ok(catalogService.deleteCatalog(catalogId));
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/{catalogId}")
    public ResponseEntity<?> getCatalog(@PathVariable Long catalogId) {
        return ResponseEntity.ok(catalogService.getCatalog(catalogId));
    }

    @RolesAllowed("ADMIN")
    @GetMapping
    public ResponseEntity<?> getAllCatalogs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CatalogParamRequest request = CatalogParamRequest.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .build();

        Pageable fixedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort()
        );

        return ResponseEntity.ok(catalogService.getAllCatalogs(request, fixedPageable));
    }
}
