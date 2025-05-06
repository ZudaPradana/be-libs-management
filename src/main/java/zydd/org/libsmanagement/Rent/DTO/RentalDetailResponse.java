package zydd.org.libsmanagement.Rent.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RentalDetailResponse {
    private Long id;
    private String member_name;
    private String catalog_title;
    private int quantity;
    private LocalDateTime rent_date;
    private LocalDateTime return_date;
    private String status;
}
