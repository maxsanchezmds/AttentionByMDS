package com.attention.analysis.api_gateway.service;
import com.attention.analysis.api_gateway.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
    }

    private String generateToken(String username, long expMillis) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expMillis))
                .signWith(key)
                .compact();
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = generateToken("user@test.com", 60000);
        String username = jwtService.extractUsername(token);
        assertEquals("user@test.com", username);
    }

    @Test
    void isTokenValid_trueForValidToken() {
        String token = generateToken("user", 60000);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_falseForExpiredToken() {
        String token = generateToken("user", -1000);
        assertFalse(jwtService.isTokenValid(token));
    }
}