package zydd.org.libsmanagement.Member.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDetailResponse {
    private String email;
    private String name;
}
