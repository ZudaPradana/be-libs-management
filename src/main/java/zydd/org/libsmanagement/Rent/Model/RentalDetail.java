package zydd.org.libsmanagement.Rent.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zydd.org.libsmanagement.Catalog.Model.Catalog;

@Entity
@Data
@Builder
@Table(name = "rental_detail")
@AllArgsConstructor
@NoArgsConstructor
public class RentalDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    @JsonBackReference
    private Rental rental;

    @ManyToOne
    private Catalog catalog;

    private Integer quantity;

    public RentalDetail(Catalog catalog, Integer quantity) {
        this.catalog = catalog;
        this.quantity = quantity;
    }
}
