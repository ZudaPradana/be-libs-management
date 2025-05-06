package zydd.org.libsmanagement.Rent.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RentalRequest {
    private List<RentalDetailRequest> catalogList;
    private String note;
}
