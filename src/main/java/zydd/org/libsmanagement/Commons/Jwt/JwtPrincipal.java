package zydd.org.libsmanagement.Commons.Jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtPrincipal {
    private Long id;
    private String role;

}
