package zydd.org.libsmanagement.Rent.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zydd.org.libsmanagement.Catalog.Model.Catalog;
import zydd.org.libsmanagement.Catalog.Repository.CatalogRepository;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Member.Model.Member;
import zydd.org.libsmanagement.Member.Model.Role;
import zydd.org.libsmanagement.Member.Repository.MemberRepository;
import zydd.org.libsmanagement.Rent.DTO.*;
import zydd.org.libsmanagement.Rent.Model.Rental;
import zydd.org.libsmanagement.Rent.Model.RentalDetail;
import zydd.org.libsmanagement.Rent.Model.RentalStatus;
import zydd.org.libsmanagement.Rent.Repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalServiceImp implements RentalService{
    private final RentalRepository rentalRepository;
    private final CatalogRepository catalogRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public ApiResponse<?> requestRental(Long memberId, RentalRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        if (!member.getRole().equals(Role.MEMBER)) {
            return ApiResponse.error(403, "Only members with role 'MEMBER' can request rentals", null);
        }

        List<Catalog> catalogs = catalogRepository.findAllById(request.getCatalogList().stream()
                .map(RentalDetailRequest::getCatalogId).collect(Collectors.toList()));

        if (catalogs.size() != request.getCatalogList().size()) {
            return ApiResponse.error(404, "One or more catalogs not found", null);
        }

        List<RentalDetail> rentalDetails = new ArrayList<>();
        for (RentalDetailRequest requestDetail : request.getCatalogList()) {
            Catalog catalog = catalogs.stream()
                    .filter(c -> c.getId().equals(requestDetail.getCatalogId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Catalog not found"));

            if (catalog.getAvailable() < requestDetail.getQuantity()) {
                return ApiResponse.error(400, "Not enough quantity for catalog: " + catalog.getTitle(), null);
            }

            // Kurangi jumlah stok yang dipinjam
            catalog.setAvailable(catalog.getQuantity() - requestDetail.getQuantity());
            rentalDetails.add(new RentalDetail(catalog, requestDetail.getQuantity()));
        }

        catalogRepository.saveAll(catalogs);

        Rental rental = Rental.builder()
                .member(member)
                .rentalDate(LocalDateTime.now())
                .returnDate(null)
                .status(RentalStatus.REQUESTED)
                .notes(request.getNote())
                .build();

        for (RentalDetail detail : rentalDetails) {
            detail.setRental(rental);
        }

        rental.setRentalDetails(rentalDetails);

        // Save Rental and RentalDetails
        Rental savedRental = rentalRepository.save(rental); // This line saves the rental

        // Return response with saved rental data
        return ApiResponse.success(201, "Rental request successfully created", savedRental);
    }


    @Override
    @Transactional
    public ApiResponse<?> approveRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        rental.setStatus(RentalStatus.APPROVED);
        rentalRepository.save(rental);

        return ApiResponse.success(200, "Rental request approved", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> rejectRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        if (!rental.getStatus().equals(RentalStatus.REQUESTED)) {
            return ApiResponse.error(400, "Cannot reject a rental that is not in 'REQUESTED' status", null);
        }

        // Mengembalikan available stock untuk catalog yang ada di rentalDetails
        for (RentalDetail detail : rental.getRentalDetails()) {
            Catalog catalog = detail.getCatalog();
            catalog.setAvailable(catalog.getAvailable() + detail.getQuantity()); // Kembalikan available
        }

        // Update status rental menjadi REJECTED
        rental.setStatus(RentalStatus.REJECTED);
        rentalRepository.save(rental); // Save rental status update

        return ApiResponse.success(200, "Rental request rejected and available stock restored", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        rental.setStatus(RentalStatus.RETURNED);
        rental.setReturnDate(LocalDateTime.now());
        rentalRepository.save(rental);

        for (RentalDetail detail : rental.getRentalDetails()) {
            Catalog catalog = detail.getCatalog();
            catalog.setAvailable(catalog.getAvailable() + detail.getQuantity());
            catalogRepository.save(catalog);
        }

        return ApiResponse.success(200, "Rental returned succeded.", null);
    }

    @Override
    public ApiResponse<Page<RentalDetailResponse>> getRentalsByMember(Long memberId, RentalStatus status, Pageable pageable) {
        Page<Rental> rentalsPage;

        if (status != null) {
            rentalsPage = rentalRepository.findByMemberIdAndStatus(memberId, status, pageable);
        } else {
            rentalsPage = rentalRepository.findByMemberId(memberId, pageable);
        }

        Page<RentalDetailResponse> rentalDetailResponses = rentalsPage.map(rental -> {
            String catalogTitles = rental.getRentalDetails().stream()
                    .map(detail -> detail.getCatalog().getTitle())
                    .collect(Collectors.joining(", "));

            int totalQuantity = rental.getRentalDetails().stream()
                    .mapToInt(RentalDetail::getQuantity)
                    .sum();

            return RentalDetailResponse.builder()
                    .id(rental.getId())
                    .member_name(rental.getMember().getName())
                    .catalog_title(catalogTitles)
                    .quantity(totalQuantity)
                    .rent_date(rental.getRentalDate())
                    .return_date(rental.getReturnDate())
                    .status(rental.getStatus().name())
                    .build();
        });

        return ApiResponse.success(200, "Rentals fetched successfully", rentalDetailResponses);
    }


    @Override
    public ApiResponse<Page<RentalResponse>> getAllRentals(String memberName, String status, Pageable pageable) {
        Specification<Rental> spec = Specification.where(null);

        if (memberName != null && !memberName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("member").get("name")), "%" + memberName.toLowerCase() + "%"));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), RentalStatus.valueOf(status)));
        }

        Page<Rental> rentals = rentalRepository.findAll(spec, pageable);

        Page<RentalResponse> rentalResponses = rentals.map(rental -> {
            String titles = rental.getRentalDetails().stream()
                    .map(detail -> detail.getCatalog().getTitle())
                    .collect(Collectors.joining(", "));

            int totalQty = rental.getRentalDetails().stream()
                    .mapToInt(RentalDetail::getQuantity)
                    .sum();

            return RentalResponse.builder()
                    .id(rental.getId())
                    .member_name(rental.getMember().getName())
                    .catalog_title(titles)
                    .quantity(totalQty)
                    .rent_date(rental.getRentalDate())
                    .return_date(rental.getReturnDate())
                    .status(rental.getStatus().name())
                    .build();
        });

        return ApiResponse.success(200, "Rentals fetched successfully", rentalResponses);
    }

}
