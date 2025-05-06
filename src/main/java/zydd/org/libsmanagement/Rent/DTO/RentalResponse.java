package zydd.org.libsmanagement.Rent.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RentalResponse {
    private Long id;
    private String member_name;
    private String catalog_title;
    private LocalDateTime rent_date;
    private LocalDateTime return_date;
    private String status;
    private int quantity;
}
