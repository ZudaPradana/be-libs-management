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
import zydd.org.libsmanagement.Catalog.Model.Catalog;
import zydd.org.libsmanagement.Catalog.Repository.CatalogRepository;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Member.Model.Member;
import zydd.org.libsmanagement.Member.Model.Role;
import zydd.org.libsmanagement.Member.Repository.MemberRepository;
import zydd.org.libsmanagement.Rent.DTO.RentalDetailRequest;
import zydd.org.libsmanagement.Rent.DTO.RentalRequest;
import zydd.org.libsmanagement.Rent.Repository.RentalRepository;
import zydd.org.libsmanagement.Rent.Service.RentalServiceImp;

import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalServiceImp rentalService;

    private Member member;
    private Catalog catalog;
    private RentalRequest rentalRequest;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.MEMBER)
                .build();

        catalog = Catalog.builder()
                .id(10L)
                .title("Book A")
                .quantity(5)
                .available(5)
                .build();

        rentalRequest = new RentalRequest();
        rentalRequest.setNote("Some note");

        RentalDetailRequest detailRequest = new RentalDetailRequest();
        detailRequest.setCatalogId(10L);
        detailRequest.setQuantity(2);
        rentalRequest.setCatalogList(List.of(detailRequest));
    }

    @Test
    void testRequestRental_whenRoleIsNotMember_thenReturnForbidden() {
        member.setRole(Role.ADMIN);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        ApiResponse<?> response = rentalService.requestRental(1L, rentalRequest);

        assertEquals(403, response.getCode());
        assertEquals("Only members with role 'MEMBER' can request rentals", response.getMessage());
    }

    @Test
    void testRequestRental_whenCatalogNotFound_thenReturnError() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(catalogRepository.findAllById(any())).thenReturn(List.of()); // Simulate not found

        ApiResponse<?> response = rentalService.requestRental(1L, rentalRequest);

        assertEquals(404, response.getCode());
        assertEquals("One or more catalogs not found", response.getMessage());
    }

    @Test
    void testRequestRental_whenQuantityNotEnough_thenReturnError() {
        catalog.setAvailable(1); // Not enough for request (needs 2)
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(catalogRepository.findAllById(any())).thenReturn(List.of(catalog));

        ApiResponse<?> response = rentalService.requestRental(1L, rentalRequest);

        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("Not enough quantity"));
    }

    @Test
    void testRequestRental_whenAllValid_thenReturnSuccess() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(catalogRepository.findAllById(any())).thenReturn(List.of(catalog));
        when(rentalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<?> response = rentalService.requestRental(1L, rentalRequest);

        assertEquals(201, response.getCode());
        assertEquals("Rental request successfully created", response.getMessage());
    }
}

