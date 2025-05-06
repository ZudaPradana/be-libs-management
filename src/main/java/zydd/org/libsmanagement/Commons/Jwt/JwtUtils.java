package zydd.org.libsmanagement.Commons.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import zydd.org.libsmanagement.Member.Model.Member;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final SecretKey SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateToken(Member member) {
        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("role", member.getRole())
                .claim("fullname", member.getName())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        }catch (Exception e) {
            return false;
        }
    }
}
