package zydd.org.libsmanagement.Rent.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Rent.DTO.*;
import zydd.org.libsmanagement.Rent.Model.RentalStatus;


public interface RentalService {
    ApiResponse<?> requestRental(Long memberId, RentalRequest request);
    ApiResponse<?> approveRental(Long rentalId);
    ApiResponse<?> rejectRental(Long rentalId);
    ApiResponse<?> returnRental(Long rentalId);
    ApiResponse<Page<RentalDetailResponse>> getRentalsByMember(Long memberId, RentalStatus status, Pageable pageable);

    ApiResponse<Page<RentalResponse>> getAllRentals(String memberName, String status, Pageable pageable);
}
