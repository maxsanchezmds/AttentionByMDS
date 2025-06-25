package com.attention.analysis.auth_service.security;

import com.attention.analysis.auth_service.model.Usuario;
import com.attention.analysis.auth_service.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set secret key and expiration values
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateAndValidateToken() {
        Usuario usuario = Usuario.builder()
                .correoElectronico("test@x.com")
                .password("pwd")
                .rol(Rol.ADMIN)
                .build();

        String token = jwtService.generateToken(usuario);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@x.com");
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }
}