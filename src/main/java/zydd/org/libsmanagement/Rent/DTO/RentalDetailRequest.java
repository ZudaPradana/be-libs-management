package zydd.org.libsmanagement.Rent.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalDetailRequest {
    private Long catalogId;
    private int quantity;
}
