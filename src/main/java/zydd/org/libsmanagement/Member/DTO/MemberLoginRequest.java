package zydd.org.libsmanagement.Member.DTO;

import lombok.Data;

@Data
public class MemberLoginRequest {
    private String email;
    private String password;
}
