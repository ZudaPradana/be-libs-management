package zydd.org.libsmanagement.Rent.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zydd.org.libsmanagement.Rent.Model.Rental;
import zydd.org.libsmanagement.Rent.Model.RentalStatus;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findByMemberId(Long memberId, Pageable pageable);
    Page<Rental> findByMemberIdAndStatus(Long memberId, RentalStatus status, Pageable pageable);
    Page<Rental> findAll(Specification<Rental> spec, Pageable pageable);
}
