package zydd.org.libsmanagement.Member.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MemberUpdateRequest {
    private String name;
    private String email;
    private String password;
}
