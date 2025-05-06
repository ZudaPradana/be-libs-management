package zydd.org.libsmanagement.Rent.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Commons.Jwt.JwtPrincipal;
import zydd.org.libsmanagement.Rent.DTO.RentalDetailResponse;
import zydd.org.libsmanagement.Rent.DTO.RentalParamRequest;
import zydd.org.libsmanagement.Rent.DTO.RentalRequest;
import zydd.org.libsmanagement.Rent.DTO.RentalResponse;
import zydd.org.libsmanagement.Rent.Model.RentalStatus;
import zydd.org.libsmanagement.Rent.Service.RentalService;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @RolesAllowed("MEMBER")
    @PostMapping("/request")
    public ResponseEntity<?> requestRental(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody RentalRequest catalogList,
            @RequestParam(required = false) String notes) {
        Long memberId = principal.getId();
        ApiResponse<?> response = rentalService.requestRental(memberId, catalogList);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @RolesAllowed("ADMIN")
    @PutMapping("/approve/{rentalId}")
    public ResponseEntity<?> approveRental(@PathVariable Long rentalId) {
        ApiResponse<?> response = rentalService.approveRental(rentalId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @RolesAllowed("ADMIN")
    @PutMapping("/reject/{rentalId}")
    public ResponseEntity<?> rejectRental(
            @PathVariable Long rentalId) {
        ApiResponse<?> response = rentalService.rejectRental(rentalId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @RolesAllowed("MEMBER")
    @PutMapping("/return/{rentalId}")
    public ResponseEntity<?> returnRental(@PathVariable Long rentalId) {
        ApiResponse<?> response = rentalService.returnRental(rentalId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @RolesAllowed({"MEMBER", "ADMIN"})
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<RentalDetailResponse>>> getRentalsByMember(
            @PathVariable Long memberId,
            @RequestParam(required = false) RentalStatus status,
            @PageableDefault(size = 10) Pageable pageable) {

        Pageable fixedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort()
        );

        ApiResponse<Page<RentalDetailResponse>> response = rentalService.getRentalsByMember(memberId, status, fixedPageable);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @RolesAllowed("ADMIN")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RentalResponse>>> getAllRentals(
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {

        Pageable fixedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort()
        );

        ApiResponse<Page<RentalResponse>> response = rentalService.getAllRentals(memberName, status, fixedPageable);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
