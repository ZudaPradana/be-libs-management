package zydd.org.libsmanagement.Catalog.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zydd.org.libsmanagement.Catalog.Model.Catalog;

import java.util.List;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    Page<Catalog> findAll(Specification<Catalog> spec, Pageable pageable);
}
