package zydd.org.libsmanagement.Catalog.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import zydd.org.libsmanagement.Catalog.DTO.CatalogDetailResponse;
import zydd.org.libsmanagement.Catalog.DTO.CatalogParamRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogResponse;
import zydd.org.libsmanagement.Catalog.Model.Catalog;
import zydd.org.libsmanagement.Catalog.Repository.CatalogRepository;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CatalogServiceImp implements CatalogService{
    private CatalogRepository catalogRepository;
    private Catalog catalog;

    public CatalogServiceImp(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    @Value("${upload.directory}")
    private String uploadDir;

    @Override
    public ApiResponse<?> addCatalog(CatalogRequest request) {
        String uploadDir = System.getProperty("user.dir") + File.separator + "images";
        File directory = new File(uploadDir);

        // Pastikan direktori images ada
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Ambil nama file asli dari gambar
        String originalFileName = request.getImageUrl().getOriginalFilename();

        if (originalFileName == null || originalFileName.isEmpty()) {
            return ApiResponse.error(400, "Image file is missing or invalid.", null);
        }

        if (request.getImageUrl().getSize() > MAX_FILE_SIZE) {  // 2MB
            return ApiResponse.error(400, "File size exceeds the 2MB limit.", null);
        }

        // Validasi format file gambar (hanya JPG, PNG, JPEG, GIF yang diperbolehkan)
        String extension = getFileExtension(originalFileName);
        if (!isAllowedImageFormat(extension)) {
            return ApiResponse.error(400, "Invalid file format. Only JPG, JPEG, PNG, and GIF are allowed.", null);
        }

        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyHHmmss"));

        String fileName = baseName + "_" + timestamp + extension;
        File imageFile = new File(uploadDir + File.separator + fileName);

        try {
            request.getImageUrl().transferTo(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error(500, "Failed to save the image.", null);
        }

        Catalog catalog = Catalog.builder()
                .title(request.getTitle())
                .genre(request.getGenre())
                .author(request.getAuthor())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .available(request.getQuantity())
                .isActive(true)
                .imageUrl(fileName)  // Menyimpan nama file gambar yang telah di-upload
                .build();

        catalogRepository.save(catalog);

        return ApiResponse.success(201, "Catalog added successfully", null);
    }

    @Override
    public ApiResponse<?> deleteCatalog(Long catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId).orElseThrow(() -> new EntityNotFoundException("Catalog not found"));
        catalog.setActive(false);
        catalogRepository.save(catalog);
        return ApiResponse.success(200, "Catalog deleted successfully", null);
    }

    @Override
    public ApiResponse<?> updateCatalog(Long catalogId, CatalogRequest request) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new UsernameNotFoundException("Catalog not found"));

        catalog.setTitle(request.getTitle());
        catalog.setGenre(request.getGenre());
        catalog.setAuthor(request.getAuthor());
        catalog.setDescription(request.getDescription());
        catalog.setQuantity(request.getQuantity());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Validasi file gambar
            MultipartFile image = request.getImageUrl();

            // Validasi ukuran file
            if (image.getSize() > MAX_FILE_SIZE) {
                return ApiResponse.error(400, "File size exceeds the 2MB limit.", null);
            }

            // Validasi ekstensi file
            String fileExtension = getFileExtension(image.getOriginalFilename());
            if (!isAllowedImageFormat(fileExtension)) {
                return ApiResponse.error(400, "Invalid file format. Only JPG, JPEG, PNG, and GIF are allowed.", null);
            }

            String oldFileName = catalog.getImageUrl();
            if (oldFileName != null) {
                File oldFile = new File(uploadDir + File.separator + oldFileName);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String newFileName = request.getImageUrl().getOriginalFilename();
            File uploadDirectory = new File(System.getProperty("user.dir") + File.separator + "images");

            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            File newFile = new File(uploadDirectory + File.separator + newFileName);

            try {
                request.getImageUrl().transferTo(newFile);
                catalog.setImageUrl(newFileName);
            } catch (IOException e) {
                return ApiResponse.error(500, "Failed to save the new image.", null);
            }
        }

        catalogRepository.save(catalog);

        return ApiResponse.success(200, "Catalog updated successfully", null);
    }

    @Override
    public ApiResponse<CatalogDetailResponse> getCatalog(Long catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId).orElseThrow(() -> new UsernameNotFoundException("Catalog not found"));

        String imageUrl = catalog.getImageUrl() != null ? "/images/"+catalog.getImageUrl() : "https://m.media-amazon.com/images/I/91A6EgLH+2L._AC_UF1000,1000_QL80_.jpg";

        CatalogDetailResponse response = CatalogDetailResponse.builder()
                .id(catalog.getId())
                .title(catalog.getTitle())
                .author(catalog.getAuthor())
                .genre(catalog.getGenre())
                .description(catalog.getDescription())
                .quantity(catalog.getQuantity())
                .available(catalog.getAvailable())
                .imageUrl(imageUrl)
                .isActive(catalog.isActive())
                .build();
        return ApiResponse.success("Catalog fetched.", response);
    }

    @Override
    public ApiResponse<Page<CatalogResponse>> getAllCatalogs(CatalogParamRequest request, Pageable pageable) {
        Specification<Catalog> spec = Specification.where((root, query, cb) -> cb.isTrue(root.get("isActive")));

        if (StringUtils.hasText(request.getTitle())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + request.getTitle().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getAuthor())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("author")), "%" + request.getAuthor().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(request.getGenre())) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("genre")), request.getGenre().toLowerCase()));
        }

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        Page<Catalog> result = catalogRepository.findAll(spec, pageable);

        Page<CatalogResponse> response = result.map(catalog -> {
            String imageUrl = catalog.getImageUrl() != null
                    ? "/images/" + catalog.getImageUrl()
                    : "https://m.media-amazon.com/images/I/91A6EgLH+2L._AC_UF1000,1000_QL80_.jpg";

            return CatalogResponse.builder()
                    .id(catalog.getId())
                    .title(catalog.getTitle())
                    .author(catalog.getAuthor())
                    .genre(catalog.getGenre())
                    .available(catalog.getAvailable())
                    .imageUrl(imageUrl)
                    .build();
        });

        return ApiResponse.success("Catalogs fetched successfully", response);
    }

    // Helper method untuk mendapatkan ekstensi file
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    // Helper method untuk memeriksa apakah ekstensi file sesuai format gambar
    private boolean isAllowedImageFormat(String extension) {
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
