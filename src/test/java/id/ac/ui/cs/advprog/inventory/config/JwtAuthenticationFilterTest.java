package id.ac.ui.cs.advprog.inventory.config;

import id.ac.ui.cs.advprog.inventory.model.Product;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldIgnoreRequestsWithoutBearerToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/search");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {
        String secret = "json-milestone-secret-json-milestone-secret";
        JwtService jwtService = new JwtService(secret);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        String token = io.jsonwebtoken.Jwts.builder()
                .subject("2001")
                .claims(java.util.Map.of("role", "TITIPER"))
                .issuedAt(new java.util.Date())
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/search");
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("2001", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parseToken("bad")).thenThrow(new JwtException("bad"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/search");
        request.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Invalid or expired token.\"}", response.getContentAsString());
    }
}
