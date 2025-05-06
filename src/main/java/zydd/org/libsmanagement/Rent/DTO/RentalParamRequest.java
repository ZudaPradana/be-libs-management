package zydd.org.libsmanagement.Rent.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RentalParamRequest {
    private String memberName;
    private String status;
}
