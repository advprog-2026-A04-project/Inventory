package id.ac.ui.cs.advprog.inventory.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void parseTokenShouldReadClaimsFromPlainSecret() {
        String secret = "json-milestone-secret-json-milestone-secret";
        JwtService jwtService = new JwtService(secret);
        String token = Jwts.builder()
                .subject("1001")
                .claims(Map.of("role", "TITIPER"))
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Claims claims = jwtService.parseToken(token);

        assertEquals("1001", claims.getSubject());
        assertEquals("TITIPER", claims.get("role", String.class));
    }

    @Test
    void parseTokenShouldReadClaimsFromBase64Secret() {
        String secret = "c29tZS1iYXNlNjQtc2VjcmV0LXN0cmluZy1mb3ItdGVzdGluZw==";
        JwtService jwtService = new JwtService(secret);
        String token = Jwts.builder()
                .subject("2002")
                .claims(Map.of("role", "ADMIN"))
                .issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret)))
                .compact();

        Claims claims = jwtService.parseToken(token);

        assertEquals("2002", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }
}
