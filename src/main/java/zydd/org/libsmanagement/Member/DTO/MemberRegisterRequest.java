package zydd.org.libsmanagement.Member.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zydd.org.libsmanagement.Member.Model.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
}
