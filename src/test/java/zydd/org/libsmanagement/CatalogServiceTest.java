package zydd.org.libsmanagement;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import zydd.org.libsmanagement.Catalog.DTO.CatalogDetailResponse;
import zydd.org.libsmanagement.Catalog.DTO.CatalogParamRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogRequest;
import zydd.org.libsmanagement.Catalog.DTO.CatalogResponse;
import zydd.org.libsmanagement.Catalog.Model.Catalog;
import zydd.org.libsmanagement.Catalog.Repository.CatalogRepository;
import zydd.org.libsmanagement.Catalog.Service.CatalogServiceImp;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;

import java.io.File;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogServiceImp catalogService;

    private CatalogRequest validRequest;
    private MultipartFile validFile;

    @BeforeEach
    void setUp() {
        // Setup valid request and file mock
        validRequest = new CatalogRequest();
        validRequest.setTitle("Test Catalog");
        validRequest.setGenre("Fiction");
        validRequest.setAuthor("Author Name");
        validRequest.setDescription("A test catalog description.");
        validRequest.setQuantity(10);
        validFile = mock(MultipartFile.class);
        validRequest.setImageUrl(validFile);
    }

    @Test
    void testAddCatalog_whenImageIsValid_thenCatalogIsAddedSuccessfully() throws Exception {
        // Mock behavior
        when(validFile.getOriginalFilename()).thenReturn("testImage.jpg");
        when(validFile.getSize()).thenReturn(100L); // Simulating size less than MAX_FILE_SIZE
        doNothing().when(validFile).transferTo(any(File.class)); // Simulating successful file transfer

        // Mock repository behavior
        when(catalogRepository.save(any(Catalog.class))).thenReturn(new Catalog());

        // Call the method
        ApiResponse<?> response = catalogService.addCatalog(validRequest);

        // Assertions
        assertEquals(201, response.getCode());
        assertEquals("Catalog added successfully", response.getMessage());
    }

    @Test
    void testAddCatalog_whenImageFileExceedsSizeLimit_thenReturnErrorResponse() throws Exception {
        CatalogRequest request = new CatalogRequest();
        MockMultipartFile oversizeFile = new MockMultipartFile(
                "image",
                "testImage.jpg",
                "image/jpeg",
                new byte[3000000] // File 3MB
        );
        request.setImageUrl(oversizeFile);

        ApiResponse<?> response = catalogService.addCatalog(request);

        assertEquals(400, response.getCode());
        assertEquals("File size exceeds the 2MB limit.", response.getMessage());

        verifyNoInteractions(catalogRepository);
    }

    @Test
    void testDeleteCatalog_whenCatalogExists_thenCatalogIsDeletedSuccessfully() {
        Long catalogId = 1L;
        Catalog catalog = new Catalog();
        catalog.setId(catalogId);
        catalog.setActive(true);

        // Mock repository behavior
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(catalog));

        ApiResponse<?> response = catalogService.deleteCatalog(catalogId);

        assertEquals(200, response.getCode());
        assertEquals("Catalog deleted successfully", response.getMessage());
        assertFalse(catalog.isActive());
    }

    @Test
    void testDeleteCatalog_whenCatalogNotFound_thenThrowException() {
        // Arrange
        Long catalogId = 1L;

        // Mock repository behavior
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            catalogService.deleteCatalog(catalogId);
        });

        assertEquals("Catalog not found", exception.getMessage());

        // Verify
        verify(catalogRepository, times(1)).findById(catalogId);
        verifyNoMoreInteractions(catalogRepository);
    }

    @Test
    void getCatalog_WhenCatalogExists_ShouldReturnSuccessResponse() {
        // Arrange
        Long catalogId = 1L;
        Catalog mockCatalog = Catalog.builder()
                .id(catalogId)
                .title("Test Book")
                .author("Test Author")
                .genre("Fiction")
                .description("Test Description")
                .quantity(5)
                .available(5)
                .imageUrl("test.jpg")
                .isActive(true)
                .build();

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(mockCatalog));

        // Act
        ApiResponse<CatalogDetailResponse> response = catalogService.getCatalog(catalogId);

        // Assert
        assertEquals("Catalog fetched.", response.getMessage());
        assertEquals(catalogId, response.getData().getId());
        assertEquals("/images/test.jpg", response.getData().getImageUrl());
        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void getCatalog_WhenCatalogNotFound_ShouldThrowException() {
        // Arrange
        Long catalogId = 99L;
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            catalogService.getCatalog(catalogId);
        });
        verify(catalogRepository, times(1)).findById(catalogId);
    }

    @Test
    void getCatalog_WhenImageUrlNull_ShouldUseDefaultImage() {
        // Arrange
        Long catalogId = 1L;
        Catalog mockCatalog = Catalog.builder()
                .id(catalogId)
                .imageUrl(null)
                .build();

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(mockCatalog));

        // Act
        ApiResponse<CatalogDetailResponse> response = catalogService.getCatalog(catalogId);

        // Assert
        assertEquals("https://m.media-amazon.com/images/I/91A6EgLH+2L._AC_UF1000,1000_QL80_.jpg",
                response.getData().getImageUrl());
    }

    @Test
    void getAllCatalogs_WithFilters_ShouldReturnFilteredResults() {
        // Arrange
        CatalogParamRequest params = CatalogParamRequest.builder()
                .title("test")
                .author("author")
                .genre("fiction")
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<Catalog> mockCatalogs = List.of(
                Catalog.builder()
                        .id(1L)
                        .title("Test Book 1")
                        .author("Author 1")
                        .genre("fiction")
                        .imageUrl("book1.jpg")
                        .isActive(true)
                        .build(),
                Catalog.builder()
                        .id(2L)
                        .title("Test Book 2")
                        .author("Author 2")
                        .genre("fiction")
                        .imageUrl(null)
                        .isActive(true)
                        .build()
        );

        Page<Catalog> mockPage = new PageImpl<>(mockCatalogs, pageable, mockCatalogs.size());

        when(catalogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        ApiResponse<Page<CatalogResponse>> response = catalogService.getAllCatalogs(params, pageable);

        // Assert
        assertEquals("Catalogs fetched successfully", response.getMessage());
        assertEquals(2, response.getData().getContent().size());

        // Verify first catalog
        CatalogResponse first = response.getData().getContent().get(0);
        assertEquals(1L, first.getId());
        assertEquals("/images/book1.jpg", first.getImageUrl());

        // Verify second catalog (default image)
        CatalogResponse second = response.getData().getContent().get(1);
        assertEquals("https://m.media-amazon.com/images/I/91A6EgLH+2L._AC_UF1000,1000_QL80_.jpg",
                second.getImageUrl());

        verify(catalogRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

}
